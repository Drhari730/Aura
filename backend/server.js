import crypto from "node:crypto";
import fs from "node:fs/promises";
import { readFileSync } from "node:fs";
import http from "node:http";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const env = loadEnv(path.join(__dirname, ".env"));
const PORT = Number(process.env.PORT || env.PORT || 8787);
const BREVO_API_KEY = process.env.BREVO_API_KEY || env.BREVO_API_KEY || "";
const OTP_FROM_EMAIL = process.env.OTP_FROM_EMAIL || env.OTP_FROM_EMAIL || "Aura AMR <info@vicharaqda.in>";
const OTP_SECRET = process.env.OTP_SECRET || env.OTP_SECRET || "dev-only-change-this-secret";
const ALLOW_OTP_FALLBACK = String(process.env.ALLOW_OTP_FALLBACK || env.ALLOW_OTP_FALLBACK || "false") === "true";
const OTP_STORE_PATH = process.env.OTP_STORE_PATH || env.OTP_STORE_PATH || path.join(__dirname, "data", "otps.json");

function loadEnv(filePath) {
  try {
    const text = readFileSync(filePath, "utf8");
    return Object.fromEntries(
      text
        .split(/\r?\n/)
        .map((line) => line.trim())
        .filter((line) => line && !line.startsWith("#") && line.includes("="))
        .map((line) => {
          const [key, ...rest] = line.split("=");
          return [key.trim(), rest.join("=").trim()];
        })
    );
  } catch {
    return {};
  }
}

async function readOtps() {
  try {
    const text = await fs.readFile(OTP_STORE_PATH, "utf8");
    const data = JSON.parse(text);
    return Array.isArray(data.otps) ? data.otps : [];
  } catch {
    return [];
  }
}

async function writeOtps(otps) {
  await fs.mkdir(path.dirname(OTP_STORE_PATH), { recursive: true });
  await fs.writeFile(OTP_STORE_PATH, JSON.stringify({ otps }, null, 2), "utf8");
}

function normalizeEmail(value) {
  const email = String(value || "").trim().toLowerCase();
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email) ? email : "";
}

function hashOtp(email, code) {
  return crypto
    .createHmac("sha256", OTP_SECRET)
    .update(`${email}:${code}`)
    .digest("hex");
}

function randomOtp() {
  return String(crypto.randomInt(100000, 1000000));
}

function parseSender(from) {
  const match = String(from || "").match(/^(.*?)<([^>]+)>$/);
  if (match) return { name: match[1].trim() || "Aura AMR", email: match[2].trim() };
  return { name: "Aura AMR", email: String(from || "info@vicharaqda.in").trim() };
}

async function sendBrevoOtp(email, code) {
  if (!BREVO_API_KEY) {
    return { sent: false, reason: "BREVO_API_KEY is not configured" };
  }

  const response = await fetch("https://api.brevo.com/v3/smtp/email", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "api-key": BREVO_API_KEY
    },
    body: JSON.stringify({
      sender: parseSender(OTP_FROM_EMAIL),
      to: [{ email }],
      subject: "Your Aura AMR verification code",
      htmlContent: `
        <div style="font-family:Arial,sans-serif;line-height:1.5;color:#142033;">
          <h2>Your Aura AMR verification code</h2>
          <p>Enter this 6-digit code in the Aura app:</p>
          <p style="font-size:32px;font-weight:700;letter-spacing:8px;margin:16px 0;">${code}</p>
          <p>This code expires in 10 minutes. Do not share it with anyone.</p>
          <p>If you did not request this code, you can ignore this email.</p>
        </div>
      `,
      textContent: `Your Aura AMR verification code is ${code}. This code expires in 10 minutes.`
    })
  });

  const data = await response.json().catch(() => ({}));
  return {
    sent: response.ok,
    reason: data.message || data.error || response.statusText
  };
}

function json(res, status, body) {
  res.writeHead(status, {
    "Content-Type": "application/json; charset=utf-8",
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Headers": "Content-Type, Authorization",
    "Access-Control-Allow-Methods": "GET, POST, OPTIONS"
  });
  res.end(status === 204 ? "" : JSON.stringify(body));
}

async function bodyJson(req) {
  const chunks = [];
  for await (const chunk of req) chunks.push(chunk);
  if (!chunks.length) return {};
  return JSON.parse(Buffer.concat(chunks).toString("utf8"));
}

async function requestOtp(req, res) {
  const input = await bodyJson(req);
  const email = normalizeEmail(input.email);
  if (!email) return json(res, 400, { error: "Enter a valid email address." });

  const now = Date.now();
  const existing = (await readOtps()).filter((otp) => otp.expiresAt > now);
  const recent = existing.find((otp) => otp.email === email && otp.createdAt > now - 60_000);
  if (recent) return json(res, 429, { error: "Please wait one minute before requesting another code." });

  const code = randomOtp();
  const otps = existing.filter((otp) => otp.email !== email);
  otps.push({
    email,
    codeHash: hashOtp(email, code),
    expiresAt: now + 10 * 60 * 1000,
    createdAt: now,
    attempts: 0
  });
  await writeOtps(otps);

  let delivery;
  try {
    delivery = await sendBrevoOtp(email, code);
  } catch (error) {
    delivery = { sent: false, reason: error.message || "Brevo delivery failed" };
  }

  if (!delivery.sent && !ALLOW_OTP_FALLBACK) {
    await writeOtps(otps.filter((otp) => otp.email !== email));
    return json(res, 502, { error: "OTP email could not be sent. " + (delivery.reason || "") });
  }

  return json(res, 200, {
    ok: true,
    delivery: delivery.sent ? "email-sent" : delivery.reason,
    devOtp: !delivery.sent && ALLOW_OTP_FALLBACK ? code : undefined
  });
}

async function verifyOtp(req, res) {
  const input = await bodyJson(req);
  const email = normalizeEmail(input.email);
  const code = String(input.code || "").trim();
  if (!email) return json(res, 400, { error: "Enter a valid email address." });
  if (!/^\d{6}$/.test(code)) return json(res, 400, { error: "Enter the 6-digit OTP." });

  const now = Date.now();
  const otps = (await readOtps()).filter((otp) => otp.expiresAt > now);
  const otp = otps.find((item) => item.email === email);
  if (!otp) return json(res, 401, { error: "OTP expired. Please request a fresh code." });
  if (otp.attempts >= 5) return json(res, 429, { error: "Too many attempts. Please request a fresh code." });

  if (otp.codeHash !== hashOtp(email, code)) {
    otp.attempts += 1;
    await writeOtps(otps);
    return json(res, 401, { error: "Incorrect OTP. Please check the latest email code." });
  }

  await writeOtps(otps.filter((item) => item.email !== email));
  return json(res, 200, { ok: true, verified: true });
}

const server = http.createServer(async (req, res) => {
  try {
    if (req.method === "OPTIONS") return json(res, 204, {});
    const url = new URL(req.url, "http://localhost");

    if (req.method === "GET" && url.pathname === "/api/health") {
      return json(res, 200, { ok: true, service: "aura-brevo-otp" });
    }

    if (req.method === "POST" && url.pathname === "/api/auth/request-otp") {
      return await requestOtp(req, res);
    }

    if (req.method === "POST" && url.pathname === "/api/auth/verify-otp") {
      return await verifyOtp(req, res);
    }

    return json(res, 404, { error: "Not found" });
  } catch (error) {
    return json(res, 500, { error: error.message || "Server error" });
  }
});

server.listen(PORT, () => {
  console.log(`Aura Brevo OTP backend listening on port ${PORT}`);
});

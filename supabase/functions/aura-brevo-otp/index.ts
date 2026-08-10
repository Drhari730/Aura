import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
const expectedAnonKey = Deno.env.get("SUPABASE_ANON_KEY") ?? "";
const brevoApiKey = Deno.env.get("BREVO_API_KEY") ?? "";
const senderEmail = Deno.env.get("BREVO_SENDER_EMAIL") ?? "info@vicharaqda.in";
const senderName = Deno.env.get("BREVO_SENDER_NAME") ?? "Aura AMR";
const otpPepper = Deno.env.get("OTP_PEPPER") ?? serviceRoleKey.slice(-32);

const supabase = createClient(supabaseUrl, serviceRoleKey, {
  auth: { persistSession: false },
});

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") {
    return json(204, {});
  }

  if (req.method !== "POST") {
    return json(405, { error: "Method not allowed" });
  }

  try {
    const requestApiKey = req.headers.get("apikey") ?? "";
    if (expectedAnonKey && requestApiKey !== expectedAnonKey) {
      return json(401, { error: "Unauthorized OTP request." });
    }

    const input = await req.json().catch(() => ({}));
    const action = String(input.action ?? "").trim().toLowerCase();
    const email = normalizeEmail(input.email);

    if (!email) {
      return json(400, { error: "Enter a valid email address." });
    }

    if (action === "send") {
      return await sendOtp(email);
    }

    if (action === "verify") {
      const code = String(input.code ?? "").trim();
      return await verifyOtp(email, code);
    }

    return json(400, { error: "Unsupported OTP action." });
  } catch (error) {
    return json(500, {
      error: error instanceof Error ? error.message : "OTP service failed.",
    });
  }
});

async function sendOtp(email: string): Promise<Response> {
  if (!supabaseUrl || !serviceRoleKey) {
    return json(500, { error: "Supabase service role is not configured on the server." });
  }

  if (!brevoApiKey) {
    return json(500, { error: "Brevo is not configured on the server." });
  }

  const oneMinuteAgo = new Date(Date.now() - 60_000).toISOString();
  const { data: recent, error: recentError } = await supabase
    .from("email_otp_codes")
    .select("id")
    .eq("email", email)
    .is("consumed_at", null)
    .gt("created_at", oneMinuteAgo)
    .limit(1);

  if (recentError) {
    return json(500, { error: "Could not check OTP rate limit." });
  }

  if ((recent ?? []).length > 0) {
    return json(429, { error: "Please wait one minute before requesting another code." });
  }

  const code = String(crypto.getRandomValues(new Uint32Array(1))[0] % 900000 + 100000);
  const codeHash = await hashOtp(email, code);
  const expiresAt = new Date(Date.now() + 10 * 60_000).toISOString();

  await supabase
    .from("email_otp_codes")
    .delete()
    .eq("email", email)
    .is("consumed_at", null);

  const { error: insertError } = await supabase
    .from("email_otp_codes")
    .insert({ email, code_hash: codeHash, expires_at: expiresAt });

  if (insertError) {
    return json(500, { error: "Could not create OTP." });
  }

  const delivery = await sendBrevoEmail(email, code);
  if (!delivery.ok) {
    await supabase
      .from("email_otp_codes")
      .delete()
      .eq("email", email)
      .is("consumed_at", null);

    return json(502, { error: delivery.error || "OTP email could not be sent." });
  }

  return json(200, { ok: true, delivery: "email-sent", expiresInSeconds: 600 });
}

async function verifyOtp(email: string, code: string): Promise<Response> {
  if (!/^\d{6}$/.test(code)) {
    return json(400, { error: "Enter the 6-digit OTP." });
  }

  const { data, error } = await supabase
    .from("email_otp_codes")
    .select("id, code_hash, attempts, expires_at")
    .eq("email", email)
    .is("consumed_at", null)
    .gt("expires_at", new Date().toISOString())
    .order("created_at", { ascending: false })
    .limit(1)
    .maybeSingle();

  if (error) {
    return json(500, { error: "Could not verify OTP." });
  }

  if (!data) {
    return json(401, { error: "OTP expired. Please request a fresh code." });
  }

  if (Number(data.attempts ?? 0) >= 5) {
    return json(429, { error: "Too many attempts. Please request a fresh code." });
  }

  const expectedHash = await hashOtp(email, code);
  if (expectedHash !== data.code_hash) {
    await supabase
      .from("email_otp_codes")
      .update({ attempts: Number(data.attempts ?? 0) + 1 })
      .eq("id", data.id);

    return json(401, { error: "Incorrect OTP. Please check the latest email code." });
  }

  await supabase
    .from("email_otp_codes")
    .update({ consumed_at: new Date().toISOString() })
    .eq("id", data.id);

  return json(200, { ok: true, verified: true });
}

async function sendBrevoEmail(
  email: string,
  code: string,
): Promise<{ ok: boolean; error?: string }> {
  const response = await fetch("https://api.brevo.com/v3/smtp/email", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "api-key": brevoApiKey,
    },
    body: JSON.stringify({
      sender: { name: senderName, email: senderEmail },
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
      textContent:
        `Your Aura AMR verification code is ${code}. This code expires in 10 minutes.`,
    }),
  });

  if (response.ok) {
    return { ok: true };
  }

  const data = await response.json().catch(() => ({}));
  return {
    ok: false,
    error: String(data.message ?? data.error ?? response.statusText),
  };
}

function normalizeEmail(value: unknown): string {
  const email = String(value ?? "").trim().toLowerCase();
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email) ? email : "";
}

async function hashOtp(email: string, code: string): Promise<string> {
  const data = new TextEncoder().encode(`${email}:${code}:${otpPepper}`);
  const digest = await crypto.subtle.digest("SHA-256", data);
  return Array.from(new Uint8Array(digest))
    .map((byte) => byte.toString(16).padStart(2, "0"))
    .join("");
}

function json(status: number, body: Record<string, unknown>): Response {
  return new Response(status === 204 ? null : JSON.stringify(body), {
    status,
    headers: {
      ...corsHeaders,
      "Content-Type": "application/json; charset=utf-8",
    },
  });
}

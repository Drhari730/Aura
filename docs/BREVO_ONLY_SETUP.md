# Aura Brevo-Only Login Setup

Aura now uses a Brevo-backed OTP backend like the OncoDiet pattern.

## Flow

1. Patient enters email in the Aura app.
2. App calls `POST /api/auth/request-otp`.
3. Backend generates a 6-digit OTP, stores only a hash with expiry, and sends the visible code through Brevo.
4. Patient enters the OTP inside Aura.
5. App calls `POST /api/auth/verify-otp`.

No magic link. No browser redirect. No Brevo key inside the APK.

## Android Configuration

Set this in `local.properties` or as an environment variable before building:

```properties
AURA_API_BASE_URL=https://your-deployed-backend.example.com
```

For Android emulator testing against a backend running on the same computer:

```properties
AURA_API_BASE_URL=http://10.0.2.2:8787
```

If `AURA_API_BASE_URL` is blank, the debug APK shows the OTP screen and allows demo code `123456` for local UI testing only.

## Backend Configuration

Backend folder:

```text
backend/
```

Create `backend/.env` on the server:

```text
PORT=8787
BREVO_API_KEY=xkeysib-your-key
OTP_FROM_EMAIL=Aura AMR <info@vicharaqda.in>
OTP_SECRET=make-a-long-random-secret
OTP_STORE_PATH=./data/otps.json
ALLOW_OTP_FALLBACK=false
```

Run:

```powershell
cd backend
npm start
```

Deploy this backend to any Node hosting service, then put that public HTTPS URL into `AURA_API_BASE_URL` before building the final APK.

## Important

Brevo is an email delivery provider, not a database. The included backend stores short-lived hashed OTPs in `backend/data/otps.json`. That is fine for a single small deployment. For a larger production deployment, replace the file store with a database or Redis.

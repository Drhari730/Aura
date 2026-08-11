# Aura Brevo OTP Backend

This is the Brevo-only OTP backend for Aura. The Android app calls:

- `POST /api/auth/request-otp` with `{ "email": "patient@example.com" }`
- `POST /api/auth/verify-otp` with `{ "email": "patient@example.com", "code": "123456" }`

The Brevo API key stays on this server. Never put it inside the Android APK.

## Environment

Create `backend/.env` on the server:

```text
PORT=8787
BREVO_API_KEY=xkeysib-your-key
OTP_FROM_EMAIL=Aura AMR <info@vicharaqda.in>
OTP_SECRET=make-a-long-random-secret
OTP_STORE_PATH=./data/otps.json
ALLOW_OTP_FALLBACK=false
```

Run locally:

```powershell
cd backend
npm start
```

Then build Android with:

```properties
AURA_API_BASE_URL=https://your-deployed-backend.example.com
```

For local emulator testing, use the computer host address:

```properties
AURA_API_BASE_URL=http://10.0.2.2:8787
```

## Production Notes

Brevo sends emails only; it is not a database or full authentication server. This backend stores short-lived hashed OTP records in `backend/data/otps.json`. For production with multiple server instances, replace file storage with a persistent database or Redis.

# Aura Supabase + Brevo OTP Setup

Aura now uses an app-first OTP flow like OncoDiet:

1. The Android app asks for the patient email.
2. The app calls the Supabase Edge Function `aura-brevo-otp`.
3. The Edge Function generates a 6-digit code, stores only a hash in Supabase, and sends the visible code using Brevo.
4. The patient types the code inside the app.

This avoids Supabase Auth magic links and prevents emails from opening `localhost:3000`.

## Android Build Values

Add these to `local.properties` on the developer machine, or provide them as environment variables before building:

```properties
SUPABASE_URL=https://YOUR_PROJECT_ID.supabase.co
SUPABASE_ANON_KEY=YOUR_PUBLIC_PUBLISHABLE_KEY
AURA_OTP_FUNCTION=aura-brevo-otp
```

Do not commit real keys. The Supabase publishable key is okay in the app; the Brevo API key is not.

## Database

The OTP table is defined in:

```text
supabase/migrations/20260810143000_create_aura_brevo_otp_codes.sql
```

It stores hashed OTP codes, expiry time, attempt count, and consumed time. Row Level Security is enabled and there are no public policies; the Edge Function uses the server-side service role key.

## Edge Function

Function source:

```text
supabase/functions/aura-brevo-otp/index.ts
```

Required Supabase secrets:

```text
BREVO_API_KEY=your-brevo-api-key
BREVO_SENDER_EMAIL=verified-sender@example.com
BREVO_SENDER_NAME=Aura AMR
OTP_PEPPER=random-long-secret
```

`SUPABASE_URL`, `SUPABASE_ANON_KEY`, and `SUPABASE_SERVICE_ROLE_KEY` are normally available to Supabase Edge Functions automatically.

Deploy with the Supabase CLI after logging in:

```powershell
supabase link --project-ref niuzekyrjugnfwvcsles
supabase secrets set BREVO_API_KEY="..." BREVO_SENDER_EMAIL="info@vicharaqda.in" BREVO_SENDER_NAME="Aura AMR" OTP_PEPPER="make-a-long-random-value"
supabase functions deploy aura-brevo-otp
```

The function is configured with `verify_jwt = false` because new patients do not have a session before OTP verification. It still checks the `apikey` header and rate-limits repeated OTP sends.

## Brevo Sender

You do not need to buy a new domain just for Play Store upload. For reliable OTP delivery, Brevo must allow the sender email you use. The most affordable route is:

- Use an existing domain email if Brevo can verify it, for example `info@vicharaqda.in`.
- If domain verification is not possible, verify a sender email in Brevo first and use that as `BREVO_SENDER_EMAIL`.
- Do not put the Brevo API key inside the Android app.

## Admin Dashboard Later

Minimum admin dashboard modules for the thesis/study:

- Admin login for researcher
- Patient list and filters
- Antibiotic courses per patient
- Adherence percentage and missed-dose report
- ADR/severe-side-effect alerts
- Learning and safety milestone completion
- CSV export for analysis

Never place the Supabase service-role key or Brevo API key inside the Android APK.

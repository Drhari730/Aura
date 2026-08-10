# Aura Supabase Setup

## Mobile Auth

Aura uses Supabase email OTP when these build values are present:

```properties
SUPABASE_URL=https://YOUR_PROJECT_ID.supabase.co
SUPABASE_ANON_KEY=YOUR_PUBLIC_ANON_KEY
```

Add them to `local.properties` on the developer machine, or provide them as environment variables before building. Do not commit real keys.

When these values are blank, the debug app falls back to the local demo OTP `123456`.

## Supabase Auth Settings

1. Create a Supabase project.
2. Open Authentication > Providers > Email.
3. Enable email provider and OTP/magic-link email login.
4. Open Authentication > Email Templates.
5. Edit both **Confirm signup** and **Magic Link** templates so the email shows the OTP token, not only a confirmation link.
6. Open Authentication > URL Configuration and remove `localhost:3000` before production.
7. Open Authentication > SMTP Settings.
8. Configure a production SMTP provider, preferably Brevo or Resend after sender/domain verification.

### Required Email OTP Template

The Android app expects the patient to type a 6-digit code. Supabase generates this as `{{ .Token }}`. If the template only contains `{{ .ConfirmationURL }}`, the patient receives a link that opens `localhost:3000`, which will not work for this mobile app.

Supabase requires **custom SMTP** before templates can be edited. Set up Brevo SMTP first, then edit the templates below.

Brevo SMTP values usually look like this:

```text
Host: smtp-relay.brevo.com
Port: 587
Username: your Brevo SMTP login email
Password: your Brevo SMTP key
Sender email: verified Brevo sender email
Sender name: Aura AMR
```

The Brevo API key is not always the same as the SMTP key. Use the SMTP key shown in Brevo > SMTP & API.

Use this subject:

```text
Your Aura AMR verification code
```

Use this body for **Confirm signup** and **Magic Link**:

```html
<h2>Your Aura AMR verification code</h2>
<p>Enter this 6-digit code in the Aura app:</p>
<h1 style="font-size:32px;letter-spacing:8px;">{{ .Token }}</h1>
<p>This code expires soon. Do not share it with anyone.</p>
<p>If you did not request this code, you can ignore this email.</p>
```

Do not ask patients to tap the confirmation link unless deep links are added to the Android app and the Supabase redirect URLs are configured for that app scheme.

## Recommended Tables

```sql
create table public.patient_profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  email text not null,
  name text,
  age text,
  gender text,
  state text,
  district text,
  city text,
  pincode text,
  created_at timestamptz not null default now()
);

create table public.medications (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  name text not null,
  dose text not null,
  frequency text not null,
  start_date date not null,
  end_date date not null,
  dose_times text not null,
  is_active boolean not null default true,
  created_at timestamptz not null default now()
);

create table public.dose_logs (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  medication_id uuid references public.medications(id) on delete cascade,
  medication_name text not null,
  dose_label text not null,
  scheduled_date date not null,
  status text not null check (status in ('taken', 'missed')),
  taken_at timestamptz not null default now()
);

create table public.adr_reports (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  medication text,
  symptoms text not null,
  severity text not null,
  notes text,
  created_at timestamptz not null default now()
);

create table public.education_progress (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  lesson_key text not null,
  completed_at timestamptz not null default now(),
  unique (user_id, lesson_key)
);
```

Enable Row Level Security and add policies so patients can read/write only their own rows. Admin dashboards should use a separate admin role or Supabase service-role key on the server only.

## Admin Dashboard

Minimum dashboard modules:

- Login for researcher/admin
- Patient list and filters
- Individual antibiotic courses
- Dose adherence percentage
- Missed-dose list
- ADR/severe-side-effect alerts
- Education and safety badge completion
- CSV export for thesis analysis

Never place the Supabase service-role key inside the Android app.

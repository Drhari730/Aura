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
4. Open Authentication > URL Configuration.
5. Add the app redirect URLs later when deep links are added.
6. Open Authentication > SMTP Settings.
7. Configure a production SMTP provider, preferably Resend after domain verification.

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

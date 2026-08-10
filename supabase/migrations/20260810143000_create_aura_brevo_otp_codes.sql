create table if not exists public.email_otp_codes (
  id uuid primary key default gen_random_uuid(),
  email text not null,
  code_hash text not null,
  expires_at timestamptz not null,
  consumed_at timestamptz,
  attempts integer not null default 0,
  created_at timestamptz not null default now()
);

create index if not exists email_otp_codes_email_created_idx
  on public.email_otp_codes (lower(email), created_at desc);

create index if not exists email_otp_codes_expiry_idx
  on public.email_otp_codes (expires_at)
  where consumed_at is null;

alter table public.email_otp_codes enable row level security;

comment on table public.email_otp_codes is
  'Short-lived Aura AMR email OTP records for Brevo-backed in-app verification. Access is via Supabase Edge Function/service role only.';

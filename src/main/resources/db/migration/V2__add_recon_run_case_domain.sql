create table recon_runs (
    id uuid primary key,
    batch_id uuid not null references settlement_import_batches(id),
    approved_from timestamp with time zone,
    approved_to timestamp with time zone,
    status varchar(32) not null,
    paybridge_row_count integer not null,
    settlement_row_count integer not null,
    case_count integer not null,
    started_at timestamp with time zone not null,
    finished_at timestamp with time zone,
    error_summary varchar(1000),
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table paybridge_snapshots (
    id uuid primary key,
    run_id uuid not null references recon_runs(id),
    payment_id uuid not null,
    order_id varchar(128),
    provider varchar(32) not null,
    status varchar(32) not null,
    amount_minor bigint not null,
    reversible_amount_minor bigint not null,
    currency varchar(8) not null,
    provider_payment_id varchar(128),
    provider_transaction_id varchar(128),
    approved_at timestamp with time zone,
    upstream_created_at timestamp with time zone,
    upstream_updated_at timestamp with time zone,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table recon_cases (
    id uuid primary key,
    run_id uuid not null references recon_runs(id),
    case_type varchar(64) not null,
    case_status varchar(32) not null,
    provider varchar(32) not null,
    payment_id uuid,
    settlement_row_id uuid references settlement_rows(id),
    summary varchar(500) not null,
    match_key varchar(255) not null,
    opened_at timestamp with time zone not null,
    resolved_at timestamp with time zone,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table recon_case_notes (
    id uuid primary key,
    case_id uuid not null references recon_cases(id),
    author varchar(100) not null,
    body varchar(2000) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create index idx_recon_runs_started_at on recon_runs(started_at desc);
create index idx_recon_runs_batch_id on recon_runs(batch_id);

create unique index uk_paybridge_snapshots_run_payment on paybridge_snapshots(run_id, payment_id);
create index idx_paybridge_snapshots_run_provider_payment_id on paybridge_snapshots(run_id, provider, provider_payment_id);
create index idx_paybridge_snapshots_run_provider_transaction_id on paybridge_snapshots(run_id, provider, provider_transaction_id);
create index idx_paybridge_snapshots_run_provider_order_id on paybridge_snapshots(run_id, provider, order_id);

create index idx_recon_cases_run_id on recon_cases(run_id);
create index idx_recon_cases_status on recon_cases(case_status);
create index idx_recon_cases_type on recon_cases(case_type);
create index idx_recon_cases_provider on recon_cases(provider);
create index idx_recon_cases_payment_id on recon_cases(payment_id);
create index idx_recon_cases_settlement_row_id on recon_cases(settlement_row_id);

create index idx_recon_case_notes_case_id on recon_case_notes(case_id);
create index idx_recon_case_notes_created_at on recon_case_notes(created_at);

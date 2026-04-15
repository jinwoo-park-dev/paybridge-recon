create table settlement_import_batches (
    id uuid primary key,
    filename varchar(255) not null,
    row_count integer not null,
    uploaded_at timestamp with time zone not null,
    uploaded_by varchar(100) not null,
    status varchar(32) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table settlement_rows (
    id uuid primary key,
    batch_id uuid not null references settlement_import_batches(id),
    row_number integer not null,
    provider varchar(32) not null,
    order_id varchar(128),
    provider_payment_id varchar(128),
    provider_transaction_id varchar(128),
    amount_minor bigint not null,
    currency varchar(8) not null,
    settled_at timestamp with time zone not null,
    raw_row_json varchar(4000) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create index idx_settlement_batches_uploaded_at on settlement_import_batches(uploaded_at desc);
create index idx_settlement_rows_batch_id on settlement_rows(batch_id);
create index idx_settlement_rows_provider_order on settlement_rows(provider, order_id);
create index idx_settlement_rows_provider_payment_id on settlement_rows(provider, provider_payment_id);
create index idx_settlement_rows_provider_transaction_id on settlement_rows(provider, provider_transaction_id);
create unique index uk_settlement_rows_batch_row_number on settlement_rows(batch_id, row_number);

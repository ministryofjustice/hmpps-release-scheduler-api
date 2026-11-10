create table if not exists release_audit
(
    rev_id                 bigint      not null,
    rev_type               smallint    not null,
    id                     uuid        not null,
    version                int         not null,
    person_identifier      varchar(10) not null,
    status_code            varchar(32) not null,
    reason_code            varchar(32),
    confirmed_release_date date,
    auto_release_date      date,
    approved_release_date  date,
    comments               text,
    grouped_dates          jsonb,
    anchor_date_time       timestamp,
    legacy_id              bigint,
    constraint pk_release_audit primary key (id, rev_id)
)
;

create index if not exists idx_release_audit_person on release_audit (person_identifier);
create index if not exists idx_release_audit_legacy_id on release_audit (legacy_id);

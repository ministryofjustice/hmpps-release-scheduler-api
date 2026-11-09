alter table person_summary
    drop column if exists cell_location,
    alter column prison_code set not null
;

create table if not exists release
(
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
    constraint pk_release primary key (id),
    constraint pk_release_person foreign key (person_identifier) references person_summary (person_identifier),
    constraint pk_release_status foreign key (status_code) references release_status (code),
    constraint pk_release_reason foreign key (reason_code) references release_reason (code),
    constraint uq_release_legacy_id unique (legacy_id)
)
;

create index if not exists idx_release_person_scheduled on release (person_identifier) where status_code in ('PENDING', 'UNVERIFIED', 'VERIFIED');
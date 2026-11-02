create table if not exists audit_revision
(
    id                bigserial   not null,
    timestamp         timestamp   not null,
    source            varchar(6)  not null,
    affected_entities text[]      not null,
    username          varchar(64) not null,
    caseload_id       varchar(10),
    reason            text,
    constraint pk_audit_revision primary key (id),
    constraint ch_audit_revision_source check (source in ('DPS', 'NOMIS'))
    )
;

create table if not exists hmpps_domain_event
(
    id         uuid    not null,
    version    int     not null,
    entity_id  uuid    not null,
    event_type text    not null,
    event      jsonb   not null,
    published  boolean not null,
    constraint pk_hmpps_domain_event primary key (id)
    )
;

create table if not exists hmpps_domain_event_audit
(
    rev_id     bigint   not null,
    rev_type   smallint not null,
    id         uuid     not null,
    version    int      not null,
    entity_id  uuid     not null,
    event_type text     not null,
    event      jsonb    not null,
    published  boolean  not null,
    constraint pk_hmpps_domain_event_audit primary key (id, rev_id),
    constraint fk_hmpps_domain_event_audit_revision foreign key (rev_id) references audit_revision (id)
    )
;

create index if not exists idx_hmpps_domain_event_audit_event_type_entity_id on hmpps_domain_event_audit (event_type, entity_id);

create table if not exists person_summary
(
    person_identifier varchar(10) not null,
    version           int         not null,
    first_name        varchar(64) not null,
    last_name         varchar(64) not null,
    prison_code       varchar(6),
    cell_location     varchar(64),
    constraint pk_person_summary primary key (person_identifier)
    )
;
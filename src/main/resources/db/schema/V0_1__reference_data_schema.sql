create table if not exists reference_data_domain
(
    code        varchar(32) not null,
    description text        not null,
    constraint pk_reference_data_domain primary key (code)
)
;

create table if not exists release_reason
(
    code            varchar(32) not null,
    description     text        not null,
    sequence_number int         not null,
    active          boolean     not null,
    constraint pk_release_reason primary key (code),
    constraint uq_release_reason_sequence_number unique (sequence_number)
)
;

create table if not exists release_status
(
    code            varchar(32) not null,
    description     text        not null,
    sequence_number int         not null,
    active          boolean     not null,
    constraint pk_release_status primary key (code),
    constraint uq_release_status_sequence_number unique (sequence_number)
)
;

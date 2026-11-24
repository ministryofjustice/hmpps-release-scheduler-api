alter table release
    drop column legacy_id
;

alter table release_audit
    drop column legacy_id
;

alter table release
    add column legacy_booking_id      bigint,
    add column legacy_sequence_number int
;

create unique index if not exists uq_release_legacy_booking_and_sequence on release(legacy_booking_id, legacy_sequence_number) where legacy_booking_id is not null and legacy_sequence_number is not null;
create unique index if not exists uq_release_legacy_booking_id_without_sequence on release(legacy_booking_id) where legacy_booking_id is not null and legacy_sequence_number is null;

alter table release_audit
    add column legacy_booking_id      bigint,
    add column legacy_sequence_number int
;

create index if not exists idx_release_audit_legacy_id_seq on release_audit(legacy_booking_id, legacy_sequence_number);
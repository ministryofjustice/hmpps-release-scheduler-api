insert into reference_data_domain(code, description)
values ('RELEASE_REASON', 'Release reason'),
       ('RELEASE_STATUS', 'Release status')
on conflict do nothing
;

with status as (
    (select *
     from (values ('PENDING', 'Awaiting details', 10, true),
                  ('SCHEDULED', 'Scheduled', 20, true),
                  ('VERIFIED', 'Verified', 30, true),
                  ('CANCELLED', 'Cancelled', 40, true),
                  ('EXPIRED', 'Expired', 50, true),
                  ('COMPLETED', 'Completed', 60, true))
              as t(code, description, sequence_number, active)))

insert
into release_status(code, description, sequence_number, active)
select code, description, sequence_number, active
from status
on conflict do nothing;

with reason as (
    (select *
     from (values ('AR', 'Auto release under 12 mth short termers', 10, true),
                  ('UAL', 'Abscond', 20, true),
                  ('MRG', 'Administrative release due to merge', 30, true),
                  ('AU', 'Appeal upheld on release', 40, true),
                  ('BD', 'Bailed from immigration detention post CRD', 50, true),
                  ('BL', 'Bailed', 60, true),
                  ('CR', 'Conditional release', 70, true),
                  ('CE', 'Conditional release extended sentence cj', 80, true),
                  ('HO', 'Detained under mental health act in hospital', 90, true),
                  ('D5', 'DTO 2 months late transfer', 100, true),
                  ('D2', 'DTO discharge 1 month early', 110, true),
                  ('D1', 'DTO discharge 2 months early', 120, true),
                  ('D3', 'DTO discharge mid term transfer', 130, true),
                  ('D4', 'DTO late transfer', 140, true),
                  ('D6', 'DTO re-release', 150, true),
                  ('DE', 'Deportation (No Sentence)', 160, true),
                  ('DL', 'Deportation licensees (Parole/Life/HMP)', 170, true),
                  ('DEC', 'Died', 180, true),
                  ('DD', 'Discharged end of sentence and deported', 190, true),
                  ('DA', 'Discharged on appeal from court', 200, true),
                  ('DS', 'Discontinuance / charges withdrawn', 210, true),
                  ('ERCG', 'Early release on compassionate grounds', 220, true),
                  ('DENUK', 'ERS deportation - direct to flight', 230, true),
                  ('DEIRC', 'ERS deportation - released to irc', 240, true),
                  ('ETR', 'ERS / TERS release', 250, true),
                  ('ESCP', 'Escape', 260, true),
                  ('ER', 'Executive release sec 32 cja 1982', 270, true),
                  ('EX', 'Extradition', 280, true),
                  ('HP', 'Final discharge to hospital-psychiatric', 290, true),
                  ('PX', 'Final parole inc YOI released on licence', 300, true),
                  ('HR', 'HDC conditional release', 310, true),
                  ('HD', 'HDC re-release conditional', 320, true),
                  ('HE', 'HDC re-release unconditional', 330, true),
                  ('HU', 'HDC unconditional release', 340, true),
                  ('HC', 'Invalid committal / habeas corpus', 350, true),
                  ('NCS', 'Non custodial sentence', 360, true),
                  ('NG', 'Not guilty', 370, true),
                  ('NP', 'Non parole release date', 380, true),
                  ('E', 'Post CRD IRC transfer', 390, true),
                  ('PF', 'Paid fine', 400, true),
                  ('PD', 'Post Recall Release', 410, true),
                  ('HQ', 'Released from mental health act in hospital', 420, true),
                  ('RE', 'Repatriated via prison transfer', 430, true),
                  ('RW', 'Royal warrant-free pardon', 440, true),
                  ('SC', 'Sentence completed / contempt purged', 450, true),
                  ('UAL_ECL', 'Abscond end of custody licence', 860, false),
                  ('AV', 'Accumulated visits', 870, false),
                  ('AS', 'Accumulated visits with stopover', 880, false),
                  ('DISC', 'Discharged', 890, false),
                  ('99', 'Discharge not for release', 900, false),
                  ('ECSLIRC', 'ECSL IRC transfer', 910, false),
                  ('ECSL', 'ECSL release', 920, false),
                  ('FR1', 'Friday/pre-bank holiday 1 day early rel', 930, false),
                  ('FR2', 'Friday/pre-bank holiday 2 day early rel', 940, false),
                  ('IF', 'Intermittent custody final release', 950, false),
                  ('MED', 'Medical', 960, false),
                  ('REL', 'Released', 970, false),
                  ('WEND', 'Weekenders', 980, false))
              as t(code, description, sequence_number, active)))

insert
into release_reason(code, description, sequence_number, active)
select code, description, sequence_number, active
from reason
on conflict do nothing;
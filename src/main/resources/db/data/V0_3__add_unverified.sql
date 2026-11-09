update release_status
set code        = 'UNVERIFIED',
    description = 'Unverified'
where code = 'SCHEDULED'
;

delete
from release_status
where code in ('CANCELLED', 'EXPIRED')
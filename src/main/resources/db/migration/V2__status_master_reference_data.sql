-- V2: reference data, not sample data.
--
-- Every booking and seat-booking row carries a foreign key into TBL_StatusMaster, so
-- these four rows are part of the schema contract and must exist in every environment
-- including production. Contrast db/seed, which holds demo cinemas and shows and is
-- loaded only under the 'local' profile.

INSERT INTO `TBL_StatusMaster` (`status_id`, `status`) VALUES (1,'SUCCESS'),(2,'FAILED'),(3,'INPROGRESS'),(4,'INITIATED');

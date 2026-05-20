-- =====================================================================
-- ArtConnect - Triggers
-- Group 8 / EFREI Databases 2 / TI603I
--
-- Four triggers covering the three families recommended by the
-- assignment: data consistency, capacity, and auditing.
-- =====================================================================

USE artconnect;

DROP TRIGGER IF EXISTS trg_workshop_date_future;
DROP TRIGGER IF EXISTS trg_booking_capacity_check;
DROP TRIGGER IF EXISTS trg_review_no_duplicate;
DROP TRIGGER IF EXISTS trg_artwork_status_audit;

DELIMITER //

-- ---------------------------------------------------------------------
-- Trigger 1: trg_workshop_date_future
-- Domain: data consistency.
-- Rejects inserting a workshop with a date in the past. Without this,
-- the booking flow would let users register for events that have
-- already happened.
-- ---------------------------------------------------------------------
CREATE TRIGGER trg_workshop_date_future
BEFORE INSERT ON workshop
FOR EACH ROW
BEGIN
    IF NEW.date < NOW() THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Workshop date must be in the future.';
    END IF;
END//

-- ---------------------------------------------------------------------
-- Trigger 2: trg_booking_capacity_check
-- Domain: capacity / business rule.
-- Refuses an INSERT into booking when the target workshop is already
-- full (counting non-cancelled bookings). PENDING bookings count
-- against capacity, since they are seats that may still be paid for.
-- ---------------------------------------------------------------------
CREATE TRIGGER trg_booking_capacity_check
BEFORE INSERT ON booking
FOR EACH ROW
BEGIN
    DECLARE current_bookings INT;
    DECLARE capacity INT;

    SELECT max_participants INTO capacity
    FROM workshop
    WHERE workshop_id = NEW.workshop_id;

    IF capacity IS NOT NULL THEN
        SELECT COUNT(*) INTO current_bookings
        FROM booking
        WHERE workshop_id = NEW.workshop_id
          AND payment_status <> 'CANCELLED';

        IF NEW.payment_status <> 'CANCELLED' AND current_bookings >= capacity THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Workshop is full; cannot accept this booking.';
        END IF;
    END IF;
END//

-- ---------------------------------------------------------------------
-- Trigger 3: trg_review_no_duplicate
-- Domain: data integrity.
-- Prevents the same community member from reviewing the same artwork
-- twice. A UNIQUE (member_id, artwork_id) constraint would also work,
-- but a trigger gives a clearer error message to the application.
-- ---------------------------------------------------------------------
CREATE TRIGGER trg_review_no_duplicate
BEFORE INSERT ON review
FOR EACH ROW
BEGIN
    DECLARE existing INT;

    SELECT COUNT(*) INTO existing
    FROM review
    WHERE member_id = NEW.member_id
      AND artwork_id = NEW.artwork_id;

    IF existing > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'This member has already reviewed this artwork.';
    END IF;
END//

-- ---------------------------------------------------------------------
-- Trigger 4: trg_artwork_status_audit
-- Domain: auditing.
-- Whenever an artwork's status changes (typically FOR_SALE -> SOLD or
-- FOR_SALE -> EXHIBITED) we write a row to audit_log so that
-- organizers can see who changed what and when.
-- ---------------------------------------------------------------------
CREATE TRIGGER trg_artwork_status_audit
AFTER UPDATE ON artwork
FOR EACH ROW
BEGIN
    IF OLD.status <> NEW.status THEN
        INSERT INTO audit_log (entity_type, entity_id, action, details)
        VALUES (
            'ARTWORK',
            NEW.artwork_id,
            'STATUS_CHANGE',
            CONCAT('"', NEW.title, '" status changed from ', OLD.status, ' to ', NEW.status)
        );
    END IF;
END//

DELIMITER ;

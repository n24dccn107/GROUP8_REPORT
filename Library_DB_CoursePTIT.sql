CREATE DATABASE IF NOT EXISTS library_management;

USE library_management;


-- 2. PERSON
CREATE TABLE person (
    person_id BIGINT AUTO_INCREMENT,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT pk_person PRIMARY KEY (person_id),
    CONSTRAINT uq_person_email UNIQUE (email)
) ENGINE = InnoDB;


-- 3. RESOURCE
CREATE TABLE resource (
    resource_id BIGINT AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    publish_date DATE,

    CONSTRAINT pk_resource PRIMARY KEY (resource_id)
) ENGINE = InnoDB;


-- 4. LICENSE
CREATE TABLE license (
    license_id BIGINT AUTO_INCREMENT,
    license_type VARCHAR(100) NOT NULL,
    description TEXT,

    CONSTRAINT pk_license PRIMARY KEY (license_id)
) ENGINE = InnoDB;


-- 5. MEMBER
CREATE TABLE member (
    member_id BIGINT,
    member_type VARCHAR(50),
    max_loan_limit INT NOT NULL DEFAULT 5,

    CONSTRAINT pk_member PRIMARY KEY (member_id),

    CONSTRAINT fk_member_person
        FOREIGN KEY (member_id)
        REFERENCES person(person_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT chk_member_max_loan
        CHECK (max_loan_limit >= 0)
) ENGINE = InnoDB;


-- 6. AUTHOR
CREATE TABLE author (
    author_id BIGINT,
    affiliation VARCHAR(255),

    CONSTRAINT pk_author PRIMARY KEY (author_id),

    CONSTRAINT fk_author_person
        FOREIGN KEY (author_id)
        REFERENCES person(person_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE = InnoDB;


-- 7. LIBRARIAN
CREATE TABLE librarian (
    librarian_id BIGINT,
    hire_date DATE,

    CONSTRAINT pk_librarian PRIMARY KEY (librarian_id),

    CONSTRAINT fk_librarian_person
        FOREIGN KEY (librarian_id)
        REFERENCES person(person_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE = InnoDB;


-- 8. PHYSICAL_BOOK
CREATE TABLE physical_book (
    resource_id BIGINT,
    isbn VARCHAR(20) NOT NULL,
    genre VARCHAR(100),

    CONSTRAINT pk_physical_book PRIMARY KEY (resource_id),
    CONSTRAINT uq_physical_book_isbn UNIQUE (isbn),

    CONSTRAINT fk_physical_book_resource
        FOREIGN KEY (resource_id)
        REFERENCES resource(resource_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE = InnoDB;


-- 9. JOURNAL
CREATE TABLE journal (
    resource_id BIGINT,
    subject_area VARCHAR(150),

    CONSTRAINT pk_journal PRIMARY KEY (resource_id),

    CONSTRAINT fk_journal_resource
        FOREIGN KEY (resource_id)
        REFERENCES resource(resource_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE = InnoDB;


-- 10. OPEN_ACCESS
CREATE TABLE open_access (
    resource_id BIGINT,
    doi VARCHAR(255),

    CONSTRAINT pk_open_access PRIMARY KEY (resource_id),

    CONSTRAINT fk_open_access_resource
        FOREIGN KEY (resource_id)
        REFERENCES resource(resource_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE = InnoDB;


-- 11. BOOK_COPY
CREATE TABLE book_copy (
    copy_id BIGINT AUTO_INCREMENT,
    resource_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
    shelf_location VARCHAR(100),

    CONSTRAINT pk_book_copy PRIMARY KEY (copy_id),

    CONSTRAINT fk_book_copy_physical_book
        FOREIGN KEY (resource_id)
        REFERENCES physical_book(resource_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT chk_book_copy_status
        CHECK (status IN ('AVAILABLE', 'BORROWED', 'DAMAGED'))
) ENGINE = InnoDB;


-- 12. LOAN_RECORD
CREATE TABLE loan_record (
    loan_id BIGINT AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    librarian_id BIGINT NOT NULL,
    loan_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    due_date DATE NOT NULL,

    CONSTRAINT pk_loan_record PRIMARY KEY (loan_id),

    CONSTRAINT fk_loan_record_member
        FOREIGN KEY (member_id)
        REFERENCES member(member_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_loan_record_librarian
        FOREIGN KEY (librarian_id)
        REFERENCES librarian(librarian_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_loan_due_date
        CHECK (due_date >= loan_date)
) ENGINE = InnoDB;


-- 13. LOANED_COPY
CREATE TABLE loaned_copy (
    loaned_copy_id BIGINT AUTO_INCREMENT,
    loan_id BIGINT NOT NULL,
    copy_id BIGINT NOT NULL,
    return_date DATE,

    CONSTRAINT pk_loaned_copy PRIMARY KEY (loaned_copy_id),

    CONSTRAINT fk_loaned_copy_loan_record
        FOREIGN KEY (loan_id)
        REFERENCES loan_record(loan_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_loaned_copy_book_copy
        FOREIGN KEY (copy_id)
        REFERENCES book_copy(copy_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE = InnoDB;


-- 14. FINE
CREATE TABLE fine (
    fine_id BIGINT AUTO_INCREMENT,
    loaned_copy_id BIGINT NOT NULL,
    librarian_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    paid_status VARCHAR(20) NOT NULL DEFAULT 'Unpaid',

    CONSTRAINT pk_fine PRIMARY KEY (fine_id),

    CONSTRAINT uq_fine_loaned_copy UNIQUE (loaned_copy_id),

    CONSTRAINT fk_fine_loaned_copy
        FOREIGN KEY (loaned_copy_id)
        REFERENCES loaned_copy(loaned_copy_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_fine_librarian
        FOREIGN KEY (librarian_id)
        REFERENCES librarian(librarian_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_fine_amount
        CHECK (amount >= 0)
) ENGINE = InnoDB;


-- 15. ACCESS_LICENSE
CREATE TABLE access_license (
    license_id BIGINT NOT NULL,
    resource_id BIGINT NOT NULL,
    granted_date DATE NOT NULL,

    CONSTRAINT pk_access_license
        PRIMARY KEY (license_id, resource_id),

    CONSTRAINT fk_access_license_license
        FOREIGN KEY (license_id)
        REFERENCES license(license_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
CONSTRAINT fk_access_license_open_access
        FOREIGN KEY (resource_id)
        REFERENCES open_access(resource_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE = InnoDB;


-- 16. ACCESS_LOG
CREATE TABLE access_log (
    access_id BIGINT AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    resource_id BIGINT NOT NULL,
    access_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_access_log PRIMARY KEY (access_id),

    CONSTRAINT fk_access_log_member
        FOREIGN KEY (member_id)
        REFERENCES member(member_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_access_log_journal
        FOREIGN KEY (resource_id)
        REFERENCES journal(resource_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE = InnoDB;


-- 17. RESOURCE_AUTHOR
CREATE TABLE resource_author (
    author_id BIGINT NOT NULL,
    resource_id BIGINT NOT NULL,

    CONSTRAINT pk_resource_author
        PRIMARY KEY (author_id, resource_id),

    CONSTRAINT fk_resource_author_author
        FOREIGN KEY (author_id)
        REFERENCES author(author_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_resource_author_resource
        FOREIGN KEY (resource_id)
        REFERENCES resource(resource_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE = InnoDB;


-- 18. ADDITIONAL CONSTRAINTS

-- 18.1. FINE PAYMENT STATUS
ALTER TABLE fine
ADD CONSTRAINT chk_fine_paid_status
CHECK (paid_status IN ('Paid', 'Unpaid'));


-- 18.2. CHECK RETURN DATE
DELIMITER $$

CREATE TRIGGER trg_check_return_date_insert
BEFORE INSERT ON loaned_copy
FOR EACH ROW
BEGIN
    DECLARE v_loan_date DATE;

    SELECT loan_date
    INTO v_loan_date
    FROM loan_record
    WHERE loan_id = NEW.loan_id;

    IF NEW.return_date IS NOT NULL
       AND NEW.return_date < v_loan_date THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Return date cannot be earlier than loan date';
    END IF;
END$$


CREATE TRIGGER trg_check_return_date_update
BEFORE UPDATE ON loaned_copy
FOR EACH ROW
BEGIN
    DECLARE v_loan_date DATE;

    SELECT loan_date
    INTO v_loan_date
    FROM loan_record
    WHERE loan_id = NEW.loan_id;

    IF NEW.return_date IS NOT NULL
       AND NEW.return_date < v_loan_date THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Return date cannot be earlier than loan date';
    END IF;
END$$


-- 18.3. CHECK RESOURCE TYPE

-- INSERT PHYSICAL_BOOK
CREATE TRIGGER trg_check_physical_book_insert
BEFORE INSERT ON physical_book
FOR EACH ROW
BEGIN
    IF EXISTS (
        SELECT 1 FROM journal
        WHERE resource_id = NEW.resource_id
    ) OR EXISTS (
        SELECT 1 FROM open_access
        WHERE resource_id = NEW.resource_id
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Resource already belongs to another type';
    END IF;
END$$


-- INSERT JOURNAL
CREATE TRIGGER trg_check_journal_insert
BEFORE INSERT ON journal
FOR EACH ROW
BEGIN
    IF EXISTS (
SELECT 1 FROM physical_book
        WHERE resource_id = NEW.resource_id
    ) OR EXISTS (
        SELECT 1 FROM open_access
        WHERE resource_id = NEW.resource_id
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Resource already belongs to another type';
    END IF;
END$$


-- INSERT OPEN_ACCESS
CREATE TRIGGER trg_check_open_access_insert
BEFORE INSERT ON open_access
FOR EACH ROW
BEGIN
    IF EXISTS (
        SELECT 1 FROM physical_book
        WHERE resource_id = NEW.resource_id
    ) OR EXISTS (
        SELECT 1 FROM journal
        WHERE resource_id = NEW.resource_id
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Resource already belongs to another type';
    END IF;
END$$


-- UPDATE PHYSICAL_BOOK
CREATE TRIGGER trg_check_physical_book_update
BEFORE UPDATE ON physical_book
FOR EACH ROW
BEGIN
    IF EXISTS (
        SELECT 1 FROM journal
        WHERE resource_id = NEW.resource_id
    ) OR EXISTS (
        SELECT 1 FROM open_access
        WHERE resource_id = NEW.resource_id
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Resource already belongs to another type';
    END IF;
END$$


-- UPDATE JOURNAL
CREATE TRIGGER trg_check_journal_update
BEFORE UPDATE ON journal
FOR EACH ROW
BEGIN
    IF EXISTS (
        SELECT 1 FROM physical_book
        WHERE resource_id = NEW.resource_id
    ) OR EXISTS (
        SELECT 1 FROM open_access
        WHERE resource_id = NEW.resource_id
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Resource already belongs to another type';
    END IF;
END$$


-- UPDATE OPEN_ACCESS
CREATE TRIGGER trg_check_open_access_update
BEFORE UPDATE ON open_access
FOR EACH ROW
BEGIN
    IF EXISTS (
        SELECT 1 FROM physical_book
        WHERE resource_id = NEW.resource_id
    ) OR EXISTS (
        SELECT 1 FROM journal
        WHERE resource_id = NEW.resource_id
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Resource already belongs to another type';
    END IF;
END$$

DELIMITER ;
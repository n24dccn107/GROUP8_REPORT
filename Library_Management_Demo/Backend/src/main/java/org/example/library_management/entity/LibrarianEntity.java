package org.example.library_management.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "Librarian")
public class LibrarianEntity {
    @Id
    @Column(name = "person_id")
    private Long personId;

    @Column(name = "librarian_id", nullable = false, length = 10)
    private String librarianId;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(name = "Leave_date")
    private LocalDate leaveDate;

    @Column(name = "shift", length = 20)
    private String shift;

    protected LibrarianEntity() {
    }

    public Long getPersonId() {
        return personId;
    }

    public String getLibrarianId() {
        return librarianId;
    }

    public LocalDate getLeaveDate() {
        return leaveDate;
    }
}

package org.example.library_management.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "Member")
public class MemberEntity {
    @Id
    @Column(name = "person_id")
    private Long personId;

    @Column(name = "member_id", nullable = false, length = 10)
    private String memberId;

    @Column(name = "join_date", nullable = false)
    private LocalDate joinDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    protected MemberEntity() {
    }

    public Long getPersonId() {
        return personId;
    }

    public String getMemberId() {
        return memberId;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }
}

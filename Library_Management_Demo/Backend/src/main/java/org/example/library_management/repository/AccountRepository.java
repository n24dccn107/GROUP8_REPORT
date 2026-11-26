package org.example.library_management.repository;

import org.example.library_management.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<AccountEntity, Long> {
    Optional<AccountEntity> findByPersonIdAndRole(long personId, String role);

    @Modifying
    @Transactional
    @Query("UPDATE AccountEntity a SET a.status = 'Disabled' WHERE a.accountId = :accountId")
    int disableAccount(@Param("accountId") long accountId);

    @Modifying
    @Transactional
    @Query("""
            UPDATE AccountEntity a SET a.status = 'Disabled'
            WHERE a.role = 'Member' AND a.status <> 'Disabled'
              AND EXISTS (SELECT m.personId FROM MemberEntity m
                          WHERE m.personId = a.personId AND m.expiryDate < CURRENT_DATE)
            """)
    int disableExpiredMemberAccounts();

    @Modifying
    @Transactional
    @Query("UPDATE AccountEntity a SET a.status = 'Disabled' WHERE a.personId = :personId AND a.role = 'Member'")
    int disableMemberAccount(@Param("personId") long personId);

    @Modifying
    @Transactional
    @Query("UPDATE AccountEntity a SET a.lastLogin = :loginTime WHERE a.accountId = :accountId")
    int updateLastLogin(@Param("accountId") long accountId, @Param("loginTime") LocalDateTime loginTime);
}

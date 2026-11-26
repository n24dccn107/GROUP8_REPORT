package org.example.library_management.repository;

import org.example.library_management.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
    Optional<MemberEntity> findByMemberId(String memberId);
    Optional<MemberEntity> findByPersonId(long personId);
}

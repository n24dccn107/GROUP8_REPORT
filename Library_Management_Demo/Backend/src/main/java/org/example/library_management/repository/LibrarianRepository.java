package org.example.library_management.repository;

import org.example.library_management.entity.LibrarianEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LibrarianRepository extends JpaRepository<LibrarianEntity, Long> {
    Optional<LibrarianEntity> findByLibrarianId(String librarianId);
    Optional<LibrarianEntity> findByPersonId(long personId);
}

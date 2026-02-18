package com.backend.situ.repository;

import com.backend.situ.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Page<User> findByCompanyIdOrderByLastNameAscFirstNameAsc(Long companyId, Pageable pageable);

    Boolean existsByDni(Integer dni);

    boolean existsByDniAndCompanyId(Integer dni, Long companyId);

    Optional<User> findByIdAndCompanyId(Long id, Long companyId);
}

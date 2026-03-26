package com.expense.management.repository;

import com.expense.management.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.role.name = :roleName AND u.isActive = true")
    List<User> findActiveUsersByRole(@Param("roleName") String roleName);

    @Query("SELECT u FROM User u WHERE u.department.id = :departmentId AND u.isActive = true")
    Page<User> findByDepartmentId(@Param("departmentId") Long departmentId, Pageable pageable);
}

package com.amazon.ecommerce.repository;

import com.amazon.ecommerce.entity.Role;
import com.amazon.ecommerce.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleType name);
}

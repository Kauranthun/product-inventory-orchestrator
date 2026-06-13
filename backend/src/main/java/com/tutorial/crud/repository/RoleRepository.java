package com.tutorial.crud.repository;

import com.tutorial.crud.entity.UserRole;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoleRepository extends CrudRepository<UserRole, Long> {
    Optional<UserRole> findByName(String name);
}
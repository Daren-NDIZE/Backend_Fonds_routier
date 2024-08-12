package com.api.backend.Fonds_routier.repository;

import com.api.backend.Fonds_routier.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {

    Role findByRoleName(String RoleName);

}

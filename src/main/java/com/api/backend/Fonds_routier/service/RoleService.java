package com.api.backend.Fonds_routier.service;

import com.api.backend.Fonds_routier.model.Role;
import com.api.backend.Fonds_routier.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {

    @Autowired
    RoleRepository roleRepository;


    public void saveRole(Role role){

        roleRepository.save(role);
    }

    public List<Role> getAllRole(){

        return roleRepository.findAll();
    }

    public void deleteRole(long id){

        roleRepository.deleteById(id);
    }

    public Role findRole(long id){

        return roleRepository.findById(id).orElse(null);
    }

    public Role getRoleByRoleName(String roleName){

        return roleRepository.findByRoleName(roleName);
    }


}

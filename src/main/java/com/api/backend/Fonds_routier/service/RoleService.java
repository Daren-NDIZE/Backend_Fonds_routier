package com.api.backend.Fonds_routier.service;

import com.api.backend.Fonds_routier.model.Permission;
import com.api.backend.Fonds_routier.model.Role;
import com.api.backend.Fonds_routier.repository.PermissionRepository;
import com.api.backend.Fonds_routier.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoleService {

    @Autowired
    RoleRepository roleRepository;
    @Autowired
    PermissionRepository permissionRepository;


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

    public void initPermission (){

        List <Permission> list=new ArrayList<>();

        list.add(new Permission(0,"VALIDATION DES PROGRAMMES",null));
        list.add(new Permission(0,"GESTION DES PROGRAMMES REPORTS",null));
        list.add(new Permission(0,"CLOTURE DES PROGRAMMES",null));
        list.add(new Permission(0,"GESTION DE LA PROVISION DE RESERVE",null));

        list.add(new Permission(0,"TRAITEMENT DCO/DAF",null));
        list.add(new Permission(0,"ENGAGER LES PROJETS",null));
        list.add(new Permission(0,"REJET/RENVOIE DES PROJETS",null));
        list.add(new Permission(0,"VISA DES PROJETS",null));

        list.add(new Permission(0,"SAISIE DES PAIEMENTS",null));

        list.add(new Permission(0,"GESTION DE LA PROGRAMMATION",null));
        list.add(new Permission(0,"SUIVI DE L'EXECUTION DES TRAVAUX",null));
        list.add(new Permission(0,"SUIVI DE LA PASSATION",null));

        permissionRepository.saveAll(list);

    }

    public Permission findPermission(long id){

        return permissionRepository.findById(id).orElse(null);
    }

    public List<Permission> getAllPermission(){

        return permissionRepository.findAll();
    }

    public boolean containsPermission (List<Permission> permissions, String d ){

        boolean decision=false;

        for(Permission p : permissions){

            if(p.getDescription().equals(d)){

                return true;
            }
        }

        return decision;
    }

}

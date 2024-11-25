package com.api.backend.Fonds_routier.repository;

import com.api.backend.Fonds_routier.enums.UserRole;
import com.api.backend.Fonds_routier.model.Role;
import com.api.backend.Fonds_routier.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur,Long> {

    Utilisateur findByUsername(String username);

    Utilisateur findByRole(Role role);

    List<Utilisateur> findByAdministration(String administration);

    List<Utilisateur> findByIdIsNot(long id);

}

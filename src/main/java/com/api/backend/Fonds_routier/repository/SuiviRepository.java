package com.api.backend.Fonds_routier.repository;

import com.api.backend.Fonds_routier.model.Projet;
import com.api.backend.Fonds_routier.model.Suivi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuiviRepository extends JpaRepository<Suivi,Long> {

    Suivi findByProjet(Projet projet);
}

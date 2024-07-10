package com.api.backend.Fonds_routier.repository;

import com.api.backend.Fonds_routier.model.SuiviTravaux;
import com.api.backend.Fonds_routier.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuiviTravauxRepository extends JpaRepository<SuiviTravaux,Long> {

}

package com.api.backend.Fonds_routier.repository;

import com.api.backend.Fonds_routier.enums.Ordonnateur;
import com.api.backend.Fonds_routier.enums.ProgrammeStatut;
import com.api.backend.Fonds_routier.enums.ProgrammeType;
import com.api.backend.Fonds_routier.model.Programme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgrammeRepository extends JpaRepository<Programme,Long> {

    @Query("FROM Programme WHERE   ordonnateur = ?1 AND statut != 'CLOTURER'")
    List <Programme> findAllByOrdonnateur(Ordonnateur ordonnateur);

    List<Programme> findByStatut(ProgrammeStatut status);

    List<Programme> findAllByStatutIn(List<ProgrammeStatut> status);

    List<Programme> findAllByOrdonnateurAndStatutIn(Ordonnateur ordonnateur,List<ProgrammeStatut> status);

    @Query("FROM Programme WHERE statut IN ('VALIDER','CLOTURER') AND ordonnateur= ?1 AND type= ?2 AND annee= ?3 ORDER BY id DESC LIMIT 1")
    Programme findSyntheseProgramme(Ordonnateur ordonnateur, ProgrammeType type, int annee);

}

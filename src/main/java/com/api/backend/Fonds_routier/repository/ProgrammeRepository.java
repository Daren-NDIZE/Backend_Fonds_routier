package com.api.backend.Fonds_routier.repository;

import com.api.backend.Fonds_routier.enums.ProgrammeStatut;
import com.api.backend.Fonds_routier.model.Programme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgrammeRepository extends JpaRepository<Programme,Long> {

    @Query("FROM Programme WHERE   ordonnateur = ?1 AND statut != 'CLOTURE'")
    List <Programme> findAllByOrdonnateur(String ordonnateur);

    List<Programme> findByStatut(ProgrammeStatut status);

    List<Programme> findAllByStatutIn(List<ProgrammeStatut> status);


}

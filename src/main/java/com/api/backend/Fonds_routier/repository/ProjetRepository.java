package com.api.backend.Fonds_routier.repository;


import com.api.backend.Fonds_routier.model.Projet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ProjetRepository extends JpaRepository<Projet,Long> {


}

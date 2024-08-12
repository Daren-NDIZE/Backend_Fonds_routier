package com.api.backend.Fonds_routier.repository;

import com.api.backend.Fonds_routier.model.Action;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ActionRepository extends JpaRepository<Action,Long> {

    List<Action> findByDateBetween(Date firstDate, Date secondDate);
}

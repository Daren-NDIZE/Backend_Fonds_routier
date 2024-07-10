package com.api.backend.Fonds_routier.service;

import com.api.backend.Fonds_routier.model.Payement;
import com.api.backend.Fonds_routier.model.Projet;
import com.api.backend.Fonds_routier.repository.PayementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class PayementService {

    @Autowired
    PayementRepository payementRepository;

    public void savePayement(Projet projet, Payement payement){

        payement.setProjet(projet);
        payement.setDate(new Date());
        payement.setM_TVA(0.1925*payement.getM_HTVA());
        payement.setM_TTC((payement.getM_HTVA())*1.1925);
        payement.setNap(payement.getM_HTVA()*(1- payement.getAir()/100));

        payementRepository.save(payement);
    }

}

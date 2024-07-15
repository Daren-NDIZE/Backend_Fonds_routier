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
        payement.setM_TTC((payement.getM_HTVA()*0.1925 + payement.getM_HTVA()));
        payement.setNap(payement.getM_HTVA()*(1- payement.getAir()/100));
        payement.setM_AIR(payement.getAir()*payement.getM_HTVA()/100);

        payementRepository.save(payement);
    }

    public Payement findPayement(long id){

        return payementRepository.findById(id).orElse(null);
    }

    public void updatePayement(Payement payement, Payement update){

        payement.setDecompte(update.getDecompte());
        payement.setObservation(update.getObservation());
        payement.setM_HTVA(update.getM_HTVA());
        payement.setAir(update.getAir());
        payement.setM_TVA(0.1925*update.getM_HTVA());
        payement.setM_TTC(update.getM_HTVA()*0.1925+update.getM_HTVA());
        payement.setNap(update.getM_HTVA()*(1- update.getAir()/100));
        payement.setM_AIR(update.getAir()*update.getM_HTVA()/100);
        payementRepository.save(payement);
    }

    public void deletePayement(long id){

        payementRepository.deleteById(id);
    }
}

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

        payement.setM_TVA(Math.round(19.25*payement.getM_HTVA())/100.0);
        payement.setM_TTC(Math.round(payement.getM_HTVA()*119.25)/100.0 );
        payement.setNap( Math.round( payement.getM_HTVA()*(100- payement.getAir()) )/100.0 );
        payement.setM_AIR(Math.round(payement.getAir()*payement.getM_HTVA())/100.0);

        payementRepository.save(payement);
    }

    public Payement findPayement(long id){

        return payementRepository.findById(id).orElse(null);
    }

    public void updatePayement(Payement payement, Payement update){

        payement.setDecompte(update.getDecompte());
        payement.setN_marche(update.getN_marche());
        payement.setObservation(update.getObservation());
        payement.setM_HTVA(update.getM_HTVA());
        payement.setAir(update.getAir());

        payement.setM_TVA(Math.round(19.25*update.getM_HTVA())/100.0);
        payement.setM_TTC(Math.round(update.getM_HTVA()*119.25)/100.0 );
        payement.setNap( Math.round( update.getM_HTVA()*(100- update.getAir()) )/100.0 );
        payement.setM_AIR(Math.round(update.getAir()*update.getM_HTVA())/100.0);

        payementRepository.save(payement);
    }

    public void deletePayement(long id){

        payementRepository.deleteById(id);
    }
}

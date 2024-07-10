package com.api.backend.Fonds_routier.service;

import com.api.backend.Fonds_routier.DTO.SuiviDTO;
import com.api.backend.Fonds_routier.model.Programme;
import com.api.backend.Fonds_routier.model.Projet;
import com.api.backend.Fonds_routier.model.Suivi;
import com.api.backend.Fonds_routier.model.SuiviTravaux;
import com.api.backend.Fonds_routier.repository.ProjetRepository;
import com.api.backend.Fonds_routier.repository.SuiviRepository;
import com.api.backend.Fonds_routier.repository.SuiviTravauxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;


@Service
public class ProjetService {

    @Autowired
    ProjetRepository projetRepository;
    @Autowired
    SuiviRepository suiviRepository;

    @Autowired
    SuiviTravauxRepository suiviTravauxRepository;

    public void saveProjet(Programme programme,Projet projet){

        projet.setProgramme(programme);
        projet.setFinancement("NORMAL");
        projetRepository.save(projet);
    }

    public void updateProjet(Projet projet, Projet update){

        update.setId(projet.getId());
        update.setProgramme(projet.getProgramme());
        update.setFinancement(projet.getFinancement());
        projetRepository.save(update);
    }

    public Projet findProjet(long id){

        return projetRepository.findById(id).orElse(null);
    }

    public void deleteProjet(long id){

        projetRepository.deleteById(id);
    }

    public void setSuivi(Projet projet, SuiviDTO suiviDTO) throws CloneNotSupportedException {

        Suivi suivi=new Suivi();
        suivi.setStatut(suiviDTO.getStatut());
        suivi.setMotif(suiviDTO.getMotif());

        if(suiviDTO.getPrestataire()!=null){
            projet.setPrestataire(suiviDTO.getPrestataire());
        }

        if(projet.getBudget_n() < suiviDTO.getEngagement()){

            suivi.setEngagement(projet.getBudget_n());
            Projet newProjet = (Projet) projet.clone();
            newProjet.setId(0);
            newProjet.setFinancement("RESERVE");
            newProjet.setPayement(null);
            newProjet.setSuivi(null);
            newProjet.setBudget_n((suiviDTO.getEngagement() - projet.getBudget_n()));
            projetRepository.save(newProjet);
            Suivi newSuivi=new Suivi(0,newProjet.getBudget_n(),suiviDTO.getStatut(),null,newProjet);
            newProjet.setSuivi(newSuivi);
            suiviRepository.save(newSuivi);

        }else {
            suivi.setEngagement(suiviDTO.getEngagement());
        }

        Suivi s=suiviRepository.findByProjet(projet);

        if(s!=null){
            suivi.setId(s.getId());
            if(suivi.getStatut().equals("Visé")){
                suivi.setEngagement(s.getEngagement());
            }
        }
        projet.setSuivi(suivi);
        suiviRepository.save(suivi);
    }

    public void saveSuiviTravaux(Projet projet, SuiviTravaux suiviTravaux){

        suiviTravaux.setProjet(projet);
        suiviTravaux.setDate(new Date());

        suiviTravauxRepository.save(suiviTravaux);
    }
}

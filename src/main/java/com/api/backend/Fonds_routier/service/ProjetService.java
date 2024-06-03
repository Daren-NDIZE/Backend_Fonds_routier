package com.api.backend.Fonds_routier.service;

import com.api.backend.Fonds_routier.DTO.SuiviDTO;
import com.api.backend.Fonds_routier.model.Programme;
import com.api.backend.Fonds_routier.model.Projet;
import com.api.backend.Fonds_routier.model.Suivi;
import com.api.backend.Fonds_routier.repository.ProjetRepository;
import com.api.backend.Fonds_routier.repository.SuiviRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjetService {

    @Autowired
    ProjetRepository projetRepository;
    @Autowired
    SuiviRepository suiviRepository;

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
            newProjet.setSuivi(null);
            newProjet.setFinancement("RESERVE");
            newProjet.setBudget_n((suiviDTO.getEngagement() - projet.getBudget_n()));
            newProjet.setSuivi(null);
            projetRepository.save(newProjet);
        }else {
            suivi.setEngagement(suiviDTO.getEngagement());
        }

        Suivi s=suiviRepository.findByProjet(projet);
        projet.setSuivi(suivi);
        if(s!=null){
            suivi.setId(s.getId());
        }
        suiviRepository.save(suivi);
    }
}

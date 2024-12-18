package com.api.backend.Fonds_routier.service;

import com.api.backend.Fonds_routier.DTO.SuiviDTO;
import com.api.backend.Fonds_routier.model.*;
import com.api.backend.Fonds_routier.repository.PassationRepository;
import com.api.backend.Fonds_routier.repository.ProjetRepository;
import com.api.backend.Fonds_routier.repository.SuiviRepository;
import com.api.backend.Fonds_routier.repository.SuiviTravauxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;


@Service
public class ProjetService {

    @Autowired
    ProjetRepository projetRepository;
    @Autowired
    SuiviRepository suiviRepository;
    @Autowired
    SuiviTravauxRepository suiviTravauxRepository;
    @Autowired
    PassationRepository passationRepository;

    public void saveProjet(Programme programme,Projet projet){

        projet.setProgramme(programme);
        projet.setFinancement("NORMAL");
        projetRepository.save(projet);
    }

    public void saveProvisionProjet(Programme programme,Projet projet){

        projet.setProgramme(programme);
        projet.setFinancement("RESERVE");
        projetRepository.save(projet);
        Suivi newSuivi=new Suivi(0,projet.getBudget_n(),"Transmis pour visa",null,projet);
        projet.setSuivi(newSuivi);
        suiviRepository.save(newSuivi);

    }

    public void updateProjet(Projet projet, Projet update){

        update.setId(projet.getId());
        update.setProgramme(projet.getProgramme());
        update.setFinancement(projet.getFinancement());
        update.setSuivi(projet.getSuivi());
        update.setSuiviTravaux(projet.getSuiviTravaux());
        update.setPayement(projet.getPayement());
        projetRepository.save(update);
    }

    public Projet findProjet(long id){

        return projetRepository.findById(id).orElse(null);
    }

    public void deleteProjet(long id){

        projetRepository.deleteById(id);
    }

    public void setSuivi(Projet projet, SuiviDTO suiviDTO) throws CloneNotSupportedException {

        LocalDateTime now=LocalDateTime.now();

        String date=now.format(DateTimeFormatter.ofPattern("dd/MM/YYYY"));

        Suivi suivi=new Suivi();
        suivi.setStatut(suiviDTO.getStatut()+" le "+date);
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
            newProjet.setSuiviTravaux(null);
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
            if(suiviDTO.getStatut().equals("Visé")){
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

    public void savePassation(Projet projet, Passation passation){

        passation.setProjet(projet);
        passation.setDate(new Date());
        passationRepository.save(passation);

    }

    public SuiviTravaux findSuiviTravaux(long id){

        return suiviTravauxRepository.findById(id).orElse(null);
    }

    public void updateSuiviTravaux(SuiviTravaux suiviTravaux ,SuiviTravaux update){

        suiviTravaux.setDescription(update.getDescription());
        suiviTravaux.setTauxAvancement(update.getTauxAvancement());
        suiviTravaux.setTauxConsommation(update.getTauxConsommation());
        suiviTravaux.setProposition(update.getProposition());

        suiviTravauxRepository.save(suiviTravaux);
    }

    public void deleteSuiviTravaux(long id){

        suiviTravauxRepository.deleteById(id);
    }

    public Passation findPassation(long id){

        return passationRepository.findById(id).orElse(null);
    }

    public void updatePassation(Passation passation ,Passation update){

        passation.setContractualisation(update.getContractualisation());
        passation.setNumeroMarche(update.getNumeroMarche());
        passation.setDateOs(update.getDateOs());

        passationRepository.save(passation);
    }

    public void deletePassation(long id){

        passationRepository.deleteById(id);
    }
}

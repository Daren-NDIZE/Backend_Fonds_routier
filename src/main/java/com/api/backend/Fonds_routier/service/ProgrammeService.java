package com.api.backend.Fonds_routier.service;

import com.api.backend.Fonds_routier.enums.ProgrammeStatut;
import com.api.backend.Fonds_routier.enums.ProgrammeType;
import com.api.backend.Fonds_routier.model.Programme;
import com.api.backend.Fonds_routier.repository.ProgrammeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProgrammeService {

    @Autowired
    ProgrammeRepository programmeRepository;


    public void saveProgramme(Programme programme){

        programmeRepository.save(programme);
    }

    public List<Programme> getProgramme(){

        return programmeRepository.findAll();
    }

    public List<Programme> getProgrammeByOrdonnateur(String ordonnateur){

        return programmeRepository.findAllByOrdonnateur(ordonnateur);
    }


    public void deleteProgramme( long id){

        programmeRepository.deleteById(id);
    }

    public Programme findProgramme(long id){

        return programmeRepository.findById(id).orElse(null);
    }


    public void updateProgramme(Programme programme,Programme update){

        programme.setBudget(update.getBudget());
        programme.setType(update.getType());

        if(update.getType()== ProgrammeType.BASE){
            programme.setIntitule("Programme "+programme.getOrdonnateur()+" "+programme.getAnnee());
        }else {
            programme.setIntitule("Programme "+update.getType().toString().toLowerCase()+" "+programme.getOrdonnateur()+" "+programme.getAnnee());
        }

        programmeRepository.save(programme);
    }

    public void submitProgramme(Programme programme){

        programme.setStatut(ProgrammeStatut.SOUMIS);
        programmeRepository.save(programme);
    }

    public List<Programme> getProgrammeByStatut(ProgrammeStatut statut){

        return programmeRepository.findByStatut(statut);
    }

    public List<Programme> getProgrammeByStatuts(List<ProgrammeStatut> statuts){

        return programmeRepository.findAllByStatutIn(statuts);

    }

}


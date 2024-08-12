package com.api.backend.Fonds_routier.service;

import com.api.backend.Fonds_routier.DTO.ProgrammeFilterDTO;
import com.api.backend.Fonds_routier.DTO.SyntheseDTO;
import com.api.backend.Fonds_routier.enums.Ordonnateur;
import com.api.backend.Fonds_routier.enums.ProgrammeStatut;
import com.api.backend.Fonds_routier.enums.ProgrammeType;
import com.api.backend.Fonds_routier.enums.Region;
import com.api.backend.Fonds_routier.model.*;
import com.api.backend.Fonds_routier.repository.*;
import jakarta.validation.ConstraintViolationException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProgrammeService {

    @Autowired
    ProgrammeRepository programmeRepository;
    @Autowired
    ProjetRepository projetRepository;
    @Autowired
    SuiviTravauxRepository suiviTravauxRepository;
    @Autowired
    PayementRepository payementRepository;
    @Autowired
    SuiviRepository suiviRepository;


    public void saveProgramme(Programme programme){

        programmeRepository.save(programme);
    }

    public List<Programme> getProgramme(){

        return programmeRepository.findAll();
    }

    public List<Programme> getProgrammeByOrdonnateur(Ordonnateur ordonnateur){

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

    public List<Programme> getProgrammeByOrdonnateurAndYear(Ordonnateur ordonnateur, int annee){

        return programmeRepository.findByOrdonnateurAndAnnee(ordonnateur, annee);
    }

    public List<Programme> getProgrammeByOrdonnateurAndStatut(Ordonnateur ordonnateur,List<ProgrammeStatut> status){

        return programmeRepository.findAllByOrdonnateurAndStatutIn(ordonnateur, status);
    }

    public List<Programme> getSyntheseProgramme(ProgrammeFilterDTO filter)  {

        List<Programme> list = new ArrayList<>();

        List verification =List.of("MINTP","MINT","MINHDU");

        if(verification.contains(filter.getOrdonnateur())){

            if(filter.getType().equals("B&A")){

                list.add(programmeRepository.findSyntheseProgramme(Ordonnateur.valueOf(filter.getOrdonnateur()), ProgrammeType.BASE ,filter.getAnnee()));
                list.add(programmeRepository.findSyntheseProgramme(Ordonnateur.valueOf(filter.getOrdonnateur()), ProgrammeType.ADDITIONNEL ,filter.getAnnee()));

            }else {
                list.add(programmeRepository.findSyntheseProgramme(Ordonnateur.valueOf(filter.getOrdonnateur()), ProgrammeType.valueOf(filter.getType()) ,filter.getAnnee()));
            }

        }else if(filter.getOrdonnateur().equals("GLOBAL")){

            if(filter.getType().equals("B&A")){

                list.add(programmeRepository.findSyntheseProgramme(Ordonnateur.valueOf("MINHDU"), ProgrammeType.BASE ,filter.getAnnee()));
                list.add(programmeRepository.findSyntheseProgramme(Ordonnateur.valueOf("MINHDU"), ProgrammeType.ADDITIONNEL ,filter.getAnnee()));
                list.add(programmeRepository.findSyntheseProgramme(Ordonnateur.valueOf("MINTP"), ProgrammeType.BASE ,filter.getAnnee()));
                list.add(programmeRepository.findSyntheseProgramme(Ordonnateur.valueOf("MINTP"), ProgrammeType.ADDITIONNEL ,filter.getAnnee()));
                list.add(programmeRepository.findSyntheseProgramme(Ordonnateur.valueOf("MINT"), ProgrammeType.BASE ,filter.getAnnee()));
                list.add(programmeRepository.findSyntheseProgramme(Ordonnateur.valueOf("MINT"), ProgrammeType.ADDITIONNEL ,filter.getAnnee()));

            }else {

                list.add(programmeRepository.findSyntheseProgramme(Ordonnateur.valueOf("MINHDU"), ProgrammeType.valueOf(filter.getType())  ,filter.getAnnee()));
                list.add(programmeRepository.findSyntheseProgramme(Ordonnateur.valueOf("MINTP"), ProgrammeType.valueOf(filter.getType())  ,filter.getAnnee()));
                list.add(programmeRepository.findSyntheseProgramme(Ordonnateur.valueOf("MINT"), ProgrammeType.valueOf(filter.getType())  ,filter.getAnnee()));
            }

        }

        while (list.remove(null)){}
        return list;
    }


    public SyntheseDTO syntheseProgramme(List<Programme> programmes, String ordonnateur){

        SyntheseDTO synthese = new SyntheseDTO();

        if(programmes.isEmpty()){
            return synthese;
        }

        if(ordonnateur.equals("MINTP")){

            synthese=new SyntheseDTO("MINTP",new long[]{0,0,0},new long[]{0,0,0});

            for(Programme programme : programmes){

                List<Projet> centrale=  programme.getProjetList().stream().filter(p->((ProjetMINTP)p).getCategorie().equals("PROJETS A GESTION CENTRALE")).collect(Collectors.toList());
                List<Projet> regionale=  programme.getProjetList().stream().filter(p->((ProjetMINTP)p).getCategorie().equals("PROJETS A GESTION REGIONALE")).collect(Collectors.toList());
                List<Projet> communale=  programme.getProjetList().stream().filter(p->((ProjetMINTP)p).getCategorie().equals("PROJETS A GESTION COMMUNALE")).collect(Collectors.toList());

                synthese.prevision[0]=synthese.prevision[0]+totalBudget(centrale);
                synthese.engagement[0]=synthese.engagement[0]+totalEngagement(centrale);

                synthese.prevision[1]=synthese.prevision[1]+totalBudget(regionale);
                synthese.engagement[1]=synthese.engagement[1]+totalEngagement(regionale);

                synthese.prevision[2]=synthese.prevision[2]+totalBudget(communale);
                synthese.engagement[2]=synthese.engagement[2]+totalEngagement(communale);

            }
        }

        else if(ordonnateur.equals("MINT")){

            synthese=new SyntheseDTO("MINT",new long[]{0,0},new long[]{0,0});

            for(Programme programme : programmes){

                List<Projet> centrale=  programme.getProjetList().stream().filter(p->((ProjetMINT)p).getOrdonnateur().equals("MINT")).collect(Collectors.toList());
                List<Projet> communale=  programme.getProjetList().stream().filter(p->((ProjetMINT)p).getOrdonnateur().equals("MAIRE")).collect(Collectors.toList());

                synthese.prevision[0]=synthese.prevision[0]+totalBudget(centrale);
                synthese.engagement[0]=synthese.engagement[0]+totalEngagement(centrale);

                synthese.prevision[1]=synthese.prevision[1]+totalBudget(communale);
                synthese.engagement[1]=synthese.engagement[1]+totalEngagement(communale);

            }

        }

        else if(ordonnateur.equals("MINHDU")){

            synthese=new SyntheseDTO("MINHDU",new long[]{0,0,0,0},new long[]{0,0,0,0});

            for(Programme programme : programmes){

                List<Projet> centraleEVU= programme.getProjetList().stream().filter(p->( ((ProjetMINHDU)p).getOrdonnateur().equals("MINHDU") && ((ProjetMINHDU)p).getType_travaux().equals("ENTRETIEN DES VOIRIES URBAINES")) ).collect(Collectors.toList());
                List<Projet> centraleECT= programme.getProjetList().stream().filter(p->( ((ProjetMINHDU)p).getOrdonnateur().equals("MINHDU") && ((ProjetMINHDU)p).getType_travaux().equals("ETUDE DES CONTROLES TECHNIQUES")) ).collect(Collectors.toList());
                List<Projet> communaleEVU= programme.getProjetList().stream().filter(p->( ((ProjetMINHDU)p).getOrdonnateur().equals("MAIRE") && ((ProjetMINHDU)p).getType_travaux().equals("ENTRETIEN DES VOIRIES URBAINES")) ).collect(Collectors.toList());
                List<Projet> communaleECT= programme.getProjetList().stream().filter(p->( ((ProjetMINHDU)p).getOrdonnateur().equals("MAIRE") && ((ProjetMINHDU)p).getType_travaux().equals("ETUDE DES CONTROLES TECHNIQUES")) ).collect(Collectors.toList());


                synthese.prevision[0]=synthese.prevision[0]+totalBudget(centraleEVU);
                synthese.engagement[0]=synthese.engagement[0]+totalEngagement(centraleEVU);

                synthese.prevision[1]=synthese.prevision[1]+totalBudget(centraleECT);
                synthese.engagement[1]=synthese.engagement[1]+totalEngagement(centraleECT);

                synthese.prevision[2]=synthese.prevision[2]+totalBudget(communaleEVU);
                synthese.engagement[2]=synthese.engagement[2]+totalEngagement(communaleEVU);

                synthese.prevision[3]=synthese.prevision[3]+totalBudget(communaleECT);
                synthese.engagement[3]=synthese.engagement[3]+totalEngagement(communaleECT);

            }

        }

        else if(ordonnateur.equals("GLOBAL")){

            synthese=new SyntheseDTO("GLOBAL",new long[]{0,0,0},new long[]{0,0,0});


            for(Programme programme: programmes){

                if(programme.getOrdonnateur()==Ordonnateur.MINTP){
                    synthese.prevision[0]=synthese.prevision[0]+totalBudget(programme.getProjetList().stream().filter(p-> p.getFinancement().equals("NORMAL") ).collect(Collectors.toList()))+programme.getPrevision();
                    synthese.engagement[0]=synthese.engagement[0]+totalBudget(programme.getProjetList());

                }else if(programme.getOrdonnateur()==Ordonnateur.MINHDU){
                    synthese.prevision[1]=synthese.prevision[1]+totalBudget(programme.getProjetList().stream().filter(p-> p.getFinancement().equals("NORMAL") ).collect(Collectors.toList()))+programme.getPrevision();
                    synthese.engagement[1]=synthese.engagement[1]+totalBudget(programme.getProjetList());

                }else if(programme.getOrdonnateur()==Ordonnateur.MINT){
                    synthese.prevision[2]=synthese.prevision[2]+totalBudget(programme.getProjetList().stream().filter(p-> p.getFinancement().equals("NORMAL") ).collect(Collectors.toList()))+programme.getPrevision();
                    synthese.engagement[2]=synthese.engagement[2]+totalBudget(programme.getProjetList());

                }
            }

        }

        return synthese;

    }

    public long totalBudget(List<Projet> projets){
        long total = 0;

        for(Projet projet: projets){
            total=total + projet.getBudget_n();
        }

        return total;
    }

    public long totalEngagement(List<Projet> projets){
        long total = 0;

        for(Projet projet: projets){

            if(projet.getSuivi()!=null){
                total=total + projet.getSuivi().getEngagement();
            }
        }

        return total;
    }

    public void ajusterProgramme(Programme programme) throws CloneNotSupportedException {

        Programme newProgramme= (Programme) programme.clone();
        newProgramme.setId(0);
        newProgramme.setType(ProgrammeType.AJUSTER);
        newProgramme.setObservation(null);
        newProgramme.setUrl_resolution(null);
        newProgramme.setProgramme(programme);
        newProgramme.setStatut(ProgrammeStatut.EN_ATTENTE_DE_SOUMISSION);
        newProgramme.setIntitule("programme ajusté "+programme.getOrdonnateur()+" "+programme.getAnnee());
        newProgramme.setProjetList(null);
        programmeRepository.save(newProgramme);

        for(Projet projet: programme.getProjetList()){

            Projet cloneProjet= (Projet) projet.clone();
            cloneProjet.setProgramme(newProgramme);
            cloneProjet.setId(0);
            cloneProjet.setSuivi(null);
            cloneProjet.setPayement(null);
            cloneProjet.setSuiviTravaux(null);
            projetRepository.save(cloneProjet);

            if(projet.getSuivi()!=null){
                Suivi suivi= (Suivi) projet.getSuivi().clone();
                suivi.setId(0);
                suivi.setProjet(cloneProjet);
                cloneProjet.setSuivi(suivi);
                suiviRepository.save(suivi);
            }

            for(SuiviTravaux suiviTravaux: projet.getSuiviTravaux()){

                suiviTravaux= (SuiviTravaux) suiviTravaux.clone();
                suiviTravaux.setId(0);
                suiviTravaux.setProjet(cloneProjet);
                suiviTravauxRepository.save(suiviTravaux);
            }
            for(Payement payement : projet.getPayement()){
                payement= (Payement) payement.clone();
                payement.setId(0);
                payement.setProjet(cloneProjet);
                payementRepository.save(payement);
            }
        }


    }

    public void importMINHDUProgramme(InputStream inputStream ,Programme programme) throws IOException {
        List<ProjetMINHDU> list = new ArrayList<>();
        List ordonnateur=List.of("MAIRE","MINHDU");

        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
                continue; // Skip the header row
            }

            ProjetMINHDU projetMINHDU = new ProjetMINHDU();
            projetMINHDU.setProgramme(programme);

            for (int i = 0; i <= 12; i++) {
                Cell cell = CellUtil.getCell(row, i);
                if (cell != null) {
                    switch (i) {
                        case 1:
                            if (cell.getCellType() == CellType.STRING) {

                                String region=cell.getStringCellValue().replace("-","_");
                                projetMINHDU.setRegion(Region.valueOf(region));
                            }else{
                                throw (new IllegalArgumentException());
                            }
                            break;
                        case 2:
                            if (cell.getCellType() == CellType.STRING ) {
                                projetMINHDU.setVille(cell.getStringCellValue());

                            }else {
                                throw (new IllegalArgumentException());
                            }
                            break;
                        case 3:
                            if (cell.getCellType() == CellType.STRING) {

                                projetMINHDU.setType_travaux(cell.getStringCellValue());

                            }else{
                                throw (new IllegalArgumentException());
                            }
                            break;
                        case 4:
                            if (cell.getCellType() == CellType.STRING) {

                                projetMINHDU.setTroçon(cell.getStringCellValue());
                            }else {
                                throw (new IllegalArgumentException());

                            }
                            break;
                        case 5:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                projetMINHDU.setLineaire((float) cell.getNumericCellValue());
                            }
                            break;
                        case 6:
                            if (cell.getCellType() == CellType.NUMERIC) {

                                projetMINHDU.setTtc((long) cell.getNumericCellValue());
                            }else{
                                throw (new IllegalArgumentException());
                            }
                            break;
                        case 7:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                projetMINHDU.setBudget_anterieur((long) cell.getNumericCellValue());
                            }
                            break;
                        case 8:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                projetMINHDU.setBudget_n((long) cell.getNumericCellValue());
                            }else {
                                throw (new IllegalArgumentException());

                            }
                            break;
                        case 9:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                projetMINHDU.setBudget_n1((long) cell.getNumericCellValue());
                            }
                            break;
                        case 10:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                projetMINHDU.setBudget_n2((long) cell.getNumericCellValue());
                            }
                            break;
                        case 11:
                            if (cell.getCellType() == CellType.STRING) {
                                projetMINHDU.setPrestataire(cell.getStringCellValue());
                            }
                            break;
                        case 12:
                            if (cell.getCellType() == CellType.STRING && ordonnateur.contains(cell.getStringCellValue())) {

                                projetMINHDU.setOrdonnateur(cell.getStringCellValue());

                            }else {
                                throw (new IllegalArgumentException());
                            }
                            break;
                        case 13:
                            if (cell.getCellType() == CellType.STRING) {
                                projetMINHDU.setObservation(cell.getStringCellValue());
                            }
                            break;
                        default:
                            // Handle unknown columns or ignore them
                            break;
                    }
                }
            }

            list.add(projetMINHDU);
        }

        projetRepository.saveAll(list);


        if (workbook != null) {
            workbook.close();
        }
    }

}


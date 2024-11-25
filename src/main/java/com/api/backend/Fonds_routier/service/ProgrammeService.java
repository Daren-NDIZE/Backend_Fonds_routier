package com.api.backend.Fonds_routier.service;

import com.api.backend.Fonds_routier.DTO.MessageDTO;
import com.api.backend.Fonds_routier.DTO.ProgrammeFilterDTO;
import com.api.backend.Fonds_routier.DTO.SyntheseDTO;
import com.api.backend.Fonds_routier.enums.Ordonnateur;
import com.api.backend.Fonds_routier.enums.ProgrammeStatut;
import com.api.backend.Fonds_routier.enums.ProgrammeType;
import com.api.backend.Fonds_routier.enums.Region;
import com.api.backend.Fonds_routier.model.*;
import com.api.backend.Fonds_routier.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
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
    @Autowired
    PassationRepository passationRepository;


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

    public List<Programme> getProgrammeByOrdonnateurAndTypeAndYear(Ordonnateur ordonnateur,ProgrammeType type, int annee){

        return programmeRepository.findByOrdonnateurAndTypeAndAnnee(ordonnateur,type, annee);
    }

    public List<Programme> getProgrammeByOrdonnateurAndStatut(Ordonnateur ordonnateur,List<ProgrammeStatut> status){

        return programmeRepository.findAllByOrdonnateurAndStatutIn(ordonnateur, status);
    }

    public List<Programme> getSyntheseProgramme(ProgrammeFilterDTO filter)  {

        List<Programme> list = new ArrayList<>();

        List<String> verification =List.of("MINTP","MINT","MINHDU");

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

            synthese=new SyntheseDTO("MINTP",new long[]{0,0,0},new long[]{0,0,0},new float[]{0,0,0,0,0,0});

            for(Programme programme : programmes){

                List<Projet> centrale=  programme.getProjetList().stream().filter(p->((ProjetMINTP)p).getCategorie().equals("PROJET A GESTION CENTRALE")).collect(Collectors.toList());
                List<Projet> regionale=  programme.getProjetList().stream().filter(p->((ProjetMINTP)p).getCategorie().equals("PROJET A GESTION REGIONALE")).collect(Collectors.toList());
                List<Projet> communale=  programme.getProjetList().stream().filter(p->((ProjetMINTP)p).getCategorie().equals("PROJET A GESTION COMMUNALE")).collect(Collectors.toList());
                List<Projet> bitume=  programme.getProjetList().stream().filter(p->((ProjetMINTP)p).getType_travaux().equals("ROUTE BITUMÉE") && p.getFinancement().equals("NORMAL")).collect(Collectors.toList());
                List<Projet> terre=  programme.getProjetList().stream().filter(p->((ProjetMINTP)p).getType_travaux().equals("ROUTE EN TERRE") && p.getFinancement().equals("NORMAL")).collect(Collectors.toList());
                List<Projet> ouvrage= programme.getProjetList().stream().filter(p->p.getFinancement().equals("NORMAL")).collect(Collectors.toList());


                synthese.prevision[0]=synthese.prevision[0]+totalBudget(centrale)+ programme.getPrevision();
                synthese.engagement[0]=synthese.engagement[0]+totalEngagement(centrale);

                synthese.lineaire[0]=synthese.lineaire[0]+totalLineaire(bitume,"ROUTE");
                synthese.lineaire[1]=synthese.lineaire[1]+totalLineaireF(bitume,"ROUTE");

                synthese.prevision[1]=synthese.prevision[1]+totalBudget(regionale);
                synthese.engagement[1]=synthese.engagement[1]+totalEngagement(regionale);

                synthese.lineaire[2]=synthese.lineaire[2]+totalLineaire(terre,"ROUTE");
                synthese.lineaire[3]=synthese.lineaire[3]+totalLineaireF(terre,"ROUTE");

                synthese.prevision[2]=synthese.prevision[2]+totalBudget(communale);
                synthese.engagement[2]=synthese.engagement[2]+totalEngagement(communale);

                synthese.lineaire[4]=synthese.lineaire[4]+totalLineaire(ouvrage,"OA");
                synthese.lineaire[5]=synthese.lineaire[5]+totalLineaireF(ouvrage,"OA");
            }
        }

        else if(ordonnateur.equals("MINT")){

            synthese=new SyntheseDTO("MINT",new long[]{0,0},new long[]{0,0},new float[]{0});

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

            synthese=new SyntheseDTO("MINHDU",new long[]{0,0,0,0},new long[]{0,0,0,0},new float[]{0,0});

            for(Programme programme : programmes){

                List<Projet> centraleEVU= programme.getProjetList().stream().filter(p->( ((ProjetMINHDU)p).getOrdonnateur().equals("MINHDU") && ((ProjetMINHDU)p).getType_travaux().equals("ENTRETIEN DES VOIRIES URBAINES")) ).collect(Collectors.toList());
                List<Projet> centraleECT= programme.getProjetList().stream().filter(p->( ((ProjetMINHDU)p).getOrdonnateur().equals("MINHDU") && ((ProjetMINHDU)p).getType_travaux().equals("ETUDES ET CONTROLES TECHNIQUES")) ).collect(Collectors.toList());
                List<Projet> communaleEVU= programme.getProjetList().stream().filter(p->( ((ProjetMINHDU)p).getOrdonnateur().equals("MAIRE") && ((ProjetMINHDU)p).getType_travaux().equals("ENTRETIEN DES VOIRIES URBAINES")) ).collect(Collectors.toList());
                List<Projet> communaleECT= programme.getProjetList().stream().filter(p->( ((ProjetMINHDU)p).getOrdonnateur().equals("MAIRE") && ((ProjetMINHDU)p).getType_travaux().equals("ETUDES ET CONTROLES TECHNIQUES")) ).collect(Collectors.toList());
                List<Projet> lineaire= programme.getProjetList().stream().filter(p->p.getFinancement().equals("NORMAL") ).collect(Collectors.toList());


                synthese.prevision[0]=synthese.prevision[0]+totalBudget(centraleEVU);
                synthese.engagement[0]=synthese.engagement[0]+totalEngagement(centraleEVU);
                synthese.lineaire[0]=synthese.lineaire[0]+totalLineaire(lineaire,"OA");
                synthese.lineaire[1]=synthese.lineaire[1]+totalLineaireF(lineaire,"OA");


                synthese.prevision[1]=synthese.prevision[1]+totalBudget(centraleECT);
                synthese.engagement[1]=synthese.engagement[1]+totalEngagement(centraleECT);

                synthese.prevision[2]=synthese.prevision[2]+totalBudget(communaleEVU);
                synthese.engagement[2]=synthese.engagement[2]+totalEngagement(communaleEVU);

                synthese.prevision[3]=synthese.prevision[3]+totalBudget(communaleECT);
                synthese.engagement[3]=synthese.engagement[3]+totalEngagement(communaleECT);

            }

        }

        else if(ordonnateur.equals("GLOBAL")){

            synthese=new SyntheseDTO("GLOBAL",new long[]{0,0,0},new long[]{0,0,0},new float[]{0});


            for(Programme programme: programmes){

                if(programme.getOrdonnateur()==Ordonnateur.MINTP){
                    synthese.prevision[0]=synthese.prevision[0]+totalBudget(programme.getProjetList().stream().filter(p-> p.getFinancement().equals("NORMAL") ).collect(Collectors.toList()))+programme.getPrevision();
                    synthese.engagement[0]=synthese.engagement[0]+totalEngagement(programme.getProjetList());

                }else if(programme.getOrdonnateur()==Ordonnateur.MINHDU){
                    synthese.prevision[1]=synthese.prevision[1]+totalBudget(programme.getProjetList().stream().filter(p-> p.getFinancement().equals("NORMAL") ).collect(Collectors.toList()))+programme.getPrevision();
                    synthese.engagement[1]=synthese.engagement[1]+totalEngagement(programme.getProjetList());

                }else if(programme.getOrdonnateur()==Ordonnateur.MINT){
                    synthese.prevision[2]=synthese.prevision[2]+totalBudget(programme.getProjetList().stream().filter(p-> p.getFinancement().equals("NORMAL") ).collect(Collectors.toList()))+programme.getPrevision();
                    synthese.engagement[2]=synthese.engagement[2]+totalEngagement(programme.getProjetList());

                }
            }

        }

        return synthese;

    }

    public long totalBudget(List<Projet> projets){
        long total = 0;

        for(Projet projet: projets){

            if(projet.getFinancement().equals("RESERVE")){
                continue;
            }
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

    public long totalReserve(List<Projet> projets){
        long total = 0;

        for(Projet projet: projets){

            if(projet.getFinancement().equals("NORMAL")){
                continue;
            }
            total=total + projet.getBudget_n();
        }

        return total;
    }

    public float totalLineaire(List<Projet> projets,String type){

        float total = 0;

        if(projets.isEmpty()){
            return total;
        }

        if(projets.get(0) instanceof ProjetMINTP){

            if(type.equals("OA")){

                for(Projet projet: projets){
                    total= total + ((ProjetMINTP) projet).getLineaire_oa();
                }
                return (float) (Math.round(total*100.0)/100.0);
            }

            for(Projet projet: projets){
                total= total + ((ProjetMINTP) projet).getLineaire_route();
            }

        }else if(projets.get(0) instanceof ProjetMINHDU){

            for(Projet projet: projets){
                total= total + ((ProjetMINHDU) projet).getLineaire();
            }
        }

        return (float) (Math.round(total*100.0)/100.0);
    }

    public float totalLineaireF(List<Projet> projets,String type){

        float total = 0;

        projets= projets.stream().filter(p->p.getSuivi()!=null && p.getSuivi().getEngagement()!=0).collect(Collectors.toList());

        if(projets.isEmpty()){
            return total;
        }

        if(projets.get(0) instanceof ProjetMINTP){

            if(type.equals("OA")){

                for(Projet projet: projets){
                    total= total + ((ProjetMINTP) projet).getLineaire_oa();
                }
                return (float) (Math.round(total*100.0)/100.0);
            }

            for(Projet projet: projets){
                total= total + ((ProjetMINTP) projet).getLineaire_route();
            }

        }else if(projets.get(0) instanceof ProjetMINHDU){

            for(Projet projet: projets){
                total= total + ((ProjetMINHDU) projet).getLineaire();
            }
        }

        return (float) (Math.round(total*100.0)/100.0);
    }

    public void ajusterProgramme(Programme programme) throws CloneNotSupportedException {

        Programme newProgramme= (Programme) programme.clone();
        newProgramme.setId(0);
        newProgramme.setType(ProgrammeType.AJUSTER);
        newProgramme.setObservation(null);
        newProgramme.setUrl_resolution(null);
        newProgramme.setProgramme(programme);
        newProgramme.setStatut(ProgrammeStatut.EN_ATTENTE_DE_SOUMISSION);
        if(programme.getProgramme()==null){
            newProgramme.setIntitule("programme ajusté "+programme.getOrdonnateur()+" "+programme.getAnnee());
        }else{
            newProgramme.setIntitule("programme réajusté "+programme.getOrdonnateur()+" "+programme.getAnnee());
        }
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

            for(Passation passation: projet.getPassation()){

                passation= (Passation) passation.clone();
                passation.setId(0);
                passation.setProjet(cloneProjet);
                passationRepository.save(passation);
            }

            for(Payement payement : projet.getPayement()){
                payement= (Payement) payement.clone();
                payement.setId(0);
                payement.setProjet(cloneProjet);
                payementRepository.save(payement);
            }
        }


    }

    public MessageDTO importMINHDUProgramme(InputStream inputStream , Programme programme) throws IOException {
        List<ProjetMINHDU> list = new ArrayList<>();
        List ordonnateur=List.of("MAIRE","MINHDU");
        List travaux=List.of("ENTRETIEN DES VOIRIES URBAINES","ETUDES ET CONTROLES TECHNIQUES");


        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();


        for (Row row : sheet) {
            if (row.getRowNum() <= 3) {
                continue; // Skip the header row
            }

            ProjetMINHDU projetMINHDU = new ProjetMINHDU();
            projetMINHDU.setFinancement("NORMAL");
            projetMINHDU.setProgramme(programme);

            Cell cel = CellUtil.getCell(row, 0);

            if(isCellMerge(sheet,cel)){

                if (cel.getCellType() == CellType.STRING ) {
                    if(cel.getStringCellValue().equals("TOTAL")){
                      break;
                    }else{
                        return new MessageDTO("erreur","erreur d'importation à la cellule "+getCellLetter(cel));
                    }
                }else {
                    return new MessageDTO("erreur","erreur d'importation à la cellule "+getCellLetter(cel));
                }

            }

            for (int i = 1; i <= 13; i++) {
                Cell cell = CellUtil.getCell(row, i);

                if (cell != null) {
                    switch (i) {

                        case 1:
                            if (cell.getCellType() == CellType.STRING) {
                                try {
                                    String region = cell.getStringCellValue().replace("-", "_");
                                    projetMINHDU.setRegion(Region.valueOf(region));
                                }
                                catch (IllegalArgumentException e){
                                    return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell));
                                }
                            }else{

                                return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell));
                            }
                            break;
                        case 2:
                            if (cell.getCellType() == CellType.STRING ) {
                                projetMINHDU.setVille(cell.getStringCellValue());

                            }else {
                                return new MessageDTO("erreur","erreur d'importation à la cellule "+getCellLetter(cell));
                            }
                            break;
                        case 3:
                            if (cell.getCellType() == CellType.STRING && travaux.contains(cell.getStringCellValue())) {

                                projetMINHDU.setType_travaux(cell.getStringCellValue());

                            }else{
                                return new MessageDTO("erreur","erreur d'importation à la cellule "+getCellLetter(cell));
                            }
                            break;
                        case 4:
                            if (cell.getCellType() == CellType.STRING) {

                                projetMINHDU.setTroçon(cell.getStringCellValue());
                            }else {

                                return new MessageDTO("erreur","erreur d'importation à la cellule "+getCellLetter(cell));
                            }
                            break;
                        case 5:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                projetMINHDU.setLineaire((float) cell.getNumericCellValue());
                            }else if(cell.getCellType()== CellType.FORMULA){

                                CellValue cellValue = evaluator.evaluate(cell);
                                projetMINHDU.setLineaire((float) cellValue.getNumberValue());
                            }
                            break;
                        case 6:
                            if (cell.getCellType() == CellType.NUMERIC) {

                                projetMINHDU.setTtc((long) cell.getNumericCellValue());
                            }else if(cell.getCellType()== CellType.FORMULA){

                                CellValue cellValue = evaluator.evaluate(cell);
                                projetMINHDU.setTtc((long) cellValue.getNumberValue());
                            }
                            else{
                                return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell));
                            }
                            break;
                        case 7:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                    projetMINHDU.setBudget_anterieur((long) cell.getNumericCellValue());
                            }else if(cell.getCellType()== CellType.FORMULA){

                                CellValue cellValue = evaluator.evaluate(cell);
                                projetMINHDU.setBudget_anterieur((long) cellValue.getNumberValue());
                            }
                            break;
                        case 8:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                projetMINHDU.setBudget_n((long) cell.getNumericCellValue());
                            }else if(cell.getCellType()== CellType.FORMULA){

                                CellValue cellValue = evaluator.evaluate(cell);
                                projetMINHDU.setBudget_n((long) cellValue.getNumberValue());
                            }
                            else {
                                return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell));
                            }
                            break;
                        case 9:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                projetMINHDU.setBudget_n1((long) cell.getNumericCellValue());
                            }else if(cell.getCellType()== CellType.FORMULA){

                                CellValue cellValue = evaluator.evaluate(cell);
                                projetMINHDU.setBudget_n1((long) cellValue.getNumberValue());
                            }
                            break;
                        case 10:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                projetMINHDU.setBudget_n2((long) cell.getNumericCellValue());
                            }else if(cell.getCellType()== CellType.FORMULA){

                                CellValue cellValue = evaluator.evaluate(cell);
                                projetMINHDU.setBudget_n2((long) cellValue.getNumberValue());
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
                                return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell));
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

        return new MessageDTO("succes","Importation réussie");

    }

    public MessageDTO importMINTPProgramme(InputStream inputStream ,Programme programme) throws IOException {
        List<ProjetMINTP> list = new ArrayList<>();
        List categorie=List.of("PROJET A GESTION CENTRALE","PROJET A GESTION REGIONALE");
        List travaux=List.of("ROUTE EN TERRE","ROUTE BITUMÉE","OUVRAGE D'ART");

        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Sheet secondSheet = workbook.getSheetAt(1);
        Sheet lastSheet = workbook.getSheetAt(2);


        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

        for (Row row : sheet) {
            if (row.getRowNum() <= 3) {
                continue; // Skip the header row
            }

            ProjetMINTP projetMINTP = new ProjetMINTP();
            projetMINTP.setFinancement("NORMAL");
            projetMINTP.setCategorie("PROJET A GESTION CENTRALE");
            projetMINTP.setProgramme(programme);

            Cell cel = CellUtil.getCell(row, 0);

            if(isCellMerge(sheet,cel)){

                if (cel.getCellType() == CellType.STRING ) {
                    if(cel.getStringCellValue().equals("TOTAL")){
                        break;
                    }else{
                        return new MessageDTO("erreur","erreur d'importation à la cellule "+getCellLetter(cel)+" de la première feuille");
                    }
                }else {
                    return new MessageDTO("erreur","erreur d'importation à la cellule "+getCellLetter(cel)+" de la première feuille");
                }

            }

            for (int i = 1; i <= 13; i++) {
                Cell cell = CellUtil.getCell(row, i);
                if (cell != null) {
                    switch (i) {
                        case 1:
                            if (cell.getCellType() == CellType.STRING) {
                                try {
                                    String region = cell.getStringCellValue().replace("-", "_");
                                    projetMINTP.setRegion(Region.valueOf(region));
                                }
                                catch(IllegalArgumentException e){
                                        return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell)+" de la première feuille");
                                    }
                            }else{
                                return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell)+" de la première feuille");
                            }
                            break;

                        case 2:
                            if (cell.getCellType() == CellType.STRING && travaux.contains(cell.getStringCellValue())) {
                                projetMINTP.setType_travaux(cell.getStringCellValue());
                            }else {
                                return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell)+" de la première feuille");
                            }
                            break;
                        case 3:
                            if (cell.getCellType() == CellType.STRING ) {
                                projetMINTP.setProjet(cell.getStringCellValue());

                            }else{
                                return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell)+" de la première feuille");
                            }
                            break;
                        case 4:
                            if (cell.getCellType() == CellType.STRING) {

                                projetMINTP.setCode_route(cell.getStringCellValue());
                            }
                            break;
                        case 5:
                            if (cell.getCellType() == CellType.NUMERIC) {

                                projetMINTP.setLineaire_route((float) cell.getNumericCellValue());
                            }
                            break;
                        case 6:
                            if (cell.getCellType() == CellType.NUMERIC) {

                                projetMINTP.setLineaire_oa((float) cell.getNumericCellValue());
                            }
                            break;
                        case 7:
                            if (cell.getCellType() == CellType.NUMERIC) {

                                projetMINTP.setTtc((long) cell.getNumericCellValue());
                            }else if(cell.getCellType()== CellType.FORMULA){

                                CellValue cellValue = evaluator.evaluate(cell);
                                projetMINTP.setTtc((long) cellValue.getNumberValue());
                            }
                            else {
                                return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell)+" de la première feuille");
                            }
                            break;
                        case 8:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                projetMINTP.setBudget_anterieur((long) cell.getNumericCellValue());
                            }else if(cell.getCellType()== CellType.FORMULA){

                                CellValue cellValue = evaluator.evaluate(cell);
                                projetMINTP.setBudget_anterieur((long) cellValue.getNumberValue());
                            }
                            break;
                        case 9:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                projetMINTP.setBudget_n((long) cell.getNumericCellValue());
                            }else if(cell.getCellType()== CellType.FORMULA){

                                CellValue cellValue = evaluator.evaluate(cell);
                                projetMINTP.setBudget_n((long) cellValue.getNumberValue());
                            }
                            else {
                                return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell)+" de la première feuille");
                            }
                            break;
                        case 10:
                            if (cell.getCellType() == CellType.NUMERIC) {

                                projetMINTP.setBudget_n1((long) cell.getNumericCellValue());
                            }else if(cell.getCellType()== CellType.FORMULA){

                                CellValue cellValue = evaluator.evaluate(cell);
                                projetMINTP.setBudget_n1((long) cellValue.getNumberValue());
                            }
                            break;
                        case 11:
                            if (cell.getCellType() == CellType.NUMERIC) {

                                projetMINTP.setBudget_n2((long) cell.getNumericCellValue());
                            }else if(cell.getCellType()== CellType.FORMULA){

                                CellValue cellValue = evaluator.evaluate(cell);
                                projetMINTP.setBudget_n2((long) cellValue.getNumberValue());
                            }
                            break;
                        case 12:
                            if (cell.getCellType() == CellType.STRING) {

                                projetMINTP.setPrestataire(cell.getStringCellValue());
                            }
                            break;
                        case 13:
                            if (cell.getCellType() == CellType.STRING) {

                                projetMINTP.setObservation(cell.getStringCellValue());
                            }
                            break;
                        default:
                            // Handle unknown columns or ignore them
                            break;
                    }
                }
            }

            list.add(projetMINTP);
        }

        for (Row row : secondSheet) {
            if (row.getRowNum() <= 3) {
                continue; // Skip the header row
            }

            ProjetMINTP projetMINTP = new ProjetMINTP();
            projetMINTP.setFinancement("NORMAL");
            projetMINTP.setCategorie("PROJET A GESTION REGIONALE");
            projetMINTP.setProgramme(programme);

            Cell cel = CellUtil.getCell(row, 0);

            if(isCellMerge(secondSheet,cel)){

                if (cel.getCellType() == CellType.STRING ) {
                    if(cel.getStringCellValue().equals("TOTAL")){
                        break;
                    }else{
                        return new MessageDTO("erreur","erreur d'importation à la cellule "+getCellLetter(cel)+" de la deuxième feuille");
                    }
                }else {
                    return new MessageDTO("erreur","erreur d'importation à la cellule "+getCellLetter(cel)+" de la deuxième feuille");
                }

            }

            for (int i = 1; i <= 13; i++) {
                Cell cell = CellUtil.getCell(row, i);
                if (cell != null) {
                    switch (i) {
                        case 1:
                            if (cell.getCellType() == CellType.STRING) {
                                try {
                                    String region = cell.getStringCellValue().replace("-", "_");
                                    projetMINTP.setRegion(Region.valueOf(region));
                                }
                                catch(IllegalArgumentException e){
                                    return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell)+" de la deuxième feuille");
                                }
                            }else{
                                return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell)+" de la deuxième feuille");
                            }
                            break;

                        case 2:
                            if (cell.getCellType() == CellType.STRING && travaux.contains(cell.getStringCellValue())) {
                                projetMINTP.setType_travaux(cell.getStringCellValue());
                            }else {
                                return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell)+" de la deuxième feuille");
                            }
                            break;
                        case 3:
                            if (cell.getCellType() == CellType.STRING ) {
                                projetMINTP.setProjet(cell.getStringCellValue());

                            }else{
                                return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell)+" de la deuxième feuille");
                            }
                            break;
                        case 4:
                            if (cell.getCellType() == CellType.STRING) {

                                projetMINTP.setCode_route(cell.getStringCellValue());
                            }
                            break;
                        case 5:
                            if (cell.getCellType() == CellType.NUMERIC) {

                                projetMINTP.setLineaire_route((float) cell.getNumericCellValue());
                            }
                            break;
                        case 6:
                            if (cell.getCellType() == CellType.NUMERIC) {

                                projetMINTP.setLineaire_oa((float) cell.getNumericCellValue());
                            }
                            break;
                        case 7:
                            if (cell.getCellType() == CellType.NUMERIC) {

                                projetMINTP.setTtc((long) cell.getNumericCellValue());
                            }else if(cell.getCellType()== CellType.FORMULA){

                                CellValue cellValue = evaluator.evaluate(cell);
                                projetMINTP.setTtc((long) cellValue.getNumberValue());
                            }
                            else {
                                return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell)+" de la deuxième feuille");
                            }
                            break;
                        case 8:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                projetMINTP.setBudget_anterieur((long) cell.getNumericCellValue());
                            }else if(cell.getCellType()== CellType.FORMULA){

                                CellValue cellValue = evaluator.evaluate(cell);
                                projetMINTP.setBudget_anterieur((long) cellValue.getNumberValue());
                            }
                            break;
                        case 9:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                projetMINTP.setBudget_n((long) cell.getNumericCellValue());
                            }else if(cell.getCellType()== CellType.FORMULA){

                                CellValue cellValue = evaluator.evaluate(cell);
                                projetMINTP.setBudget_n((long) cellValue.getNumberValue());
                            }
                            else {
                                return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell)+" de la deuxième feuille");
                            }
                            break;
                        case 10:
                            if (cell.getCellType() == CellType.NUMERIC) {

                                projetMINTP.setBudget_n1((long) cell.getNumericCellValue());
                            }else if(cell.getCellType()== CellType.FORMULA){

                                CellValue cellValue = evaluator.evaluate(cell);
                                projetMINTP.setBudget_n1((long) cellValue.getNumberValue());
                            }
                            break;
                        case 11:
                            if (cell.getCellType() == CellType.NUMERIC) {

                                projetMINTP.setBudget_n2((long) cell.getNumericCellValue());
                            }else if(cell.getCellType()== CellType.FORMULA){

                                CellValue cellValue = evaluator.evaluate(cell);
                                projetMINTP.setBudget_n2((long) cellValue.getNumberValue());
                            }
                            break;
                        case 12:
                            if (cell.getCellType() == CellType.STRING) {

                                projetMINTP.setPrestataire(cell.getStringCellValue());
                            }
                            break;
                        case 13:
                            if (cell.getCellType() == CellType.STRING) {

                                projetMINTP.setObservation(cell.getStringCellValue());
                            }
                            break;
                        default:
                            // Handle unknown columns or ignore them
                            break;
                    }
                }
            }

            list.add(projetMINTP);
        }

        for (Row row : lastSheet) {
            if (row.getRowNum() <= 3) {
                continue; // Skip the header row
            }

            ProjetMINTP projetMINTP = new ProjetMINTP();
            projetMINTP.setFinancement("NORMAL");
            projetMINTP.setCategorie("PROJET A GESTION COMMUNALE");
            projetMINTP.setProgramme(programme);

            Cell cel = CellUtil.getCell(row, 0);

            if(isCellMerge(secondSheet,cel)){

                if (cel.getCellType() == CellType.STRING ) {
                    if(cel.getStringCellValue().equals("TOTAL")){
                        break;
                    }else{
                        return new MessageDTO("erreur","erreur d'importation à la cellule "+getCellLetter(cel)+" de la troisième feuille");
                    }
                }else {
                    return new MessageDTO("erreur","erreur d'importation à la cellule "+getCellLetter(cel)+" de la troisième feuille");
                }

            }

            for (int i = 1; i <= 15; i++) {
                Cell cell = CellUtil.getCell(row, i);
                if (cell != null) {
                    switch (i) {
                        case 1:
                            if (cell.getCellType() == CellType.STRING) {
                                try {
                                    String region = cell.getStringCellValue().replace("-", "_");
                                    projetMINTP.setRegion(Region.valueOf(region));
                                }
                                catch(IllegalArgumentException e){
                                    return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell)+" de la troisième feuille");
                                }
                            }else{
                                return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell)+" de la troisième feuille");
                            }
                            break;
                        case 2:
                            if (cell.getCellType() == CellType.STRING) {
                                projetMINTP.setDepartement(cell.getStringCellValue());
                            }else{
                                return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell)+" de la troisième feuille");
                            }
                            break;
                        case 3:
                            if (cell.getCellType() == CellType.STRING) {
                                projetMINTP.setCommune(cell.getStringCellValue());
                            }else{
                                return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell)+" de la troisième feuille");
                            }
                            break;
                        case 4:
                            if (cell.getCellType() == CellType.STRING && travaux.contains(cell.getStringCellValue())) {
                                projetMINTP.setType_travaux(cell.getStringCellValue());
                            }else {
                                return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell)+" de la troisième feuille");
                            }
                            break;
                        case 5:
                            if (cell.getCellType() == CellType.STRING ) {
                                projetMINTP.setProjet(cell.getStringCellValue());

                            }else{
                                return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell)+" de la troisième feuille");
                            }
                            break;
                        case 6:
                            if (cell.getCellType() == CellType.STRING) {

                                projetMINTP.setCode_route(cell.getStringCellValue());
                            }
                            break;
                        case 7:
                            if (cell.getCellType() == CellType.NUMERIC) {

                                projetMINTP.setLineaire_route((float) cell.getNumericCellValue());
                            }
                            break;
                        case 8:
                            if (cell.getCellType() == CellType.NUMERIC) {

                                projetMINTP.setLineaire_oa((float) cell.getNumericCellValue());
                            }
                            break;
                        case 9:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                projetMINTP.setTtc((long) cell.getNumericCellValue());
                            }else if(cell.getCellType()== CellType.FORMULA){

                                CellValue cellValue = evaluator.evaluate(cell);
                                projetMINTP.setTtc((long) cellValue.getNumberValue());
                            }
                            else {
                                return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell)+" de la troisième feuille");
                            }
                            break;
                        case 10:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                projetMINTP.setBudget_anterieur((long) cell.getNumericCellValue());
                            }else if(cell.getCellType()== CellType.FORMULA){

                                CellValue cellValue = evaluator.evaluate(cell);
                                projetMINTP.setBudget_anterieur((long) cellValue.getNumberValue());
                            }
                            break;
                        case 11:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                projetMINTP.setBudget_n((long) cell.getNumericCellValue());

                            }else if(cell.getCellType()== CellType.FORMULA){

                                CellValue cellValue = evaluator.evaluate(cell);
                                projetMINTP.setBudget_n((long) cellValue.getNumberValue());
                            }
                            else {
                                return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell)+" de la troisième feuille");
                            }
                            break;
                        case 12:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                projetMINTP.setBudget_n1((long) cell.getNumericCellValue());
                            }else if(cell.getCellType()== CellType.FORMULA){

                                CellValue cellValue = evaluator.evaluate(cell);
                                projetMINTP.setBudget_n1((long) cellValue.getNumberValue());
                            }
                            break;
                        case 13:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                projetMINTP.setBudget_n2((long) cell.getNumericCellValue());
                            }else if(cell.getCellType()== CellType.FORMULA){

                                CellValue cellValue = evaluator.evaluate(cell);
                                projetMINTP.setBudget_n2((long) cellValue.getNumberValue());
                            }
                            break;
                        case 14:
                            if (cell.getCellType() == CellType.STRING) {
                                projetMINTP.setPrestataire(cell.getStringCellValue());
                            }
                            break;
                        case 15:
                            if (cell.getCellType() == CellType.STRING) {
                                projetMINTP.setObservation(cell.getStringCellValue());
                            }
                            break;
                        default:
                            // Handle unknown columns or ignore them
                            break;
                    }
                }
            }

            list.add(projetMINTP);
        }

        projetRepository.saveAll(list);


        if (workbook != null) {
            workbook.close();
        }

        return new MessageDTO("succes","Importation réussie");

    }

    public MessageDTO importMINTProgramme(InputStream inputStream ,Programme programme) throws IOException {
        List<ProjetMINT> list = new ArrayList<>();

        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Sheet sencondSheet = workbook.getSheetAt(1);

        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

        for (Row row : sheet) {
            if (row.getRowNum() <= 3) {
                continue; // Skip the header row
            }

            ProjetMINT projetMINT = new ProjetMINT();
            projetMINT.setFinancement("NORMAL");
            projetMINT.setOrdonnateur("MINT");
            projetMINT.setProgramme(programme);

            Cell cel = CellUtil.getCell(row, 0);

            if(isCellMerge(sheet,cel)){

                if (cel.getCellType() == CellType.STRING ) {
                    if(cel.getStringCellValue().equals("TOTAL")){
                        break;
                    }else{
                        return new MessageDTO("erreur","erreur d'importation à la cellule "+getCellLetter(cel)+" de la première feuille");
                    }
                }else {
                    return new MessageDTO("erreur","erreur d'importation à la cellule "+getCellLetter(cel)+" de la première feuille");
                }

            }

            for (int i = 1; i <= 11; i++) {
                Cell cell = CellUtil.getCell(row, i);
                if (cell != null) {
                    switch (i) {
                        case 1:
                            if (cell.getCellType() == CellType.STRING) {
                                try {
                                    String region = cell.getStringCellValue().replace("-", "_");
                                    projetMINT.setRegion(Region.valueOf(region));

                                }catch (IllegalArgumentException e){
                                    return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell)+" de la première feuille");
                                }
                            }else{
                                return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell)+" de la première feuille");
                            }
                            break;
                        case 2:
                            if (cell.getCellType() == CellType.STRING ) {

                                projetMINT.setMission(cell.getStringCellValue());
                            }else {
                                return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell)+" de la première feuille");
                            }
                            break;
                        case 3:
                            if (cell.getCellType() == CellType.STRING ) {

                                projetMINT.setObjectif(cell.getStringCellValue());

                            }else{
                                return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell)+" de la première feuille");
                            }
                            break;
                        case 4:
                            if (cell.getCellType() == CellType.STRING) {

                                projetMINT.setAllotissement(cell.getStringCellValue());
                            }
                            break;
                        case 5:
                            if (cell.getCellType() == CellType.NUMERIC) {

                                projetMINT.setTtc((long) cell.getNumericCellValue());
                            }else if(cell.getCellType()== CellType.FORMULA){

                                CellValue cellValue = evaluator.evaluate(cell);
                                projetMINT.setTtc((long) cellValue.getNumberValue());
                            }
                            else{
                                return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell)+" de la première feuille");
                            }
                            break;
                        case 6:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                projetMINT.setBudget_anterieur((long) cell.getNumericCellValue());
                            }else if(cell.getCellType()== CellType.FORMULA){

                                CellValue cellValue = evaluator.evaluate(cell);
                                projetMINT.setBudget_anterieur((long) cellValue.getNumberValue());
                            }
                            break;
                        case 7:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                projetMINT.setBudget_n((long) cell.getNumericCellValue());
                            }else if(cell.getCellType()== CellType.FORMULA){

                                CellValue cellValue = evaluator.evaluate(cell);
                                projetMINT.setBudget_n((long) cellValue.getNumberValue());
                            }
                            else {
                                return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell)+" de la première feuille");
                            }
                            break;
                        case 8:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                projetMINT.setBudget_n1((long) cell.getNumericCellValue());
                            }else if(cell.getCellType()== CellType.FORMULA){

                                CellValue cellValue = evaluator.evaluate(cell);
                                projetMINT.setBudget_n1((long) cellValue.getNumberValue());
                            }
                            break;
                        case 9:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                projetMINT.setBudget_n2((long) cell.getNumericCellValue());
                            }else if(cell.getCellType()== CellType.FORMULA){

                                CellValue cellValue = evaluator.evaluate(cell);
                                projetMINT.setBudget_n2((long) cellValue.getNumberValue());
                            }
                            break;
                        case 10:
                            if (cell.getCellType() == CellType.STRING) {
                                projetMINT.setPrestataire(cell.getStringCellValue());
                            }
                            break;

                        case 11:
                            if (cell.getCellType() == CellType.STRING) {
                                projetMINT.setObservation(cell.getStringCellValue());
                            }
                            break;
                        default:
                            // Handle unknown columns or ignore them
                            break;
                    }
                }
            }

            list.add(projetMINT);
        }

        for (Row row : sencondSheet) {
            if (row.getRowNum() <= 3) {
                continue; // Skip the header row
            }

            ProjetMINT projetMINT = new ProjetMINT();
            projetMINT.setFinancement("NORMAL");
            projetMINT.setOrdonnateur("MAIRE");
            projetMINT.setProgramme(programme);

            Cell cel = CellUtil.getCell(row, 0);

            if(isCellMerge(sencondSheet,cel)){

                if (cel.getCellType() == CellType.STRING ) {
                    if(cel.getStringCellValue().equals("TOTAL")){
                        break;
                    }else{
                        return new MessageDTO("erreur","erreur d'importation à la cellule "+getCellLetter(cel)+" de la deuxième feuille");
                    }
                }else {
                    return new MessageDTO("erreur","erreur d'importation à la cellule "+getCellLetter(cel)+" de la deuxième feuille");
                }

            }

            for (int i = 1; i <= 13; i++) {
                Cell cell = CellUtil.getCell(row, i);
                if (cell != null) {
                    switch (i) {
                        case 1:
                            if (cell.getCellType() == CellType.STRING) {
                                try {
                                    String region = cell.getStringCellValue().replace("-", "_");
                                    projetMINT.setRegion(Region.valueOf(region));

                                }catch (IllegalArgumentException e){
                                    return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell)+" de la deuxième feuille");
                                }
                            }else{
                                return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell)+" de la deuxième feuille");
                            }
                            break;
                        case 2:
                            if (cell.getCellType() == CellType.STRING ) {

                                projetMINT.setDepartement(cell.getStringCellValue());
                            }else {
                                return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell)+" de la deuxième feuille");
                            }
                            break;
                        case 3:
                            if (cell.getCellType() == CellType.STRING ) {

                                projetMINT.setCommune(cell.getStringCellValue());
                            }else {
                                return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell)+" de la deuxième feuille");
                            }
                            break;
                        case 4:
                            if (cell.getCellType() == CellType.STRING ) {

                                projetMINT.setMission(cell.getStringCellValue());
                            }else {
                                return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell)+" de la deuxième feuille");
                            }
                            break;
                        case 5:
                            if (cell.getCellType() == CellType.STRING ) {

                                projetMINT.setObjectif(cell.getStringCellValue());

                            }
                            break;
                        case 6:
                            if (cell.getCellType() == CellType.STRING) {

                                projetMINT.setAllotissement(cell.getStringCellValue());
                            }
                            break;
                        case 7:
                            if (cell.getCellType() == CellType.NUMERIC) {

                                projetMINT.setTtc((long) cell.getNumericCellValue());
                            }else if(cell.getCellType()== CellType.FORMULA){

                                CellValue cellValue = evaluator.evaluate(cell);
                                projetMINT.setTtc((long) cellValue.getNumberValue());
                            }
                            else{
                                return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell)+" de la deuxième feuille");
                            }
                            break;
                        case 8:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                projetMINT.setBudget_anterieur((long) cell.getNumericCellValue());
                            }else if(cell.getCellType()== CellType.FORMULA){

                                CellValue cellValue = evaluator.evaluate(cell);
                                projetMINT.setBudget_anterieur((long) cellValue.getNumberValue());
                            }
                            break;
                        case 9:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                projetMINT.setBudget_n((long) cell.getNumericCellValue());
                            }else if(cell.getCellType()== CellType.FORMULA){

                                CellValue cellValue = evaluator.evaluate(cell);
                                projetMINT.setBudget_n((long) cellValue.getNumberValue());
                            }
                            else {
                                return new MessageDTO("erreur","Erreur d'importation à la cellule "+getCellLetter(cell)+" de la deuxième feuille");
                            }
                            break;
                        case 10:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                projetMINT.setBudget_n1((long) cell.getNumericCellValue());
                            }else if(cell.getCellType()== CellType.FORMULA){

                                CellValue cellValue = evaluator.evaluate(cell);
                                projetMINT.setBudget_n1((long) cellValue.getNumberValue());
                            }
                            break;
                        case 11:
                            if (cell.getCellType() == CellType.NUMERIC) {
                                projetMINT.setBudget_n2((long) cell.getNumericCellValue());
                            }else if(cell.getCellType()== CellType.FORMULA){

                                CellValue cellValue = evaluator.evaluate(cell);
                                projetMINT.setBudget_n2((long) cellValue.getNumberValue());
                            }
                            break;
                        case 12:
                            if (cell.getCellType() == CellType.STRING) {
                                projetMINT.setPrestataire(cell.getStringCellValue());
                            }
                            break;
                        case 13:
                            if (cell.getCellType() == CellType.STRING) {
                                projetMINT.setObservation(cell.getStringCellValue());
                            }
                            break;
                        default:
                            // Handle unknown columns or ignore them
                            break;
                    }
                }
            }

            list.add(projetMINT);
        }

        projetRepository.saveAll(list);


        if (workbook != null) {
            workbook.close();
        }
        return new MessageDTO("succes","Importation réussie");

    }

    private String getCellLetter(Cell cell) {
        // Convertir l'index de colonne en lettre (A, B, C, ...)
        StringBuilder columnLetter = new StringBuilder();
        int dividend = cell.getColumnIndex() + 1;
        int modulo;
        int indexRow=cell.getRowIndex()+1;

        while (dividend > 0) {
            modulo = (dividend - 1) % 26;
            columnLetter.insert(0, (char) (65 + modulo));
            dividend = (dividend - modulo) / 26;
        }

        return columnLetter.toString()+String.valueOf(indexRow);
    }

    public boolean isCellMerge(Sheet sheet, Cell cell){

        for(CellRangeAddress mergedERegion: sheet.getMergedRegions()){
            if(mergedERegion.isInRange(cell)){
                return true;
            }
        }

        return false;
    }
}


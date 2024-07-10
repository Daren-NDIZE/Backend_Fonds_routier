package com.api.backend.Fonds_routier.controller;

import com.api.backend.Fonds_routier.DTO.MessageDTO;
import com.api.backend.Fonds_routier.DTO.SuiviDTO;
import com.api.backend.Fonds_routier.enums.Ordonnateur;
import com.api.backend.Fonds_routier.enums.ProgrammeStatut;
import com.api.backend.Fonds_routier.model.*;
import com.api.backend.Fonds_routier.service.PayementService;
import com.api.backend.Fonds_routier.service.ProgrammeService;
import com.api.backend.Fonds_routier.service.ProjetService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@RestController
public class ProjetController {

    @Autowired
    ProjetService projetService;
    @Autowired
    ProgrammeService programmeService;
    @Autowired
    PayementService payementService;
    @Autowired
    JwtDecoder jwtDecoder;

    @PostMapping("/addProjetToProgrammeMINTP/{id}")
    public ResponseEntity<MessageDTO> addProjetMINTP(@PathVariable(value = "id") long id, @RequestBody ProjetMINTP projet){

        if(projet.getProjet()=="" ||projet.getRegion()==null ||projet.getTtc()==0 || projet.getCommune()==""
             || projet.getDepartement()=="" ||projet.getBudget_n()==0 || projet.getObservation()=="") {

            return ResponseEntity.ok(new MessageDTO("erreur","veuillez remplir tous les champs"));
        }

        Programme programme=programmeService.findProgramme(id);
        if(programme==null){
            return ResponseEntity.ok(new MessageDTO("erreur","programme inexistant"));
        }
        if(programme.getStatut()== ProgrammeStatut.VALIDER || programme.getStatut()== ProgrammeStatut.SOUMIS){

            return ResponseEntity.ok(new MessageDTO("erreur","impossible, programme en cours de traitement"));
        }
        projetService.saveProjet(programme,projet);
        return ResponseEntity.ok(new MessageDTO("succes","projet enregistré avec succès"));

    }

    @PostMapping("/addProjetToProgrammeMINT/{id}")
    public ResponseEntity<MessageDTO> addProjetMINT(@PathVariable(value = "id") long id, @RequestBody ProjetMINT projet){

        if(projet.getRegion()==null || projet.getMission()==null || projet.getObjectif()==null ||
                projet.getTtc()==0 ||  projet.getBudget_n()==0 || projet.getOrdonnateur()==null )
        {
            return ResponseEntity.ok(new MessageDTO("erreur","veuillez remplir tous les champs"));
        }

        Programme programme=programmeService.findProgramme(id);
        if(programme==null){
            return ResponseEntity.ok(new MessageDTO("erreur","programme inexistant"));
        }

        if(programme.getStatut()== ProgrammeStatut.VALIDER || programme.getStatut()== ProgrammeStatut.SOUMIS){

            return ResponseEntity.ok(new MessageDTO("erreur","impossible, programme en cours de traitement"));
        }

        projetService.saveProjet(programme,projet);
        return ResponseEntity.ok(new MessageDTO("succes","projet enregistrer avec succes"));

    }

    @PostMapping("/addProjetToProgrammeMINHDU/{id}")
    public ResponseEntity<MessageDTO> addProjetMINDHU(@PathVariable(value = "id") long id, @RequestBody ProjetMINHDU projet){

        if(projet.getRegion()==null || projet.getVille()==null || projet.getTroçon()==null ||
                projet.getTtc()==0 ||  projet.getBudget_n()==0 || projet.getOrdonnateur()==null )
        {
            return ResponseEntity.ok(new MessageDTO("erreur","veuillez remplir tous les champs"));
        }

        Programme programme=programmeService.findProgramme(id);
        if(programme==null){
            return ResponseEntity.ok(new MessageDTO("erreur","programme inexistant"));
        }

        if(programme.getStatut()== ProgrammeStatut.VALIDER || programme.getStatut()== ProgrammeStatut.SOUMIS){

            return ResponseEntity.ok(new MessageDTO("erreur","impossible, programme en cours de traitement"));
        }

        projetService.saveProjet(programme,projet);
        return ResponseEntity.ok(new MessageDTO("succes","projet enregistrer avec succes"));

    }

    @PutMapping("/updateProjetMINTP/{id}")
    public ResponseEntity<MessageDTO> updateProjetMINTP(@PathVariable(value = "id") long id, @RequestBody ProjetMINTP update){

        if(update.getProjet()=="" ||update.getRegion()==null ||update.getTtc()==0 || update.getCommune()==""
                || update.getDepartement()=="" ||update.getBudget_n()==0 || update.getObservation()=="") {

            return ResponseEntity.ok(new MessageDTO("erreur","veuillez remplir tous les champs"));
        }

        Projet projet=projetService.findProjet(id);
        if(projet==null){
            return ResponseEntity.ok(new MessageDTO("erreur","projet inexistant"));
        }

        Programme programme=projet.getProgramme();
        if(programme.getStatut()== ProgrammeStatut.VALIDER || programme.getStatut()== ProgrammeStatut.SOUMIS){

            return ResponseEntity.ok(new MessageDTO("erreur","impossible, programme en cours de traitement"));
        }

        projetService.updateProjet(projet,update);
        return ResponseEntity.ok(new MessageDTO("succes","projet modifié avec succes"));

    }

    @PutMapping("/updateProjetMINHDU/{id}")
    public ResponseEntity<MessageDTO> updateProjetMINDHU(@PathVariable(value = "id") long id, @RequestBody ProjetMINHDU update){

        if( update.getRegion()==null || update.getVille()==null || update.getTroçon()==null ||
                update.getTtc()==0 ||  update.getBudget_n()==0 || update.getOrdonnateur()==null )
        {
            return ResponseEntity.ok(new MessageDTO("erreur","veuillez remplir tous les champs"));
        }

        Projet projet=projetService.findProjet(id);
        if(projet==null){
            return ResponseEntity.ok(new MessageDTO("erreur","projet inexistant"));
        }

        Programme programme=projet.getProgramme();
        if(programme.getStatut()== ProgrammeStatut.VALIDER || programme.getStatut()== ProgrammeStatut.SOUMIS){

            return ResponseEntity.ok(new MessageDTO("erreur","impossible, programme en cours de traitement"));
        }

        projetService.updateProjet(projet,update);
        return ResponseEntity.ok(new MessageDTO("succes","projet modifié avec succes"));

    }

    @PutMapping("/updateProjetMINT/{id}")
    public ResponseEntity<MessageDTO> updateProjetMINT(@PathVariable(value = "id") long id, @RequestBody ProjetMINT update){

        if( update.getRegion()==null || update.getMission()==null || update.getObjectif()==null ||
                update.getTtc()==0 ||  update.getBudget_n()==0 || update.getOrdonnateur()==null )
        {
            return ResponseEntity.ok(new MessageDTO("erreur","veuillez remplir tous les champs"));
        }

        Projet projet=projetService.findProjet(id);
        if(projet==null){
            return ResponseEntity.ok(new MessageDTO("erreur","projet inexistant"));
        }

        Programme programme=projet.getProgramme();

        if(programme.getStatut()== ProgrammeStatut.VALIDER || programme.getStatut()== ProgrammeStatut.SOUMIS){

            return ResponseEntity.ok(new MessageDTO("erreur","impossible, programme en cours de traitement"));
        }

        projetService.updateProjet(projet,update);
        return ResponseEntity.ok(new MessageDTO("succes","projet modifié avec succes"));

    }

    @DeleteMapping("/deleteProjet/{id}")
    public ResponseEntity<MessageDTO> deleteProjetMINTP(@PathVariable(value = "id") long id, @RequestHeader("Authorization") String token){

        Jwt jwt=jwtDecoder.decode(token.substring(7));
        Projet projet=projetService.findProjet(id);
        if(projet==null){
            return ResponseEntity.ok(new MessageDTO("erreur","projet inexistant"));
        }

        Programme programme=projet.getProgramme();

        if( !(programme.getOrdonnateur().equals(Ordonnateur.valueOf(jwt.getClaim("role")))) ){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageDTO("erreur","accès refusé"));
        }
        if(programme.getStatut()== ProgrammeStatut.VALIDER || programme.getStatut()== ProgrammeStatut.SOUMIS){

            return ResponseEntity.ok(new MessageDTO("erreur","impossible, programme en cours de traitement"));
        }

        projetService.deleteProjet(id);
        return ResponseEntity.ok(new MessageDTO("succes","projet supprimé avec succès"));

    }

    @GetMapping("/projet/{id}")
    public ResponseEntity getProjetById(@PathVariable(value = "id") long id){

        Projet projet=projetService.findProjet(id);
        if(projet==null){
            return ResponseEntity.ok(new MessageDTO("erreur","projet inexistant"));
        }

        return ResponseEntity.ok(projet);
    }

    @PostMapping("/suiviProjet/{id}")
    public ResponseEntity<MessageDTO> suivProjet(@PathVariable(value = "id") long id, @ModelAttribute SuiviDTO suiviDTO) throws IOException, CloneNotSupportedException {

        Projet projet= projetService.findProjet(id);
        if(projet==null){
            return ResponseEntity.ok(new MessageDTO("erreur","Projet inexistant"));
        }

        Programme programme=projet.getProgramme();
        if(programme.getStatut()!= ProgrammeStatut.VALIDER ){
            return ResponseEntity.ok(new MessageDTO("erreur","Impossible, programme en cours de traitement"));
        }
        if(suiviDTO.getStatut().equals("Visé") && (projet.getSuivi()==null || projet.getSuivi().getEngagement()==0) ){

            return ResponseEntity.ok(new MessageDTO("erreur","Impossible,car le projet n'a pas été engagé"));
        }

        if(suiviDTO.getFile()!=null){
            String extention= FilenameUtils.getExtension(suiviDTO.getFile().getOriginalFilename()) ;

            if(!extention.equals("pdf")){
                return ResponseEntity.ok(new MessageDTO("erreur","le type de fichier que vous avez soumis est incorrect"));
            }

            File folder= new File("C:\\Users\\Daren NDIZE\\Desktop\\FR\\Back end\\Backend_Fonds_routier\\src\\main\\resources\\static\\bordereau\\"+programme.getOrdonnateur()+"\\");

            if(!folder.exists()){
                folder.mkdirs();
            }
            final Path path = Paths.get("C:\\Users\\Daren NDIZE\\Desktop\\FR\\Back end\\Backend_Fonds_routier\\src\\main\\resources\\static\\bordereau\\"+programme.getOrdonnateur()+"\\marché_"+projet.getId()+".pdf");
            Files.write(path,suiviDTO.getFile().getBytes());
            projet.setBordereau("bordereau/"+programme.getOrdonnateur()+"/marché_"+projet.getId()+".pdf");
        }

        projetService.setSuivi(projet, suiviDTO);
        return ResponseEntity.ok(new MessageDTO("succes","Les informations ont été bien enregistrés"));

    }

    @GetMapping("/projet/getBordereau/{id}")
    public ResponseEntity getResolution(@PathVariable(value = "id") long id) throws IOException {

        Projet projet =projetService.findProjet(id);

        if(projet==null){
            return ResponseEntity.ok(new MessageDTO("erreur","projet inexistant"));
        }
        if(projet.getBordereau()==null){
            return ResponseEntity.ok(new MessageDTO("erreur","aucun fichier disponible"));
        }

        Path path=Paths.get("C:/Users/Daren NDIZE/Desktop/FR/Back end/Backend_Fonds_routier/src/main/resources/static/"+projet.getBordereau());

        if(!Files.exists(path)){
            return ResponseEntity.ok(new MessageDTO("erreur","fichier introuvable"));
        }
        byte[] pdf=Files.readAllBytes(path);

        HttpHeaders header=new HttpHeaders();
        header.setContentType(MediaType.parseMediaType("application/pdf"));
        header.setContentLength(pdf.length);

        return new ResponseEntity(pdf,header, HttpStatus.OK);
    }

    @DeleteMapping("/deletePrevisionProjet/{id}")
    public ResponseEntity<MessageDTO> deletePrevisionProjet(@PathVariable(value = "id") long id){

        Projet projet=projetService.findProjet(id);

        if(projet==null){
            return ResponseEntity.ok(new MessageDTO("erreur","projet inexistant"));
        }

        projetService.deleteProjet(id);
        return ResponseEntity.ok(new MessageDTO("succes","projet supprimé avec succès"));
    }

    @PostMapping("/projet/savePayement/{id}")
    public  ResponseEntity<MessageDTO> savePayement(@PathVariable(value = "id") long id, @RequestBody Payement payement){

        Projet projet =projetService.findProjet(id);
        if(projet==null){
            return ResponseEntity.ok(new MessageDTO("erreur","projet inexistant"));
        }
        if(projet.getBordereau()==null){
            return ResponseEntity.ok(new MessageDTO("erreur","impossible, car le projet n'est pas validé"));
        }
        if(payement.getM_HTVA()<=0 || payement.getDecompte()==null || ( payement.getAir()!=5.5 && payement.getAir()!=2.2 ) ){

            return ResponseEntity.ok(new MessageDTO("erreur","veuillez remplir correctement les champs"));
        }

        payementService.savePayement(projet,payement);
        return ResponseEntity.ok(new MessageDTO("succes","payement enregistré avec succès"));

    }

    @PostMapping("/projet/saveSuiviTravaux/{id}")
    public ResponseEntity<MessageDTO> saveSuiviTravaux(@PathVariable(value = "id") long id, @RequestBody SuiviTravaux suiviTravaux){

        Projet projet =projetService.findProjet(id);
        if(projet==null){
            return ResponseEntity.ok(new MessageDTO("erreur","projet inexistant"));
        }
        if(projet.getBordereau()==null){
            return ResponseEntity.ok(new MessageDTO("erreur","impossible, car le projet n'est pas validé"));
        }
        if( suiviTravaux.getDescription()==null || suiviTravaux.getTauxAvancement()<=0 || suiviTravaux.getTauxConsommation()<=0){

            return ResponseEntity.ok(new MessageDTO("erreur","veuillez remplir tous les champs"));
        }

        projetService.saveSuiviTravaux(projet,suiviTravaux);

        return ResponseEntity.ok(new MessageDTO("succes","suivi des travaux enregistré avec succès"));

    }
}

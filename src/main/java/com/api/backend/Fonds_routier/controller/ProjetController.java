package com.api.backend.Fonds_routier.controller;

import com.api.backend.Fonds_routier.DTO.MessageDTO;
import com.api.backend.Fonds_routier.DTO.SuiviDTO;
import com.api.backend.Fonds_routier.enums.Ordonnateur;
import com.api.backend.Fonds_routier.enums.ProgrammeStatut;
import com.api.backend.Fonds_routier.enums.ProgrammeType;
import com.api.backend.Fonds_routier.model.*;
import com.api.backend.Fonds_routier.service.*;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

@RestController
public class ProjetController {

    @Autowired
    ProjetService projetService;
    @Autowired
    AccountService accountService;
    @Autowired
    ProgrammeService programmeService;
    @Autowired
    PayementService payementService;
    @Autowired
    RoleService roleService;
    @Autowired
    JwtDecoder jwtDecoder;

    ClassPathResource resource=new ClassPathResource("static/");
    String chemin=resource.getURI().getPath().substring(1);

    public ProjetController() throws IOException {
    }


    //Gestion de la programmation



    // Saisie des projets

    @PostMapping("/addProjetToProgrammeMINTP/{id}")
    public ResponseEntity<MessageDTO> addProjetMINTP(@PathVariable(value = "id") long id,@RequestHeader("Authorization") String token, @RequestBody ProjetMINTP projet){

        List<String> categorie=List.of("PROJET A GESTION CENTRALE","PROJET A GESTION REGIONALE","PROJET A GESTION COMMUNALE");
        List<String> typeTravaux=List.of("ROUTE EN TERRE","ROUTE BITUMÉE","OUVRAGE D'ART");
        Jwt jwt=jwtDecoder.decode(token.substring(7));
        Role role= roleService.getRoleByRoleName(jwt.getClaim("role"));

        Programme programme=programmeService.findProgramme(id);

        if(programme==null){
            return ResponseEntity.ok(new MessageDTO("erreur","programme inexistant"));
        }

        if(programme.getType()==ProgrammeType.REPORT){

            if(!roleService.containsPermission(role.getPermissions(),"GESTION DES PROGRAMMES REPORTS")){
                return ResponseEntity.ok(new MessageDTO("erreur","Vous n'avez pas accès à cette fonctionnalité"));
            }

        }else{
            if(!roleService.containsPermission(role.getPermissions(),"GESTION DE LA PROGRAMMATION")){
                return ResponseEntity.ok(new MessageDTO("erreur","Vous n'avez pas accès à cette fonctionnalité"));
            }

            if(!programme.getOrdonnateur().equals( Ordonnateur.valueOf(jwt.getClaim("structure")) )){
                return ResponseEntity.ok(new MessageDTO("erreur","accès refusé"));
            }

            if(programme.getStatut()== ProgrammeStatut.VALIDER || programme.getStatut()== ProgrammeStatut.SOUMIS){

                return ResponseEntity.ok(new MessageDTO("erreur","impossible, programme en cours de traitement"));
            }
        }


        if(projet.getProjet()=="" ||projet.getRegion()==null ||projet.getTtc()==0 || projet.getCommune()=="" || !categorie.contains(projet.getCategorie())
            || !typeTravaux.contains(projet.getType_travaux()) || projet.getDepartement()=="" ||projet.getBudget_n()==0 || projet.getObservation()=="") {

            return ResponseEntity.ok(new MessageDTO("erreur","veuillez remplir correctement tous les champs"));
        }

        if(programme.getStatut()==ProgrammeStatut.CLOTURER){

            return ResponseEntity.ok(new MessageDTO("erreur","impossible,le programme est cloturé"));
        }

        projetService.saveProjet(programme,projet);

        if(programme.getType()==ProgrammeType.REPORT){
            programme.setBudget(programme.getBudget()+projet.getBudget_n());
            programmeService.saveProgramme(programme);
        }

        return ResponseEntity.ok(new MessageDTO("succes","projet enregistré avec succès"));
    }

    @PostMapping("/addProjetToProgrammeMINT/{id}")
    public ResponseEntity<MessageDTO> addProjetMINT(@PathVariable(value = "id") long id,@RequestHeader("Authorization") String token, @RequestBody ProjetMINT projet){

        Jwt jwt=jwtDecoder.decode(token.substring(7));
        List<String> gestionnaire=List.of("MINT","MAIRE");
        Role role= roleService.getRoleByRoleName(jwt.getClaim("role"));

        Programme programme=programmeService.findProgramme(id);
        if(programme==null){
            return ResponseEntity.ok(new MessageDTO("erreur","programme inexistant"));
        }

        if(programme.getType()==ProgrammeType.REPORT){

            if(!roleService.containsPermission(role.getPermissions(),"GESTION DES PROGRAMMES REPORTS")){
                return ResponseEntity.ok(new MessageDTO("erreur","Vous n'avez pas accès à cette fonctionnalité"));
            }

        }else{
            if(!roleService.containsPermission(role.getPermissions(),"GESTION DE LA PROGRAMMATION")){
                return ResponseEntity.ok(new MessageDTO("erreur","Vous n'avez pas accès à cette fonctionnalité"));
            }

            if(!programme.getOrdonnateur().equals(Ordonnateur.valueOf(jwt.getClaim("structure")) )){
                return ResponseEntity.ok(new MessageDTO("erreur","accès refusé"));
            }

            if(programme.getStatut()== ProgrammeStatut.VALIDER || programme.getStatut()== ProgrammeStatut.SOUMIS){

                return ResponseEntity.ok(new MessageDTO("erreur","impossible, programme en cours de traitement"));
            }
        }

        if(projet.getRegion()==null || projet.getMission()=="" || projet.getObjectif()=="" ||projet.getDepartement()=="" ||
            projet.getCommune()=="" ||  projet.getTtc()==0 ||  projet.getBudget_n()==0 || !gestionnaire.contains(projet.getOrdonnateur( )) )
        {
            return ResponseEntity.ok(new MessageDTO("erreur","veuillez remplir correctement les champs"));
        }

        if(programme.getStatut()==ProgrammeStatut.CLOTURER){

            return ResponseEntity.ok(new MessageDTO("erreur","impossible,le programme est cloturé"));
        }

        projetService.saveProjet(programme,projet);

        if(programme.getType()==ProgrammeType.REPORT){
            programme.setBudget(programme.getBudget()+projet.getBudget_n());
            programmeService.saveProgramme(programme);
        }
        return ResponseEntity.ok(new MessageDTO("succes","projet enregistré avec succes"));

    }

    @PostMapping("/addProjetToProgrammeMINHDU/{id}")
    public ResponseEntity<MessageDTO> addProjetMINDHU(@PathVariable(value = "id") long id,@RequestHeader("Authorization") String token, @RequestBody ProjetMINHDU projet){
        Jwt jwt=jwtDecoder.decode(token.substring(7));
        List<String> typeTravaux=List.of("ENTRETIEN DES VOIRIES URBAINES","ETUDES ET CONTROLES TECHNIQUES");
        List<String> gestionnaire=List.of("MINHDU","MAIRE");
        Role role= roleService.getRoleByRoleName(jwt.getClaim("role"));

        Programme programme=programmeService.findProgramme(id);
        if(programme==null){
            return ResponseEntity.ok(new MessageDTO("erreur","programme inexistant"));
        }

        if(programme.getType()==ProgrammeType.REPORT){

            if(!roleService.containsPermission(role.getPermissions(),"GESTION DES PROGRAMMES REPORTS")){
                return ResponseEntity.ok(new MessageDTO("erreur","Vous n'avez pas accès à cette fonctionnalité"));
            }

        }else{
            if(!roleService.containsPermission(role.getPermissions(),"GESTION DE LA PROGRAMMATION")){
                return ResponseEntity.ok(new MessageDTO("erreur","Vous n'avez pas accès à cette fonctionnalité"));
            }

            if(!programme.getOrdonnateur().equals(Ordonnateur.valueOf(jwt.getClaim("structure")) )){
                return ResponseEntity.ok(new MessageDTO("erreur","accès refusé"));
            }

            if(programme.getStatut()== ProgrammeStatut.VALIDER || programme.getStatut()== ProgrammeStatut.SOUMIS){

                return ResponseEntity.ok(new MessageDTO("erreur","impossible, programme en cours de traitement"));
            }
        }

        if(projet.getRegion()==null || projet.getVille()==null || projet.getTroçon()==null || !typeTravaux.contains(projet.getType_travaux())
               || projet.getTtc()==0 ||  projet.getBudget_n()==0 || !gestionnaire.contains(projet.getOrdonnateur() ))
        {
            return ResponseEntity.ok(new MessageDTO("erreur","veuillez remplir correctement tous les champs"));
        }


        if(programme.getStatut()==ProgrammeStatut.CLOTURER){

            return ResponseEntity.ok(new MessageDTO("erreur","impossible,le programme est cloturé"));
        }

        projetService.saveProjet(programme,projet);

        if(programme.getType()==ProgrammeType.REPORT){
            programme.setBudget(programme.getBudget()+projet.getBudget_n());
            programmeService.saveProgramme(programme);
        }

        return ResponseEntity.ok(new MessageDTO("succes","projet enregistré avec succes"));
    }

    //Update des projet

    @PutMapping("/updateProjetMINTP/{id}")
    public ResponseEntity<MessageDTO> updateProjetMINTP(@PathVariable(value = "id") long id,@RequestHeader("Authorization") String token, @RequestBody ProjetMINTP update){

        List<String> categorie=List.of("PROJET A GESTION CENTRALE","PROJET A GESTION REGIONALE","PROJET A GESTION COMMUNALE");
        List<String> typeTravaux=List.of("ROUTE EN TERRE","ROUTE BITUMÉE","OUVRAGE D'ART");
        Jwt jwt=jwtDecoder.decode(token.substring(7));
        Role role= roleService.getRoleByRoleName(jwt.getClaim("role"));

        Projet projet=projetService.findProjet(id);
        if(projet==null){
            return ResponseEntity.ok(new MessageDTO("erreur","projet inexistant"));
        }

        Programme programme=projet.getProgramme();

        if(programme.getType()==ProgrammeType.REPORT){

            if(!roleService.containsPermission(role.getPermissions(),"GESTION DES PROGRAMMES REPORTS")){
                return ResponseEntity.ok(new MessageDTO("erreur","Vous n'avez pas accès à cette fonctionnalité"));
            }

        }else{

            if(!roleService.containsPermission(role.getPermissions(),"GESTION DE LA PROGRAMMATION")){
                return ResponseEntity.ok(new MessageDTO("erreur","Vous n'avez pas accès à cette fonctionnalité"));
            }

            if(!programme.getOrdonnateur().equals(Ordonnateur.valueOf(jwt.getClaim("structure")) )){
                return ResponseEntity.ok(new MessageDTO("erreur","accès refusé"));
            }

            if(programme.getStatut()== ProgrammeStatut.VALIDER || programme.getStatut()== ProgrammeStatut.SOUMIS){

                return ResponseEntity.ok(new MessageDTO("erreur","impossible, programme en cours de traitement"));
            }
        }

        if(update.getProjet()=="" ||update.getRegion()==null ||update.getTtc()==0 || update.getCommune()=="" ||!categorie.contains(update.getCategorie())
                || !typeTravaux.contains(update.getType_travaux()) || update.getDepartement()=="" ||update.getBudget_n()==0 || update.getObservation()=="") {

            return ResponseEntity.ok(new MessageDTO("erreur","veuillez remplir tous les champs"));
        }

        if(programme.getStatut()==ProgrammeStatut.CLOTURER){

            return ResponseEntity.ok(new MessageDTO("erreur","impossible,le programme est cloturé"));
        }

        projetService.updateProjet(projet,update);

        if(programme.getType()==ProgrammeType.REPORT) {
            programme.setBudget(programme.getBudget() + (update.getBudget_n() - projet.getBudget_n()));
            programmeService.saveProgramme(programme);
        }

        return ResponseEntity.ok(new MessageDTO("succes","projet modifié avec succès"));

    }

    @PutMapping("/updateProjetMINHDU/{id}")
    public ResponseEntity<MessageDTO> updateProjetMINDHU(@PathVariable(value = "id") long id,@RequestHeader("Authorization") String token, @RequestBody ProjetMINHDU update){

        Jwt jwt=jwtDecoder.decode(token.substring(7));
        List<String> typeTravaux=List.of("ENTRETIEN DES VOIRIES URBAINES","ETUDES ET CONTROLES TECHNIQUES");
        List<String> gestionnaire=List.of("MINHDU","MAIRE");
        Role role= roleService.getRoleByRoleName(jwt.getClaim("role"));

        Projet projet=projetService.findProjet(id);
        if(projet==null){
            return ResponseEntity.ok(new MessageDTO("erreur","projet inexistant"));
        }

        Programme programme=projet.getProgramme();

        if(programme.getType()==ProgrammeType.REPORT){

            if(!roleService.containsPermission(role.getPermissions(),"GESTION DES PROGRAMMES REPORTS")){
                return ResponseEntity.ok(new MessageDTO("erreur","Vous n'avez pas accès à cette fonctionnalité"));
            }

        }else{

            if(!roleService.containsPermission(role.getPermissions(),"GESTION DE LA PROGRAMMATION")){
                return ResponseEntity.ok(new MessageDTO("erreur","Vous n'avez pas accès à cette fonctionnalité"));
            }

            if(!programme.getOrdonnateur().equals(Ordonnateur.valueOf(jwt.getClaim("structure")) )){
                return ResponseEntity.ok(new MessageDTO("erreur","accès refusé"));
            }

            if(programme.getStatut()== ProgrammeStatut.VALIDER || programme.getStatut()== ProgrammeStatut.SOUMIS){
                return ResponseEntity.ok(new MessageDTO("erreur","impossible, programme en cours de traitement"));
            }
        }

        if( update.getRegion()==null || update.getVille()==null || update.getTroçon()==null || !typeTravaux.contains(update.getType_travaux())
                ||  update.getTtc()==0 ||  update.getBudget_n()==0 || !gestionnaire.contains(update.getOrdonnateur()) )
        {
            return ResponseEntity.ok(new MessageDTO("erreur","veuillez remplir tous les champs"));
        }

        if(programme.getStatut()==ProgrammeStatut.CLOTURER){

            return ResponseEntity.ok(new MessageDTO("erreur","impossible,le programme est cloturé"));
        }

        projetService.updateProjet(projet,update);

        if(programme.getType()==ProgrammeType.REPORT) {
            programme.setBudget(programme.getBudget() + (update.getBudget_n() - projet.getBudget_n()));
            programmeService.saveProgramme(programme);
        }

        return ResponseEntity.ok(new MessageDTO("succes","projet modifié avec succès"));
    }

    @PutMapping("/updateProjetMINT/{id}")
    public ResponseEntity<MessageDTO> updateProjetMINT(@PathVariable(value = "id") long id,@RequestHeader("Authorization") String token, @RequestBody ProjetMINT update){

        Jwt jwt=jwtDecoder.decode(token.substring(7));
        List<String> gestionnaire=List.of("MINT","MAIRE");
        Role role= roleService.getRoleByRoleName(jwt.getClaim("role"));

        Projet projet=projetService.findProjet(id);
        if(projet==null){
            return ResponseEntity.ok(new MessageDTO("erreur","projet inexistant"));
        }

        Programme programme=projet.getProgramme();

        if(programme.getType()==ProgrammeType.REPORT){

            if(!roleService.containsPermission(role.getPermissions(),"GESTION DES PROGRAMMES REPORTS")){
                return ResponseEntity.ok(new MessageDTO("erreur","Vous n'avez pas accès à cette fonctionnalité"));
            }

        }else{

            if(!roleService.containsPermission(role.getPermissions(),"GESTION DE LA PROGRAMMATION")){
                return ResponseEntity.ok(new MessageDTO("erreur","Vous n'avez pas accès à cette fonctionnalité"));
            }

            if(!programme.getOrdonnateur().equals(Ordonnateur.valueOf(jwt.getClaim("structure")) )){
                return ResponseEntity.ok(new MessageDTO("erreur","accès refusé"));
            }

            if(programme.getStatut()== ProgrammeStatut.VALIDER || programme.getStatut()== ProgrammeStatut.SOUMIS){

                return ResponseEntity.ok(new MessageDTO("erreur","impossible, programme en cours de traitement"));
            }
        }

        if( update.getRegion()==null || update.getMission()==null || update.getObjectif()==null || update.getDepartement()==""||
                update.getCommune()=="" ||  update.getTtc()==0 ||  update.getBudget_n()==0 || !gestionnaire.contains(update.getOrdonnateur()) )
        {
            return ResponseEntity.ok(new MessageDTO("erreur","veuillez remplir tous les champs"));
        }

        if(programme.getStatut()==ProgrammeStatut.CLOTURER){
            return ResponseEntity.ok(new MessageDTO("erreur","impossible,le programme est cloturé"));
        }

        projetService.updateProjet(projet,update);

        if(programme.getType()==ProgrammeType.REPORT) {
            programme.setBudget(programme.getBudget() + (update.getBudget_n() - projet.getBudget_n()));
            programmeService.saveProgramme(programme);
        }

        return ResponseEntity.ok(new MessageDTO("succes","projet modifié avec succes"));
    }

    // Delete des projets

    @DeleteMapping("/deleteProjet/{id}")
    public ResponseEntity<MessageDTO> deleteProjet(@PathVariable(value = "id") long id, @RequestHeader("Authorization") String token){

        Jwt jwt=jwtDecoder.decode(token.substring(7));
        Projet projet=projetService.findProjet(id);
        Role role= roleService.getRoleByRoleName(jwt.getClaim("role"));

        if(!roleService.containsPermission(role.getPermissions(),"GESTION DE LA PROGRAMMATION")){
            return ResponseEntity.ok(new MessageDTO("erreur","Vous n'avez pas accès à cette fonctionnalité"));
        }

        if(projet==null){
            return ResponseEntity.ok(new MessageDTO("erreur","projet inexistant"));
        }

        Programme programme=projet.getProgramme();

        if(!programme.getOrdonnateur().equals(Ordonnateur.valueOf(jwt.getClaim("structure")) )){
            return ResponseEntity.ok(new MessageDTO("erreur","accès refusé"));
        }

        if(programme.getStatut()== ProgrammeStatut.VALIDER || programme.getStatut()== ProgrammeStatut.SOUMIS){

            return ResponseEntity.ok(new MessageDTO("erreur","impossible, programme en cours de traitement"));
        }
        if(programme.getStatut()== ProgrammeStatut.CLOTURER){

            return ResponseEntity.ok(new MessageDTO("erreur","impossible,Ce programme est cloturé"));
        }
        if(programme.getType()==ProgrammeType.AJUSTER){

            return ResponseEntity.ok(new MessageDTO("erreur","impossible de supprimer un projet dans le programme ajusté"));
        }

        if(projet.getSuivi()!=null){

            return ResponseEntity.ok(new MessageDTO("erreur","impossible de supprimer ce projet car il a été engagé"));
        }

        projetService.deleteProjet(id);
        return ResponseEntity.ok(new MessageDTO("succes","projet supprimé avec succès"));
    }




    // Gestion de la provision de reserve

    @Secured("DCO")
    @PostMapping("/addProjetToProvisionMINTP/{id}")
    public ResponseEntity<MessageDTO> addProvisionMINTP(@PathVariable(value = "id") long id, @RequestBody ProjetMINTP projet ,@RequestHeader("Authorization") String token){

        Jwt jwt=jwtDecoder.decode(token.substring(7));
        Role role= roleService.getRoleByRoleName(jwt.getClaim("role"));

        if(!roleService.containsPermission(role.getPermissions(),"GESTION DE LA PROVISION DE RESERVE")){
            return ResponseEntity.ok(new MessageDTO("erreur","Vous n'avez pas accès à cette fonctionnalité"));
        }

        List<String> categorie=List.of("PROJET A GESTION CENTRALE","PROJET A GESTION REGIONALE","PROJET A GESTION COMMUNALE");
        List<String> typeTravaux=List.of("ROUTE EN TERRE","ROUTE BITUMÉE","OUVRAGE D'ART");

        if(projet.getProjet()=="" ||projet.getRegion()==null ||projet.getTtc()==0 || projet.getCommune()=="" || !categorie.contains(projet.getCategorie())
           || !typeTravaux.contains(projet.getType_travaux()) || projet.getDepartement()=="" ||projet.getBudget_n()==0 || projet.getObservation()=="") {

            return ResponseEntity.ok(new MessageDTO("erreur","veuillez remplir correctement tous les champs"));
        }

        Programme programme=programmeService.findProgramme(id);
        if(programme==null){
            return ResponseEntity.ok(new MessageDTO("erreur","programme inexistant"));
        }

        if(programme.getStatut()==ProgrammeStatut.CLOTURER){

            return ResponseEntity.ok(new MessageDTO("erreur","impossible,le programme est cloturé"));
        }

        projetService.saveProvisionProjet(programme,projet);
        return ResponseEntity.ok(new MessageDTO("succes","projet enregistré avec succès"));

    }

    @Secured("DCO")
    @PostMapping("/addProjetToProvisionMINHDU/{id}")
    public ResponseEntity<MessageDTO> addProvisionMINHDU(@PathVariable(value = "id") long id, @RequestBody ProjetMINHDU projet ,@RequestHeader("Authorization") String token){

        Jwt jwt=jwtDecoder.decode(token.substring(7));
        Role role= roleService.getRoleByRoleName(jwt.getClaim("role"));

        if(!roleService.containsPermission(role.getPermissions(),"GESTION DE LA PROVISION DE RESERVE")){
            return ResponseEntity.ok(new MessageDTO("erreur","Vous n'avez pas accès à cette fonctionnalité"));
        }

        List<String> typeTravaux=List.of("ENTRETIEN DES VOIRIES URBAINES","ETUDES ET CONTROLES TECHNIQUES");
        List<String> gestionnaire=List.of("MINHDU","MAIRE");

        if(projet.getRegion()==null || projet.getVille()==null || projet.getTroçon()==null || !typeTravaux.contains(projet.getType_travaux())
              ||  projet.getTtc()==0 ||  projet.getBudget_n()==0 || !gestionnaire.contains(projet.getOrdonnateur()) )
        {
            return ResponseEntity.ok(new MessageDTO("erreur","veuillez remplir tous les champs"));
        }

        Programme programme=programmeService.findProgramme(id);
        if(programme==null){
            return ResponseEntity.ok(new MessageDTO("erreur","programme inexistant"));
        }

        if(programme.getStatut()==ProgrammeStatut.CLOTURER){

            return ResponseEntity.ok(new MessageDTO("erreur","impossible,le programme est cloturé"));
        }

        projetService.saveProvisionProjet(programme,projet);
        return ResponseEntity.ok(new MessageDTO("succes","projet enregistré avec succès"));

    }

    @Secured("DCO")
    @PostMapping("/addProjetToProvisionMINT/{id}")
    public ResponseEntity<MessageDTO> addProvisionMINT(@PathVariable(value = "id") long id, @RequestBody ProjetMINT projet ,@RequestHeader("Authorization") String token){

        Jwt jwt=jwtDecoder.decode(token.substring(7));
        Role role= roleService.getRoleByRoleName(jwt.getClaim("role"));

        if(!roleService.containsPermission(role.getPermissions(),"GESTION DE LA PROVISION DE RESERVE")){
            return ResponseEntity.ok(new MessageDTO("erreur","Vous n'avez pas accès à cette fonctionnalité"));
        }

        List<String> gestionnaire=List.of("MINT","MAIRE");

        if(projet.getRegion()==null || projet.getMission()=="" || projet.getObjectif()=="" ||projet.getTtc()==0 || projet.getDepartement()=="" ||
            projet.getCommune()=="" || projet.getBudget_n()==0 || !gestionnaire.contains(projet.getOrdonnateur()))
        {
            return ResponseEntity.ok(new MessageDTO("erreur","veuillez remplir tous les champs"));
        }

        Programme programme=programmeService.findProgramme(id);
        if(programme==null){
            return ResponseEntity.ok(new MessageDTO("erreur","programme inexistant"));
        }

        if(programme.getStatut()==ProgrammeStatut.CLOTURER){

            return ResponseEntity.ok(new MessageDTO("erreur","impossible,le programme est cloturé"));
        }

        projetService.saveProvisionProjet(programme,projet);
        return ResponseEntity.ok(new MessageDTO("succes","projet enregistré avec succès"));

    }

    @Secured("DCO")
    @DeleteMapping("/deleteProvisionProjet/{id}")
    public ResponseEntity<MessageDTO> deletePrevisionProjet(@PathVariable(value = "id") long id ,@RequestHeader("Authorization") String token){

        Jwt jwt=jwtDecoder.decode(token.substring(7));
        Role role= roleService.getRoleByRoleName(jwt.getClaim("role"));

        if(!roleService.containsPermission(role.getPermissions(),"GESTION DE LA PROVISION DE RESERVE")){
            return ResponseEntity.ok(new MessageDTO("erreur","Vous n'avez pas accès à cette fonctionnalité"));
        }

        Projet projet=projetService.findProjet(id);

        if(projet==null){
            return ResponseEntity.ok(new MessageDTO("erreur","projet inexistant"));
        }
        if(projet.getFinancement().equals("NORMAL")){

            return ResponseEntity.ok(new MessageDTO("erreur","impossible, car ce projet n'est pas dans la provision"));
        }
        if(projet.getProgramme().getStatut()==ProgrammeStatut.CLOTURER){

            return ResponseEntity.ok(new MessageDTO("erreur","impossible, car ce programme est  cloturé"));
        }

        projetService.deleteProjet(id);
        return ResponseEntity.ok(new MessageDTO("succes","projet supprimé avec succès"));
    }




    // Gestion du report des crédits

    @Secured("DCO")
    @DeleteMapping("/deleteReportProjet/{id}")
    public ResponseEntity<MessageDTO> deleteReportProjet(@PathVariable(value = "id") long id ,@RequestHeader("Authorization") String token){

        Jwt jwt=jwtDecoder.decode(token.substring(7));
        Role role= roleService.getRoleByRoleName(jwt.getClaim("role"));

        if(!roleService.containsPermission(role.getPermissions(),"GESTION DES PROGRAMMES REPORTS")){
            return ResponseEntity.ok(new MessageDTO("erreur","Vous n'avez pas accès à cette fonctionnalité"));
        }

        Projet projet=projetService.findProjet(id);

        if(projet==null){
            return ResponseEntity.ok(new MessageDTO("erreur","projet inexistant"));
        }

        Programme programme=projet.getProgramme();

        if(programme.getType()!=ProgrammeType.REPORT ){

            return ResponseEntity.ok(new MessageDTO("erreur","impossible, car ce projet n'est pas dans le programme des reports"));
        }

        if(programme.getStatut()== ProgrammeStatut.CLOTURER){

            return ResponseEntity.ok(new MessageDTO("erreur","impossible,Ce programme est cloturé"));
        }

        projetService.deleteProjet(id);

        programme.setBudget(programme.getBudget()-projet.getBudget_n());
        programmeService.saveProgramme(programme);

        return ResponseEntity.ok(new MessageDTO("succes","projet supprimé avec succès"));
    }



    //////////////////////////////////////////////////////

    @GetMapping("/projet/{id}")
    public ResponseEntity<Object> getProjetById(@PathVariable(value = "id") long id){

        Projet projet=projetService.findProjet(id);
        if(projet==null){
            return ResponseEntity.ok(new MessageDTO("erreur","projet inexistant"));
        }

        return ResponseEntity.ok(projet);
    }

    @GetMapping("/projet/getBordereau/{id}")
    public ResponseEntity<Object> getResolution(@PathVariable(value = "id") long id) throws IOException {

        Projet projet =projetService.findProjet(id);

        if(projet==null){
            return ResponseEntity.ok(new MessageDTO("erreur","projet inexistant"));
        }
        if(projet.getBordereau()==null){
            return ResponseEntity.ok(new MessageDTO("erreur","aucun fichier disponible"));
        }

        Path path=Paths.get(chemin+projet.getBordereau());

        if(!Files.exists(path)){
            return ResponseEntity.ok(new MessageDTO("erreur","fichier introuvable"));
        }
        byte[] pdf=Files.readAllBytes(path);

        HttpHeaders header=new HttpHeaders();
        header.setContentType(MediaType.parseMediaType("application/pdf"));
        header.setContentLength(pdf.length);

        return new ResponseEntity(pdf,header, HttpStatus.OK);
    }



    // Traitement des projets

    @PostMapping("/suiviProjet/{id}")
    public ResponseEntity<MessageDTO> suivProjet(@PathVariable(value = "id") long id,@RequestHeader("Authorization") String token, @ModelAttribute SuiviDTO suiviDTO) throws IOException, CloneNotSupportedException {

        List<String>situation= List.of("Visé","Traitment DCO","Traitement DAF","En attente pour correction","Rejeté","Transmis pour visa");
        List<String>role= List.of("ACO","CO","DCO");

        Jwt jwt=jwtDecoder.decode(token.substring(7));

        Projet projet= projetService.findProjet(id);

        if(projet==null){
            return ResponseEntity.ok(new MessageDTO("erreur","Projet inexistant"));
        }
        if(!situation.contains(suiviDTO.getStatut())){

            return ResponseEntity.ok(new MessageDTO("erreur","Veuillez remplir correctement les champs"));
        }
        if(( suiviDTO.getStatut().equals("Transmis pour visa") || suiviDTO.getStatut().equals("Visé") ) && !role.contains(jwt.getClaim("role"))){
            return ResponseEntity.ok(new MessageDTO("erreur","Vous n'êtes pas autorisé à éffectuer cette opération"));

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

        if(suiviDTO.getStatut().equals("Transmis pour visa") && projet.getBudget_n() < suiviDTO.getEngagement() ){

            long total=programmeService.totalReserve(programme.getProjetList())+suiviDTO.getEngagement()-projet.getBudget_n();

            if(programme.getPrevision() < total){

                return ResponseEntity.ok(new MessageDTO("erreur","Impossible, car votre provision de réserve est insuffisante"));
            }
        }

        projetService.setSuivi(projet, suiviDTO);

        accountService.saveAction(new Action(0,jwt.getSubject(),jwt.getClaim("role"),suiviDTO.getStatut()+": PROJET DE LA REGION DU/DE "+projet.getRegion()+" DU "+programme.getIntitule(),new Date()));

        return ResponseEntity.ok(new MessageDTO("succes","Les informations ont été bien enregistrés"));

    }




    // Gestion des paiements

    @Secured({"DAF", "COMPTABLE FR"})
    @PostMapping("/projet/savePayement/{id}")
    public  ResponseEntity<MessageDTO> savePayement(@PathVariable(value = "id") long id, @RequestBody Payement payement ,@RequestHeader("Authorization") String token){

        Jwt jwt=jwtDecoder.decode(token.substring(7));
        Role role= roleService.getRoleByRoleName(jwt.getClaim("role"));

        if(!roleService.containsPermission(role.getPermissions(),"SAISIE DES PAIEMENTS")){
            return ResponseEntity.ok(new MessageDTO("erreur","Vous n'avez pas accès à cette fonctionnalité"));
        }

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

    @Secured({"DAF", "COMPTABLE FR"})
    @PutMapping("/projet/updatePayement/{id}")
    public  ResponseEntity<MessageDTO> updatePayement(@PathVariable(value = "id") long id, @RequestBody Payement update ,@RequestHeader("Authorization") String token){

        Jwt jwt=jwtDecoder.decode(token.substring(7));
        Role role= roleService.getRoleByRoleName(jwt.getClaim("role"));

        if(!roleService.containsPermission(role.getPermissions(),"SAISIE DES PAIEMENTS")){
            return ResponseEntity.ok(new MessageDTO("erreur","Vous n'avez pas accès à cette fonctionnalité"));
        }

        Payement payement= payementService.findPayement(id);

        if(payement==null){
            return ResponseEntity.ok(new MessageDTO("erreur","payement inexistant"));
        }

        if(payement.getM_HTVA()<=0 || payement.getDecompte()==null || ( payement.getAir()!=5.5 && payement.getAir()!=2.2 ) ){

            return ResponseEntity.ok(new MessageDTO("erreur","veuillez remplir correctement les champs"));
        }

        long j= (new Date().getTime()- payement.getDate().getTime())/86400000;

        if(j > 5){

            return ResponseEntity.ok(new MessageDTO("erreur","Impossible de modifier ce payement car il a été enregistré il y'a plus d'une semaine"));
        }

        payementService.updatePayement(payement,update);

        return ResponseEntity.ok(new MessageDTO("succes","payement modifié avec succès"));

    }

    @Secured({"DAF", "COMPTABLE FR"})
    @DeleteMapping("/projet/deletePayement/{id}")
    public  ResponseEntity<MessageDTO> deletePayement(@PathVariable(value = "id") long id ,@RequestHeader("Authorization") String token){

        Jwt jwt=jwtDecoder.decode(token.substring(7));
        Role role= roleService.getRoleByRoleName(jwt.getClaim("role"));

        if(!roleService.containsPermission(role.getPermissions(),"SAISIE DES PAIEMENTS")){
            return ResponseEntity.ok(new MessageDTO("erreur","Vous n'avez pas accès à cette fonctionnalité"));
        }

        Payement payement= payementService.findPayement(id);
        if(payement==null){
            return ResponseEntity.ok(new MessageDTO("erreur","payement inexistant"));
        }

        long j= (new Date().getTime()-payement.getDate().getTime())/86400000;
        if(j > 5){
            return ResponseEntity.ok(new MessageDTO("erreur","Impossible de supprimer ce payement car il a été enregistré il y'a plus d'une semaine"));
        }

        payementService.deletePayement(id);

        return ResponseEntity.ok(new MessageDTO("succes","payement supprimé avec succès"));

    }

    @Secured({"DAF", "COMPTABLE FR"})
    @PostMapping("/projet/importPayement/{id}")
    public  ResponseEntity<MessageDTO> importPayement(@PathVariable(value = "id") long id,@RequestParam("file") MultipartFile file ,@RequestHeader("Authorization") String token){

        Jwt jwt=jwtDecoder.decode(token.substring(7));
        Role role= roleService.getRoleByRoleName(jwt.getClaim("role"));

        if(!roleService.containsPermission(role.getPermissions(),"SAISIE DES PAIEMENTS")){
            return ResponseEntity.ok(new MessageDTO("erreur","Vous n'avez pas accès à cette fonctionnalité"));
        }

        MessageDTO messsage;
        Projet projet =projetService.findProjet(id);
        if(projet==null){
            return ResponseEntity.ok(new MessageDTO("erreur","projet inexistant"));
        }
        if(projet.getBordereau()==null){
            return ResponseEntity.ok(new MessageDTO("erreur","impossible, car le projet n'est pas validé"));
        }

        try {
            messsage=payementService.importpayement(file.getInputStream(),projet);

        } catch (IOException  | IllegalArgumentException   e) {
            // Log the exception or handle it as needed
            e.printStackTrace();
            return ResponseEntity.ok(new MessageDTO("erreur","Erreur d'importation , votre fichier ne respecte pas les contraintes"));
        }
        return ResponseEntity.ok(messsage);

    }



    // Gestion du suivi des travaux

    @PostMapping("/projet/saveSuiviTravaux/{id}")
    public ResponseEntity<MessageDTO> saveSuiviTravaux(@PathVariable(value = "id") long id, @RequestBody SuiviTravaux suiviTravaux ,@RequestHeader("Authorization") String token){

        Jwt jwt=jwtDecoder.decode(token.substring(7));
        Role role= roleService.getRoleByRoleName(jwt.getClaim("role"));

        if(!roleService.containsPermission(role.getPermissions(),"SUIVI DE L'EXECUTION DES TRAVAUX")){
            return ResponseEntity.ok(new MessageDTO("erreur","Vous n'avez pas accès à cette fonctionnalité"));
        }

        Projet projet =projetService.findProjet(id);
        if(projet==null){
            return ResponseEntity.ok(new MessageDTO("erreur","projet inexistant"));
        }

        if(!projet.getProgramme().getOrdonnateur().equals(Ordonnateur.valueOf(jwt.getClaim("structure")))){
            return ResponseEntity.ok(new MessageDTO("erreur","accès refusé"));
        }

        if(projet.getBordereau()==null){
            return ResponseEntity.ok(new MessageDTO("erreur","impossible, car le projet n'est pas validé"));
        }
        if( suiviTravaux.getTauxAvancement()<=0 || suiviTravaux.getTauxConsommation()<=0){

            return ResponseEntity.ok(new MessageDTO("erreur","veuillez remplir tous les champs"));
        }
        if( suiviTravaux.getTauxAvancement()>100 || suiviTravaux.getTauxConsommation()>100){

            return ResponseEntity.ok(new MessageDTO("erreur","les taux doivent être inférieur à 100%"));
        }

        projetService.saveSuiviTravaux(projet,suiviTravaux);

        return ResponseEntity.ok(new MessageDTO("succes","suivi des travaux enregistré avec succès"));

    }

    @PutMapping("/projet/updateSuiviTravaux/{id}")
    public ResponseEntity<MessageDTO> updateSuiviTravaux(@PathVariable(value = "id") long id, @RequestBody SuiviTravaux update ,@RequestHeader("Authorization") String token){

        Jwt jwt=jwtDecoder.decode(token.substring(7));
        Role role= roleService.getRoleByRoleName(jwt.getClaim("role"));

        if(!roleService.containsPermission(role.getPermissions(),"SUIVI DE L'EXECUTION DES TRAVAUX")){
            return ResponseEntity.ok(new MessageDTO("erreur","Vous n'avez pas accès à cette fonctionnalité"));
        }

        SuiviTravaux suiviTravaux=projetService.findSuiviTravaux(id);
        if(suiviTravaux==null){
            return ResponseEntity.ok(new MessageDTO("erreur","suivi  inexistant"));
        }

        if(!suiviTravaux.getProjet().getProgramme().getOrdonnateur().equals(Ordonnateur.valueOf(jwt.getClaim("structure")))){
            return ResponseEntity.ok(new MessageDTO("erreur","accès refusé"));
        }

        if( update.getDescription()==null || update.getTauxAvancement()<=0 || update.getTauxConsommation()<=0){

            return ResponseEntity.ok(new MessageDTO("erreur","veuillez remplir tous les champs"));
        }
        if( update.getTauxAvancement()>100 || update.getTauxConsommation()>100){

            return ResponseEntity.ok(new MessageDTO("erreur","les taux doivent être inférieur à 100%"));
        }

        long j= (new Date().getTime()- suiviTravaux.getDate().getTime())/86400000;

        if(j > 5){
            return ResponseEntity.ok(new MessageDTO("erreur","Impossible de modifier ce suivi car il a été enregistré il y'a plus d'une semaine"));
        }

        projetService.updateSuiviTravaux(suiviTravaux,update);

        return ResponseEntity.ok(new MessageDTO("succes","suivi des travaux modifié avec succès"));

    }

    @DeleteMapping("/projet/deleteSuiviTravaux/{id}")
    public ResponseEntity<MessageDTO> deleteSuiviTravaux(@PathVariable(value = "id") long id ,@RequestHeader("Authorization") String token){

        Jwt jwt=jwtDecoder.decode(token.substring(7));
        Role role= roleService.getRoleByRoleName(jwt.getClaim("role"));

        if(!roleService.containsPermission(role.getPermissions(),"SUIVI DE L'EXECUTION DES TRAVAUX")){
            return ResponseEntity.ok(new MessageDTO("erreur","Vous n'avez pas accès à cette fonctionnalité"));
        }

        SuiviTravaux suiviTravaux =projetService.findSuiviTravaux(id);

        if(suiviTravaux==null){
            return ResponseEntity.ok(new MessageDTO("erreur","suivi  inexistant"));
        }

        if(!suiviTravaux.getProjet().getProgramme().getOrdonnateur().equals(Ordonnateur.valueOf(jwt.getClaim("structure")))){
            return ResponseEntity.ok(new MessageDTO("erreur","accès refusé"));
        }

        long j= (new Date().getTime()-suiviTravaux.getDate().getTime())/86400000;

        if(j > 7){
            return ResponseEntity.ok(new MessageDTO("erreur","Impossible de supprimer ce suivi car il a été enregistré il y'a plus d'une semaine"));
        }

        projetService.deleteSuiviTravaux(id);

        return ResponseEntity.ok(new MessageDTO("succes","suivi supprimé avec succès"));
    }



    //Gestion de la passation

    @PostMapping("/projet/savePassation/{id}")
    public ResponseEntity<MessageDTO> savePassation(@PathVariable(value = "id") long id, @RequestBody Passation passation ,@RequestHeader("Authorization") String token){

        Jwt jwt=jwtDecoder.decode(token.substring(7));
        Role role= roleService.getRoleByRoleName(jwt.getClaim("role"));

        if(!roleService.containsPermission(role.getPermissions(),"SUIVI DE LA PASSATION")){
            return ResponseEntity.ok(new MessageDTO("erreur","Vous n'avez pas accès à cette fonctionnalité"));
        }

        Projet projet =projetService.findProjet(id);
        if(projet==null){
            return ResponseEntity.ok(new MessageDTO("erreur","projet inexistant"));
        }

        if(!projet.getProgramme().getOrdonnateur().equals(Ordonnateur.valueOf(jwt.getClaim("structure")) )){
            return ResponseEntity.ok(new MessageDTO("erreur","accès refusé"));
        }

        if(projet.getProgramme().getStatut()!=ProgrammeStatut.VALIDER){
            return ResponseEntity.ok(new MessageDTO("erreur","Votre programme n'a pas été validé"));
        }

        if( passation.getContractualisation()=="" || passation.getDateOs()==null || passation.getNumeroMarche()==""){

            return ResponseEntity.ok(new MessageDTO("erreur","veuillez remplir tous les champs"));
        }

        projetService.savePassation(projet,passation);

        return ResponseEntity.ok(new MessageDTO("succes","suivi de passation enregistré avec succès"));
    }

    @PutMapping("/projet/updatePassation/{id}")
    public ResponseEntity<MessageDTO> updatePassation(@PathVariable(value = "id") long id, @RequestBody Passation update ,@RequestHeader("Authorization") String token){

        Jwt jwt=jwtDecoder.decode(token.substring(7));
        Role role= roleService.getRoleByRoleName(jwt.getClaim("role"));

        if(!roleService.containsPermission(role.getPermissions(),"SUIVI DE LA PASSATION")){

            return ResponseEntity.ok(new MessageDTO("erreur","Vous n'avez pas accès à cette fonctionnalité"));
        }

        Passation passation=projetService.findPassation(id);
        if(passation==null){
            return ResponseEntity.ok(new MessageDTO("erreur","suivi  inexistant"));
        }

        if(!passation.getProjet().getProgramme().getOrdonnateur().equals(Ordonnateur.valueOf(jwt.getClaim("structure")) )){
            return ResponseEntity.ok(new MessageDTO("erreur","accès refusé"));
        }

        if( update.getContractualisation().isEmpty() || update.getDateOs()==null || update.getNumeroMarche().isEmpty()){

            return ResponseEntity.ok(new MessageDTO("erreur","veuillez remplir tous les champs"));
        }

        long j= (new Date().getTime()- passation.getDate().getTime())/86400000;

        if(j > 5){
            return ResponseEntity.ok(new MessageDTO("erreur","Impossible de modifier cet élément car il a été enregistré il y'a plus d'une semaine"));
        }

        projetService.updatePassation(passation,update);

        return ResponseEntity.ok(new MessageDTO("succes","suivi de passation modifié avec succès"));
    }

    @DeleteMapping("/projet/deletePassation/{id}")
    public ResponseEntity<MessageDTO> deletePassation(@PathVariable(value = "id") long id ,@RequestHeader("Authorization") String token){

        Jwt jwt=jwtDecoder.decode(token.substring(7));
        Role role= roleService.getRoleByRoleName(jwt.getClaim("role"));

        if(!roleService.containsPermission(role.getPermissions(),"SUIVI DE LA PASSATION")){

            return ResponseEntity.ok(new MessageDTO("erreur","Vous n'avez pas accès à cette fonctionnalité"));
        }

        Passation passation =projetService.findPassation(id);

        if(passation==null){
            return ResponseEntity.ok(new MessageDTO("erreur","suivi  inexistant"));
        }

        if(!passation.getProjet().getProgramme().getOrdonnateur().equals(Ordonnateur.valueOf(jwt.getClaim("structure")))){
            return ResponseEntity.ok(new MessageDTO("erreur","accès refusé"));
        }

        long j= (new Date().getTime()-passation.getDate().getTime())/86400000;

        if(j > 7){
            return ResponseEntity.ok(new MessageDTO("erreur","Impossible de supprimer cet élément car il a été enregistré il y'a plus d'une semaine"));
        }

        projetService.deletePassation(id);

        return ResponseEntity.ok(new MessageDTO("succes","suppression réussite"));
    }

}

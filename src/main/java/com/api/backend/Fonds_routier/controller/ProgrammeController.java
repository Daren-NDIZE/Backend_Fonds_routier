package com.api.backend.Fonds_routier.controller;
import com.api.backend.Fonds_routier.DTO.DecisionDTO;
import com.api.backend.Fonds_routier.DTO.MessageDTO;
import com.api.backend.Fonds_routier.DTO.ProgrammeFilterDTO;
import com.api.backend.Fonds_routier.DTO.SyntheseDTO;
import com.api.backend.Fonds_routier.enums.Ordonnateur;
import com.api.backend.Fonds_routier.enums.ProgrammeStatut;
import com.api.backend.Fonds_routier.enums.ProgrammeType;
import com.api.backend.Fonds_routier.model.Action;
import com.api.backend.Fonds_routier.model.Programme;
import com.api.backend.Fonds_routier.model.Projet;
import com.api.backend.Fonds_routier.model.Utilisateur;
import com.api.backend.Fonds_routier.service.AccountService;
import com.api.backend.Fonds_routier.service.HTTPService;
import com.api.backend.Fonds_routier.service.ProgrammeService;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
public class ProgrammeController {

    @Autowired
    ProgrammeService programmeService;
    @Autowired
    AccountService accountService;

    @Autowired
    JwtDecoder jwtDecoder;


    @PostMapping("/saveProgramme")
    public ResponseEntity<MessageDTO> saveProgramme(@RequestBody Programme programme,@RequestHeader("Authorization") String token){

        if(programme.getAnnee()==0 || programme.getType()==null){
            return ResponseEntity.ok(new MessageDTO("erreur","veuillez remplir tous les champs"));
        }
        if(programme.getBudget()<=0){
            return ResponseEntity.ok(new MessageDTO("erreur","budget incorrect"));
        }

        int difference= programme.getAnnee()-LocalDate.now().getYear();
        Jwt jwt=jwtDecoder.decode(token.substring(7));

        if( difference!=0 && difference!=1 ){
            return ResponseEntity.ok(new MessageDTO("erreur","année incorrect"));
        }

        List<Programme> list=programmeService.getProgrammeByOrdonnateurAndTypeAndYear(Ordonnateur.valueOf(jwt.getClaim("role")),programme.getType(),programme.getAnnee());

        if(!list.isEmpty()){
            return ResponseEntity.ok(new MessageDTO("erreur","Vous avez déja créé le programme de cette année"));
        }

        programme.setStatut(ProgrammeStatut.EN_ATTENTE_DE_SOUMISSION);
        programme.setOrdonnateur(Ordonnateur.valueOf(jwt.getClaim("role")));

        if(programme.getType()==ProgrammeType.BASE){
            programme.setIntitule("Programme "+jwt.getClaim("role")+" "+programme.getAnnee());
        }else {
            programme.setIntitule("Programme "+programme.getType().toString().toLowerCase()+" "+jwt.getClaim("role")+" "+programme.getAnnee());
        }
        programmeService.saveProgramme(programme);

        return ResponseEntity.ok(new MessageDTO("succes","programme créé avec succès"));

    }

    @Secured({"DCO","CO"})
    @PostMapping("/saveReportProgramme")
    public ResponseEntity<MessageDTO> saveProgrammeReport(@RequestBody Programme programme,@RequestHeader("Authorization") String token){

        Jwt jwt=jwtDecoder.decode(token.substring(7));
        LocalDate date = LocalDate.now();

        List<Programme> list=programmeService.getProgrammeByOrdonnateurAndTypeAndYear(programme.getOrdonnateur(),ProgrammeType.REPORT,date.getYear());

        if(!list.isEmpty()){
            return ResponseEntity.ok(new MessageDTO("erreur","Vous avez déja créé le programme report de cette année"));
        }

        programme.setAnnee(date.getYear());
        programme.setType(ProgrammeType.REPORT);
        programme.setStatut(ProgrammeStatut.VALIDER);
        programme.setIntitule("Programme report "+programme.getOrdonnateur()+" "+date.getYear());

        programmeService.saveProgramme(programme);

        accountService.saveAction(new Action(0,jwt.getSubject(),jwt.getClaim("role"),"CREATION DU "+programme.getIntitule(),new Date()));

        return ResponseEntity.ok(new MessageDTO("succes","programme créé avec succès"));

    }

    @GetMapping("/getProgrammes")
    public List<Programme>  getProgramme(){

        List<Programme> list=programmeService.getProgramme();

        return list;
    }

    @PostMapping("/searchProgramme")
    public ResponseEntity<Object> searchProgramme(@RequestBody ProgrammeFilterDTO filter,@RequestHeader("Authorization") String token){

        Jwt jwt=jwtDecoder.decode(token.substring(7));
        filter.setOrdonnateur(jwt.getClaim("role"));
        List <String> type=List.of("BASE","ADDITIONNEL","REPORT");
        List<ProgrammeStatut> status=List.of(ProgrammeStatut.VALIDER,ProgrammeStatut.CLOTURER);

        if( filter.getAnnee()==0 || !type.contains(filter.getType()) ){

            return ResponseEntity.ok(new MessageDTO("erreur","Veuillez remplir correctement tous les champs"));
        }

        List<Programme> list= programmeService.getProgrammeByOrdonnateurAndTypeAndYear(Ordonnateur.valueOf(jwt.getClaim("role")),ProgrammeType.valueOf(filter.getType()),filter.getAnnee());

        if(list.isEmpty()){
            return ResponseEntity.ok(new MessageDTO("erreur","Programme inexistant"));
        }
        if( !status.contains( list.get(0).getStatut()) ){

            return ResponseEntity.ok(new MessageDTO("erreur","Le programme que vous cherchez n'a pas encore été validé"));
        }

        return ResponseEntity.ok(list.get(0));

    }

    @Secured({"MINTP", "MINHDU","MINT"})
    @GetMapping("/getProgrammesByRole")
    public List<Programme>  getProgrammeByRole(@RequestHeader("Authorization") String token){

        Jwt jwt=jwtDecoder.decode(token.substring(7));
        List<Programme> list=programmeService.getProgrammeByOrdonnateur(Ordonnateur.valueOf(jwt.getClaim("role")));

        return list;
    }

    @GetMapping("/getSubmitProgramme")
    public List<Programme>  getSubmitProgramme(){

        return programmeService.getProgrammeByStatuts(List.of(ProgrammeStatut.SOUMIS,ProgrammeStatut.CORRECTION,ProgrammeStatut.REJETER));
    }

    @GetMapping("/getValidProgramme")
    public List<Programme>  getValidProgramme(){

        return programmeService.getProgrammeByStatut(ProgrammeStatut.VALIDER);
    }

    @GetMapping("/getCloseProgramme")
    public List<Programme>  getCloseProgramme(){

        return programmeService.getProgrammeByStatut(ProgrammeStatut.CLOTURER);
    }

    @Secured({"MINTP", "MINHDU","MINT"})
    @GetMapping("/getCloseProgrammeByRole")
    public List<Programme>  getCloseProgrammeByRole(@RequestHeader("Authorization") String token){

        Jwt jwt=jwtDecoder.decode(token.substring(7));
        List<Programme> list=programmeService.getProgrammeByOrdonnateurAndStatut(Ordonnateur.valueOf(jwt.getClaim("role")),List.of(ProgrammeStatut.CLOTURER));
        return list;
    }

    @Secured({"MINTP", "MINHDU","MINT"})
    @GetMapping("/getValidAndCloseProgrammeByRole")
    public List<Programme>  getValidAndCloseProgrammeByrole(@RequestHeader("Authorization") String token){

        Jwt jwt=jwtDecoder.decode(token.substring(7));
        return programmeService.getProgrammeByOrdonnateurAndStatut(Ordonnateur.valueOf(jwt.getClaim("role")), List.of(ProgrammeStatut.VALIDER,ProgrammeStatut.CLOTURER));
    }

    @GetMapping("/getValidAndCloseProgramme")
    public List<Programme>  getValidAndCloseProgramme(){

        return programmeService.getProgrammeByStatuts(List.of(ProgrammeStatut.VALIDER,ProgrammeStatut.CLOTURER));
    }

    @Secured({"MINTP", "MINHDU","MINT"})
    @DeleteMapping("/deleteProgramme/{id}")
    public ResponseEntity<MessageDTO> deleteProgramme(@PathVariable(value = "id") long id,@RequestHeader("Authorization") String token){

        Programme programme=programmeService.findProgramme(id);
        Jwt jwt=jwtDecoder.decode(token.substring(7));

        if(programme==null){
            return ResponseEntity.ok(new MessageDTO("erreur","programme inexistant"));
        }
        if( !programme.getOrdonnateur().equals(Ordonnateur.valueOf(jwt.getClaim("role"))) ){

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageDTO("erreur","accès refusé"));
        }
        if(programme.getStatut()!=ProgrammeStatut.EN_ATTENTE_DE_SOUMISSION ){

            return ResponseEntity.ok(new MessageDTO("erreur","impossible, programme en cours de traitement"));
        }

        programmeService.deleteProgramme(id);

        return ResponseEntity.ok(new MessageDTO("succes","programme supprimé avec succès"));
    }

    @DeleteMapping("/deleteReportProgramme/{id}")
    public ResponseEntity<MessageDTO> deleteReportProgramme(@PathVariable(value = "id") long id,@RequestHeader("Authorization") String token){

        Programme programme=programmeService.findProgramme(id);
        Jwt jwt=jwtDecoder.decode(token.substring(7));

        if(programme==null){
            return ResponseEntity.ok(new MessageDTO("erreur","Programme inexistant"));
        }
        if(programme.getType()!=ProgrammeType.REPORT){

            return ResponseEntity.ok(new MessageDTO("erreur","Impossible, car ce programme n'est pas un report"));
        }
        if(!programme.getProjetList().isEmpty()){

            return ResponseEntity.ok(new MessageDTO("erreur","Impossible, car ce programme contient des projets"));
        }

        programmeService.deleteProgramme(id);

        accountService.saveAction(new Action(0,jwt.getSubject(),jwt.getClaim("role"),"SUPPRESSION DU "+programme.getIntitule().toUpperCase(),new Date()));

        return ResponseEntity.ok(new MessageDTO("succes","programme supprimé avec succès"));
    }

    @Secured({"MINTP", "MINHDU","MINT"})
    @PostMapping("/ajusteProgramme/{id}")
    public ResponseEntity<MessageDTO> ajusteProgramme(@PathVariable(value = "id") long id,@RequestHeader("Authorization") String token) throws CloneNotSupportedException {

        Programme programme=programmeService.findProgramme(id);
        Jwt jwt=jwtDecoder.decode(token.substring(7));

        if(programme==null){
            return ResponseEntity.ok(new MessageDTO("erreur","programme inexistant"));
        }
        if( !programme.getOrdonnateur().equals(Ordonnateur.valueOf(jwt.getClaim("role"))) ){

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageDTO("erreur","accès refusé"));
        }
        if(programme.getType()==ProgrammeType.REPORT ){

            return ResponseEntity.ok(new MessageDTO("erreur","Il est impossible d'ajuster le programme des reports"));
        }
        if(programme.getStatut()!=ProgrammeStatut.VALIDER ){

            return ResponseEntity.ok(new MessageDTO("erreur","Ce programme n'a pas encore été validé"));
        }

        programmeService.ajusterProgramme(programme);

        return ResponseEntity.ok(new MessageDTO("succes","programme ajusté avec succès"));
    }

    @Secured({"ADMIN","DCO","CO","ACO","STAGIAIRE"})
    @GetMapping("/programme/{id}")
    public ResponseEntity<Object> getProgrammeById(@PathVariable(value = "id") long id){

        Programme programme=programmeService.findProgramme(id);
        if(programme==null){
            return ResponseEntity.ok(new MessageDTO("erreur","programme inexistant"));
        }

        return ResponseEntity.ok(programme);

    }

    @Secured({"MINTP", "MINHDU","MINT"})
    @GetMapping("/programmeByRole/{id}")
    public ResponseEntity<Object> getProgrammeById(@PathVariable(value = "id") long id,@RequestHeader("Authorization") String token){

        Programme programme=programmeService.findProgramme(id);
        Jwt jwt=jwtDecoder.decode(token.substring(7));

        if(programme==null){
            return ResponseEntity.ok(new MessageDTO("erreur","programme inexistant"));
        }
        if(!programme.getOrdonnateur().equals(Ordonnateur.valueOf(jwt.getClaim("role")))){

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageDTO("erreur","accès refusé"));
        }
        programme.getProjetList();
        return ResponseEntity.ok(programme);

    }

    @Secured({"MINTP", "MINHDU","MINT"})
    @PutMapping("/updateProgramme/{id}")
    public ResponseEntity<MessageDTO> updateProgramme(@PathVariable(value = "id") long id,@RequestBody Programme update,@RequestHeader("Authorization") String token){

        Programme programme =programmeService.findProgramme(id);
        Jwt jwt=jwtDecoder.decode(token.substring(7));

        if(programme==null){
            return ResponseEntity.ok(new MessageDTO("erreur","programme inexistant"));
        }
        if( !programme.getOrdonnateur().equals(Ordonnateur.valueOf(jwt.getClaim("role"))) ){

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageDTO("erreur","accès refusé"));
        }
        if(programme.getStatut()== ProgrammeStatut.VALIDER || programme.getStatut()== ProgrammeStatut.SOUMIS){

            return ResponseEntity.ok(new MessageDTO("erreur","impossible, programme en cours de traitement"));
        }

        programmeService.updateProgramme(programme,update);

        return ResponseEntity.ok(new MessageDTO("succes","programme modifié avec succès"));


    }

    @PutMapping("/updatePrevision/{id}")
    public ResponseEntity<MessageDTO> updatePrevision(@PathVariable(value = "id") long id,@RequestBody Programme update,@RequestHeader("Authorization") String token){

        Programme programme =programmeService.findProgramme(id);
        Jwt jwt=jwtDecoder.decode(token.substring(7));

        if(programme==null){
            return ResponseEntity.ok(new MessageDTO("erreur","programme inexistant"));
        }
        if( !programme.getOrdonnateur().equals(Ordonnateur.valueOf(jwt.getClaim("role"))) ){

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageDTO("erreur","accès refusé"));
        }
        if(programme.getStatut()== ProgrammeStatut.VALIDER || programme.getStatut()== ProgrammeStatut.SOUMIS){

            return ResponseEntity.ok(new MessageDTO("erreur","impossible, programme en cours de traitement"));
        }
        programme.setPrevision(update.getPrevision());
        programmeService.saveProgramme(programme);

        return ResponseEntity.ok(new MessageDTO("succes","La provision de réserve à été bien enregistré"));

    }

    @Secured({"MINTP", "MINHDU","MINT"})
    @PutMapping("/submitProgramme/{id}")
    public ResponseEntity<MessageDTO> submitProgramme(@PathVariable(value = "id") long id,@RequestHeader("Authorization") String token) throws IOException, InterruptedException {

        Programme programme =programmeService.findProgramme(id);
        Jwt jwt=jwtDecoder.decode(token.substring(7));

        if(programme==null){
            return ResponseEntity.ok(new MessageDTO("erreur","programme inexistant"));
        }
        if(!programme.getOrdonnateur().equals(Ordonnateur.valueOf(jwt.getClaim("role")))){

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageDTO("erreur","accès refusé"));
        }
        if(programme.getStatut()== ProgrammeStatut.VALIDER || programme.getStatut()== ProgrammeStatut.SOUMIS){

            return ResponseEntity.ok(new MessageDTO("erreur","Impossible, programme en cours de traitement"));
        }
        if(programme.getStatut()== ProgrammeStatut.CLOTURER){

            return ResponseEntity.ok(new MessageDTO("erreur","impossible, programme cloturé"));
        }

        List<Projet> projetList=  programme.getProjetList().stream().filter(p->p.getFinancement().equals("NORMAL")).collect(Collectors.toList());

        long total=programmeService.totalBudget(projetList)+programme.getPrevision();

        if(programme.getBudget()!=total){

            return ResponseEntity.ok(new MessageDTO("erreur","le budget total du programme que vous avez rempli lors de sa creation est différent du total actuel "));
        }

        programmeService.submitProgramme(programme);

        List<Utilisateur> utilisateurs=accountService.getAllUser();
        for(Utilisateur utilisateur :utilisateurs){

            if(utilisateur.getRole().getRoleName().equals("DCO")){

                try {
                    HTTPService service = new HTTPService();
                    service.httpRequest(List.of("237" + utilisateur.getTelephone()), "Le " + programme.getIntitule() + " vient d'être soumis sur la plateforme COWEB-FR");
                }catch (ConnectException e){
                    e.printStackTrace();
                }
                break;
            }
        }
        accountService.saveAction(new Action(0,jwt.getSubject(),jwt.getClaim("role")," SOUMISSION DU "+programme.getIntitule(),new Date()));

        return ResponseEntity.ok(new MessageDTO("succes","Programme soumis"));
    }

    @Secured("DCO")
    @PutMapping("/decideProgramme/{id}")
    public ResponseEntity<MessageDTO> decision(@PathVariable(value = "id") long id,@RequestHeader("Authorization") String token, @ModelAttribute DecisionDTO decision) throws IOException, InterruptedException {

        Programme programme =programmeService.findProgramme(id);
        Jwt jwt=jwtDecoder.decode(token.substring(7));

        if(programme==null){
            return ResponseEntity.ok(new MessageDTO("erreur","programme inexistant"));
        }
        if(programme.getStatut()!=ProgrammeStatut.SOUMIS){
            return ResponseEntity.ok(new MessageDTO("erreur","impossible car le programme n'a pas été soumis"));
        }
        if(programme.getType()==ProgrammeType.REPORT && decision.getStatut()!=ProgrammeStatut.VALIDER){

            return ResponseEntity.ok(new MessageDTO("erreur","impossible d'effectuer cette action"));
        }
        if(decision.getStatut()==ProgrammeStatut.VALIDER && decision.getFile()!=null){

            String extention= FilenameUtils.getExtension(decision.getFile().getOriginalFilename()) ;

            if(!extention.equals("pdf")){
                return ResponseEntity.ok(new MessageDTO("erreur","le type de fichier que vous avez soumis est incorrect"));
            }

            File folder= new File("C:\\Users\\Daren NDIZE\\Desktop\\FR\\Back end\\Backend_Fonds_routier\\src\\main\\resources\\static\\programmes\\");

            if(!folder.exists()){
                folder.mkdir();
            }
            final Path path =Paths.get("C:\\Users\\Daren NDIZE\\Desktop\\FR\\Back end\\Backend_Fonds_routier\\src\\main\\resources\\static\\programmes\\"+programme.getIntitule()+".pdf");
            Files.write(path,decision.getFile().getBytes());
            programme.setUrl_resolution("programmes/"+programme.getIntitule()+".pdf");
        }
        programme.setStatut(decision.getStatut());
        programme.setObservation(decision.getObservation());

        if(programme.getProgramme()!=null && decision.getStatut()==ProgrammeStatut.VALIDER){

            programme.setType(programme.getProgramme().getType());
            programme.getProgramme().setStatut(ProgrammeStatut.CLOTURER);
            //programme.getProgramme().setIntitule("Programme initiale "+programme.getOrdonnateur()+" "+programme.getAnnee());
            programmeService.saveProgramme(programme.getProgramme());

        }
        programmeService.saveProgramme(programme);

        List<Utilisateur> utilisateurs=accountService.getAllUser();
        for(Utilisateur utilisateur :utilisateurs){

            if(utilisateur.getRole().getRoleName().equals(programme.getOrdonnateur().toString())){
                String message = "";
                if(decision.getStatut()==ProgrammeStatut.VALIDER){
                    message="Le "+programme.getIntitule()+" vient d'être validé";
                }else if(decision.getStatut()==ProgrammeStatut.REJETER){
                    message="Le "+programme.getIntitule()+" vient d'être rejeté";
                }else if(decision.getStatut()==ProgrammeStatut.CORRECTION){
                    message="Le "+programme.getIntitule()+" vient d'être renvoyé pour correction";
                }

                try{
                    HTTPService service=new HTTPService();
                    service.httpRequest(List.of("237"+utilisateur.getTelephone()),message);
                }catch (ConnectException e){
                    e.printStackTrace();
                }
                break;
            }
        }

        accountService.saveAction(new Action(0,jwt.getSubject(),jwt.getClaim("role"),programme.getStatut()+" LE "+programme.getIntitule(),new Date()));

        return ResponseEntity.ok(new MessageDTO("succes","votre demmande a été executé"));
    }

    @Secured("DCO")
    @PutMapping("/clotureProgramme/{id}")
    public ResponseEntity<MessageDTO> clotureProgramme(@PathVariable(value = "id") long id){

        Programme programme =programmeService.findProgramme(id);

        if(programme==null){
            return ResponseEntity.ok(new MessageDTO("erreur","programme inexistant"));
        }
        if(programme.getStatut()!=ProgrammeStatut.VALIDER){

            return ResponseEntity.ok(new MessageDTO("erreur","Impossible, programme en cours de traitement"));
        }

        programme.setStatut(ProgrammeStatut.CLOTURER);
        programmeService.saveProgramme(programme);

        return ResponseEntity.ok(new MessageDTO("succes","Programme cloturé"));

    }

    @GetMapping("/programme/getResolution/{id}")
    public ResponseEntity<Object> getResolution(@PathVariable(value = "id") long id) throws IOException {

        Programme programme =programmeService.findProgramme(id);

        if(programme==null){
            return ResponseEntity.ok(new MessageDTO("erreur","programme inexistant"));
        }
        if(programme.getUrl_resolution()==null){
            return ResponseEntity.ok(new MessageDTO("erreur","aucun fichier disponible"));
        }

        Path path=Paths.get("C:/Users/Daren NDIZE/Desktop/FR/Back end/Backend_Fonds_routier/src/main/resources/static/"+programme.getUrl_resolution());

        if(!Files.exists(path)){
            return ResponseEntity.ok(new MessageDTO("erreur","fichier introuvable"));
        }
        byte[] pdf=Files.readAllBytes(path);

        HttpHeaders header=new HttpHeaders();
        header.setContentType(MediaType.parseMediaType("application/pdf"));
        header.setContentLength(pdf.length);

        return new ResponseEntity(pdf,header,HttpStatus.OK);
    }

    @PostMapping("/programme/syntheseProgramme")
    public ResponseEntity<SyntheseDTO> getFinalProgramme(@RequestBody ProgrammeFilterDTO filter){

        List<Programme> list= programmeService.getSyntheseProgramme(filter);

        SyntheseDTO syntheseDTO=programmeService.syntheseProgramme(list, filter.getOrdonnateur());

        return ResponseEntity.ok(syntheseDTO);
    }

    @Secured({"MINTP", "MINHDU","MINT"})
    @PostMapping("/programme/syntheseOrdonnateur")
    public ResponseEntity<SyntheseDTO> getFinalSynthese(@RequestHeader("Authorization") String token,@RequestBody ProgrammeFilterDTO filter){

        Jwt jwt=jwtDecoder.decode(token.substring(7));
        filter.setOrdonnateur(jwt.getClaim("role"));

        List<Programme> list= programmeService.getSyntheseProgramme(filter);
        SyntheseDTO syntheseDTO=programmeService.syntheseProgramme(list, filter.getOrdonnateur());

        return ResponseEntity.ok(syntheseDTO);
    }

    @Secured({"MINTP", "MINHDU","MINT"})
    @PostMapping("/programme/importProgrammeFile/{id}")
    public ResponseEntity<MessageDTO> importExcel(@PathVariable(value = "id") long id,@RequestParam("file") MultipartFile file) {

        Programme programme=programmeService.findProgramme(id);
        if(programme==null){
            return ResponseEntity.ok(new MessageDTO("erreur","programme inexistant"));
        }
        if(!programme.getProjetList().isEmpty()){

            return ResponseEntity.ok(new MessageDTO("erreur","Impossible, car le programme doit être vide"));
        }
        if(programme.getStatut()!=ProgrammeStatut.EN_ATTENTE_DE_SOUMISSION){

            return ResponseEntity.ok(new MessageDTO("erreur","Impossible , car se programme est en cours de traitement"));
        }
        try {
            
            MessageDTO messageDTO = new MessageDTO();

            if(programme.getOrdonnateur()==Ordonnateur.MINHDU){

               messageDTO= programmeService.importMINHDUProgramme(file.getInputStream(),programme);

            }else if(programme.getOrdonnateur()==Ordonnateur.MINTP){

                messageDTO= programmeService.importMINTPProgramme(file.getInputStream(),programme);

            }else if(programme.getOrdonnateur()==Ordonnateur.MINT){

               messageDTO= programmeService.importMINTProgramme(file.getInputStream(),programme);
            }

            return ResponseEntity.ok(messageDTO);

        } catch (IOException  | IllegalArgumentException  e) {
            // Log the exception or handle it as needed
            e.printStackTrace();
            return ResponseEntity.ok(new MessageDTO("erreur","Erreur d'importation , votre fichier ne respecte pas les contraintes"));
        }
    }
}





package com.api.backend.Fonds_routier.controller;
import com.api.backend.Fonds_routier.DTO.DecisionDTO;
import com.api.backend.Fonds_routier.DTO.MessageDTO;
import com.api.backend.Fonds_routier.DTO.ProgrammeFilterDTO;
import com.api.backend.Fonds_routier.DTO.SyntheseDTO;
import com.api.backend.Fonds_routier.enums.Ordonnateur;
import com.api.backend.Fonds_routier.enums.ProgrammeStatut;
import com.api.backend.Fonds_routier.enums.ProgrammeType;
import com.api.backend.Fonds_routier.model.Programme;
import com.api.backend.Fonds_routier.service.ProgrammeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.io.FilenameUtils;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
public class ProgrammeController {

    @Autowired
    ProgrammeService programmeService;
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

        programme.setStatut(ProgrammeStatut.EN_ATTENTE_DE_SOUMISSION);
        programme.setOrdonnateur(Ordonnateur.valueOf(jwt.getClaim("role")));

        if(programme.getType()==ProgrammeType.BASE){
            programme.setIntitule("Programme "+jwt.getClaim("role")+" "+programme.getAnnee());
        }else {
            programme.setIntitule("Programme "+programme.getType().toString().toLowerCase()+" "+jwt.getClaim("role")+" "+programme.getAnnee());
        }
        programmeService.saveProgramme(programme);

        return ResponseEntity.ok(new MessageDTO("succes","programme créer avec succès"));

    }

    @GetMapping("/getProgrammes")
    public List<Programme>  getProgramme(){

        List<Programme> list=programmeService.getProgramme();

        return list;
    }

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

    @GetMapping("/getCloseProgrammeByRole")
    public List<Programme>  getCloseProgrammeByRole(@RequestHeader("Authorization") String token){

        Jwt jwt=jwtDecoder.decode(token.substring(7));
        List<Programme> list=programmeService.getProgrammeByOrdonnateurAndStatut(Ordonnateur.valueOf(jwt.getClaim("role")),List.of(ProgrammeStatut.VALIDER));
        return list;
    }

    @GetMapping("/getValidAndCloseProgramme")
    public List<Programme>  getValidAndCloseProgramme(){

        return programmeService.getProgrammeByStatuts(List.of(ProgrammeStatut.VALIDER,ProgrammeStatut.CLOTURER));
    }

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
        if(programme.getStatut()!=ProgrammeStatut.VALIDER ){

            return ResponseEntity.ok(new MessageDTO("erreur","vous avez la possibilité de modifier ce programme"));
        }

        programmeService.ajusterProgramme(programme);

        return ResponseEntity.ok(new MessageDTO("succes","programme ajusté avec succès"));
    }


    @GetMapping("/programme/{id}")
    public ResponseEntity getProgrammeById(@PathVariable(value = "id") long id){

        Programme programme=programmeService.findProgramme(id);
        if(programme==null){
            return ResponseEntity.ok(new MessageDTO("erreur","programme inexistant"));
        }

        return ResponseEntity.ok(programme);

    }

    @GetMapping("/programmeByRole/{id}")
    public ResponseEntity getProgrammeById(@PathVariable(value = "id") long id,@RequestHeader("Authorization") String token){

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

        return ResponseEntity.ok(new MessageDTO("succes","La prévision de réserve à été bien enregistré"));

    }


    @PutMapping("/submitProgramme/{id}")
    public ResponseEntity<MessageDTO> submitProgramme(@PathVariable(value = "id") long id,@RequestHeader("Authorization") String token){

        Programme programme =programmeService.findProgramme(id);
        Jwt jwt=jwtDecoder.decode(token.substring(7));

        if(programme==null){
            return ResponseEntity.ok(new MessageDTO("erreur","programme inexistant"));
        }
        if(!programme.getOrdonnateur().equals(Ordonnateur.valueOf(jwt.getClaim("role")))){

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageDTO("erreur","accès refusé"));
        }
        if(programme.getStatut()== ProgrammeStatut.VALIDER || programme.getStatut()== ProgrammeStatut.SOUMIS){

            return ResponseEntity.ok(new MessageDTO("erreur","impossible, programme en cours de traitement"));
        }

        programmeService.submitProgramme(programme);
        return ResponseEntity.ok(new MessageDTO("succes","Programme soumis"));
    }


    @PutMapping("/decideProgramme/{id}")
    public ResponseEntity<MessageDTO> decision(@PathVariable(value = "id") long id, @ModelAttribute DecisionDTO decision) throws IOException {

        Programme programme =programmeService.findProgramme(id);

        if(programme==null){
            return ResponseEntity.ok(new MessageDTO("erreur","programme inexistant"));
        }
        if(programme.getStatut()!=ProgrammeStatut.SOUMIS){
            return ResponseEntity.ok(new MessageDTO("erreur","impossible car le programme n'a pas été soumis"));
        }
        if(decision.getFile()!=null){
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
        programmeService.saveProgramme(programme);

        return ResponseEntity.ok(new MessageDTO("succes","votre demmande a été executé"));
    }

    @GetMapping("/programme/getResolution/{id}")
    public ResponseEntity getResolution(@PathVariable(value = "id") long id) throws IOException {

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
    public ResponseEntity getFinalProgramme(@RequestBody ProgrammeFilterDTO filter){

        List<Programme> list= programmeService.getSyntheseProgramme(filter);

        SyntheseDTO syntheseDTO=programmeService.syntheseProgramme(list, filter.getOrdonnateur());

        return ResponseEntity.ok(syntheseDTO);
    }
}





package com.api.backend.Fonds_routier.controller;

import com.api.backend.Fonds_routier.DTO.*;
import com.api.backend.Fonds_routier.model.Action;
import com.api.backend.Fonds_routier.model.Role;
import com.api.backend.Fonds_routier.model.Utilisateur;
import com.api.backend.Fonds_routier.service.AccountService;
import com.api.backend.Fonds_routier.service.HTTPService;
import com.api.backend.Fonds_routier.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
public class AcountController {

    @Autowired
    private AccountService accountService;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private RoleService roleService;

    @Autowired
    JwtDecoder jwtDecoder;

    @PostMapping("/login")
    public ResponseEntity<Object> authentification(@RequestBody LoginDTO loginDTO){

        if(loginDTO.getUsername()==null || loginDTO.getPassword()==null){

           return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("requête incorrecte");
        }

        Utilisateur utilisateur=accountService.findUserByUsername(loginDTO.getUsername());

        if( utilisateur==null || !(passwordEncoder.matches(loginDTO.getPassword(),utilisateur.getPassword())) ){

            return ResponseEntity.ok(new ResLoginDTO(false,""));
        }

        return ResponseEntity.ok(new ResLoginDTO(true, accountService.generateToken( utilisateur )));
    }

    @GetMapping("/profil")
    public Utilisateur profil(@RequestHeader("Authorization") String token){

        Jwt jwt=jwtDecoder.decode(token.substring(7));
        Utilisateur utilisateur= accountService.findUserByUsername(jwt.getSubject());

        return utilisateur;
    }

    @Secured("ADMIN")
    @PostMapping("/createUser")
    public ResponseEntity<MessageDTO> createUser(@RequestBody UserDTO userDTO) throws IOException, InterruptedException {

        if(userDTO.getNom().isEmpty() ||userDTO.getPrenom().isEmpty() ||userDTO.getEmail().isEmpty() ||userDTO.getUsername().isEmpty()){

            return ResponseEntity.ok(new MessageDTO("erreur","veuillez remplir tous les champs"));
        }

        if(Long.toString(userDTO.getTelephone()).length()!=9 || Long.toString(userDTO.getTelephone()).indexOf("6")!=0 ) {

            return ResponseEntity.ok(new MessageDTO("erreur","numéro de téléphone incorrect"));
        }

        Utilisateur user=accountService.findUserByUsername(userDTO.getUsername());
        if(user!=null){
            return ResponseEntity.ok(new MessageDTO("erreur","Le username que vous avez entré est déja utilisé, veuillez choisir un autre"));
        }

        Role role=roleService.getRoleByRoleName(userDTO.getRoleName());

        if(role==null){
            return ResponseEntity.ok(new MessageDTO("erreur","veuillez remplir correctement le champs role"));
        }

        accountService.saveUser(userDTO,role);

        try {
            HTTPService service = new HTTPService();
            service.httpRequest(List.of("237" + userDTO.getTelephone()), "Votre compte viens d'être créé sur la plateforme coweb-FR, votre mot de passe est par défaut est: fonds*2024");
        }catch (ConnectException e){
            e.printStackTrace();
        }
        return ResponseEntity.ok(new MessageDTO("succes","utlisateur créé avec succès"));

    }

    @PutMapping("/updateUser")
    public ResponseEntity<MessageDTO> updateProfil(@RequestHeader("Authorization") String token ,@RequestBody Utilisateur update){

        if(update.getNom()=="" ||update.getPrenom()=="" ||update.getEmail()==""){

            return ResponseEntity.ok(new MessageDTO("erreur","veuillez remplir tous les champs"));
        }

        if(Long.toString(update.getTelephone()).length()!=9 || Long.toString(update.getTelephone()).indexOf("6")!=0 ) {

            return ResponseEntity.ok(new MessageDTO("erreur","numéro de téléphone incorrect"));
        }

        Jwt jwt=jwtDecoder.decode(token.substring(7));
        Utilisateur utilisateur= accountService.findUserByUsername(jwt.getSubject());

        accountService.updateUser(utilisateur,update);

        return ResponseEntity.ok(new MessageDTO("succes","Profil modifié avec succès"));

    }

    @PutMapping("/updatePassword")
    public ResponseEntity<MessageDTO> updatePassword(@RequestHeader("Authorization") String token ,@RequestBody PasswordDTO passwordDTO){

        if(passwordDTO.getPassword()=="" ||passwordDTO.getConfirmPassword()=="" || passwordDTO.getNewPassword()=="" ){

            return ResponseEntity.ok(new MessageDTO("erreur","veuillez remplir tous les champs"));
        }
        if( !passwordDTO.getNewPassword().equals(passwordDTO.getConfirmPassword()) ){

            return ResponseEntity.ok(new MessageDTO("erreur","Mot de passe de confirmation incorrect"));
        }

        Jwt jwt=jwtDecoder.decode(token.substring(7));
        Utilisateur utilisateur= accountService.findUserByUsername(jwt.getSubject());

        if( !passwordEncoder.matches(passwordDTO.getPassword(),utilisateur.getPassword())  ){
            return ResponseEntity.ok(new MessageDTO("erreur","Mot de passe actuel incorrect, veuillez réessayer avec le bon mot de passe"));
        }

        utilisateur.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
        accountService.save(utilisateur);

        return ResponseEntity.ok(new MessageDTO("succes","Mot de passe modifié avec succès"));

    }

    @Secured("ADMIN")
    @DeleteMapping("/deleteUser/{id}")
    public ResponseEntity<MessageDTO> deleteUser(@RequestHeader("Authorization") String token, @PathVariable(value = "id") long id ){

        Utilisateur utilisateur= accountService.findUser(id);
        Jwt jwt=jwtDecoder.decode(token.substring(7));
        if(utilisateur==null){
            return ResponseEntity.ok(new MessageDTO("erreur","Cet utilisateur n'existe pas"));
        }
        if(utilisateur.getUsername().equals(jwt.getSubject())){
            return ResponseEntity.ok(new MessageDTO("erreur","Vos ne pouvez pas supprimer votre propre compte"));
        }
        accountService.deleteUser(id);
        return ResponseEntity.ok(new MessageDTO("succes","Cet utilisateur à bien été supprimé"));

    }

    @Secured("ADMIN")
    @PutMapping("/resetPassword/{id}")
    public ResponseEntity<MessageDTO> resetPassword( @PathVariable(value = "id") long id ) throws IOException {

        Utilisateur utilisateur= accountService.findUser(id);
        if(utilisateur==null){
            return ResponseEntity.ok(new MessageDTO("erreur","Cet utilisateur n'existe pas"));
        }
        utilisateur.setPassword(passwordEncoder.encode(AccountService.defaultPassword));
        accountService.save(utilisateur);

        try {
            HTTPService service = new HTTPService();
            service.httpRequest(List.of("237" + utilisateur.getTelephone()), "Votre mot de passe vient d'être réinitialisé sur la plateforme coweb-FR du Fonds routier");
        }catch (ConnectException  | InterruptedException e){
            e.printStackTrace();
        }

        return ResponseEntity.ok(new MessageDTO("succes","Mot de passe réinitialiser"));
    }

    @Secured("ADMIN")
    @PostMapping("/role/saveRole")
    public ResponseEntity<MessageDTO> saveRole(@RequestBody Role role){

        if(role.getRoleName().isEmpty()){
            return ResponseEntity.ok(new MessageDTO("erreur","veuillez remplir tous les champs"));
        }
        Role roles=roleService.getRoleByRoleName(role.getRoleName());

        if(roles!=null){
            return ResponseEntity.ok(new MessageDTO("erreur","Ce role existe déja dans la plateforme, veuillez ajouter un autre role"));
        }
        roleService.saveRole(role);

        return ResponseEntity.ok(new MessageDTO("succes","Role enregistré avec succès"));
    }

    @Secured("ADMIN")
    @GetMapping("/getAllUser")
    public List<Utilisateur> getAllUser( ){

        return accountService.getAllUser();
    }

    @Secured("ADMIN")
    @GetMapping("/role/getAllRole")
    public List<Role> getAllRole( ){

        return roleService.getAllRole();
    }

    @Secured("ADMIN")
    @DeleteMapping("/role/deleteRole/{id}")
    public ResponseEntity<MessageDTO> deleteRole(@PathVariable(value = "id") long id ){

        Role role= roleService.findRole(id);
        if(role==null){

            return ResponseEntity.ok(new MessageDTO("erreur","Ce role n'existe pas"));
        }

        Utilisateur utilisateur= accountService.getUserByRole(role);
        if(utilisateur!=null){
            return ResponseEntity.ok(new MessageDTO("erreur","Impossible de supprimer ce role car il est utilisé"));
        }

        roleService.deleteRole(id);
        return ResponseEntity.ok(new MessageDTO("succes","Le role à bien été supprimé"));

    }

    @Secured("ADMIN")
    @PostMapping("/action/getActionByPeriode")
    public ResponseEntity<Object> getAction(@RequestBody ActionFormDTO filter){

        List<String> periode=List.of("TODAY","WEEK","MONTH","PERIODE");

        List<Action> actions;

        if(!periode.contains(filter.getPeriode())){

            return ResponseEntity.ok(new MessageDTO("erreur","veuillez remplir correctement les champs"));
        }
        if(filter.getPeriode().equals("PERIODE") &&(filter.getFirstDate()==null || filter.getSecondDate()==null)){

            return ResponseEntity.ok(new MessageDTO("erreur","veuillez remplir tous les champs"));
        }
        if(filter.getPeriode().equals("PERIODE") && filter.getSecondDate().before(filter.getFirstDate())){

            return ResponseEntity.ok(new MessageDTO("erreur","la première date doit être plus récente que la deuxième"));
        }

        if(filter.getPeriode().equals("PERIODE")){

            filter.getFirstDate().set(Calendar.HOUR_OF_DAY,0);
            filter.getSecondDate().set(Calendar.HOUR_OF_DAY,23);

            actions=accountService.findActionByDate(filter.getFirstDate().getTime(),filter.getSecondDate().getTime());

        }else if(filter.getPeriode().equals("TODAY")){

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY,0);
            Date date = calendar.getTime();

            actions=accountService.findActionByDate(date,new Date());

        }else if(filter.getPeriode().equals("WEEK")){

            Calendar calendar = Calendar.getInstance();
            calendar.setFirstDayOfWeek(Calendar.MONDAY);
            calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
            calendar.set(Calendar.HOUR_OF_DAY,0);
            Date date = calendar.getTime();

            actions=accountService.findActionByDate(date,new Date());
        }else{
            Calendar calendar=Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH,1);
            calendar.set(Calendar.HOUR_OF_DAY,0);
            Date date=calendar.getTime();

            actions=accountService.findActionByDate(date,new Date());
        }

        return ResponseEntity.ok(actions);

    }

}

package com.api.backend.Fonds_routier.controller;

import com.api.backend.Fonds_routier.DTO.LoginDTO;
import com.api.backend.Fonds_routier.DTO.MessageDTO;
import com.api.backend.Fonds_routier.DTO.PasswordDTO;
import com.api.backend.Fonds_routier.DTO.ResLoginDTO;
import com.api.backend.Fonds_routier.model.Utilisateur;
import com.api.backend.Fonds_routier.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class AcountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    JwtDecoder jwtDecoder;

    @PostMapping("/login")
    public ResponseEntity authentification(@RequestBody LoginDTO loginDTO){

        if(loginDTO.getUsername()==null || loginDTO.getPassword()==null){

           return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("requête incorrecte");

        }else{
            Utilisateur utilisateur=accountService.findUserByUsername(loginDTO.getUsername());

            if(utilisateur==null || !(utilisateur.getPassword().equals(loginDTO.getPassword())) ){

                return ResponseEntity.ok(new ResLoginDTO(false,""));
            }

            return ResponseEntity.ok(new ResLoginDTO(true,
                    accountService.generateToken( utilisateur )
                    ));

        }
    }

    @GetMapping("/profil")
    public Utilisateur profil(@RequestHeader("Authorization") String token){

        Jwt jwt=jwtDecoder.decode(token.substring(7));
        Utilisateur utilisateur= accountService.findUser(jwt.getClaim("ref"));

        return utilisateur;
    }


    @PostMapping("/createUser")
    public ResponseEntity<MessageDTO> createUser(@RequestBody Utilisateur utilisateur){

        Utilisateur user=accountService.findUserByUsername(utilisateur.getUsername());

        if(user!=null){
            return ResponseEntity.ok(new MessageDTO("erreur","Le username que vous avez entré est déja utilisé, veuillez choisir un autre"));
        }

        utilisateur.setPassword("fonds*2024");
        accountService.saveUser(utilisateur);

        return ResponseEntity.ok(new MessageDTO("succes","utlisateur créé avec succès"));

    }

    @PutMapping("/updateUser")
    public ResponseEntity<MessageDTO> updateProfil(@RequestHeader("Authorization") String token ,@RequestBody Utilisateur update){

        if(update.getNom()=="" ||update.getPrenom()=="" ||update.getEmail()=="" ||update.getUsername()==""){

            return ResponseEntity.ok(new MessageDTO("erreur","veuillez remplir tous les champs"));
        }

        if(Long.toString(update.getTelephone()).length()!=9 || Long.toString(update.getTelephone()).indexOf("6")!=0 ) {

            return ResponseEntity.ok(new MessageDTO("erreur","numéro de téléphone incorrect"));
        }

        Jwt jwt=jwtDecoder.decode(token.substring(7));
        Utilisateur utilisateur= accountService.findUser(jwt.getClaim("ref"));

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
        Utilisateur utilisateur= accountService.findUser(jwt.getClaim("ref"));

        if( !utilisateur.getPassword().equals(passwordDTO.getPassword()) ){
            return ResponseEntity.ok(new MessageDTO("erreur","Mot de passe actuel incorrect, veuillez réessayer avec le bon mot de passe"));
        }

        utilisateur.setPassword(passwordDTO.getNewPassword());
        accountService.saveUser(utilisateur);

        return ResponseEntity.ok(new MessageDTO("succes","Mot de passe modifié avec succès"));

    }

    @GetMapping("/getAllUser")
    public List<Utilisateur> getAllUser(@RequestHeader("Authorization") String token ){

        Jwt jwt=jwtDecoder.decode(token.substring(7));
        return accountService.getAllUser(jwt.getClaim("ref"));
    }

}

package com.api.backend.Fonds_routier.service;

import com.api.backend.Fonds_routier.DTO.UserDTO;
import com.api.backend.Fonds_routier.enums.UserRole;
import com.api.backend.Fonds_routier.model.Action;
import com.api.backend.Fonds_routier.model.Role;
import com.api.backend.Fonds_routier.model.Utilisateur;
import com.api.backend.Fonds_routier.repository.ActionRepository;
import com.api.backend.Fonds_routier.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Service
public class AccountService {

    @Autowired
    JwtEncoder jwtEncoder;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    @Autowired
    private ActionRepository actionRepository;

    public static final String defaultPassword="fonds*2024";


    public Utilisateur findUserByUsername(String nom){

        return utilisateurRepository.findByUsername(nom);
    }

    public Utilisateur findUser(long id){

        return utilisateurRepository.findById(id).orElse(null);
    }

    public void updateUser(Utilisateur utilisateur, Utilisateur update){

        utilisateur.setNom(update.getNom());
        utilisateur.setPrenom(update.getPrenom());
        utilisateur.setEmail(update.getEmail());
        utilisateur.setTelephone(update.getTelephone());

        utilisateurRepository.save(utilisateur);
    }

    public void saveUser(UserDTO userDTO,Role role){

        Utilisateur utilisateur=new Utilisateur();

        utilisateur.setNom(userDTO.getNom());
        utilisateur.setPrenom(userDTO.getPrenom());
        utilisateur.setUsername(userDTO.getUsername());
        utilisateur.setPassword(passwordEncoder.encode(defaultPassword));
        utilisateur.setRole(role);
        utilisateur.setEmail(userDTO.getEmail());
        utilisateur.setTelephone(userDTO.getTelephone());

        utilisateurRepository.save(utilisateur);
    }

    public void save(Utilisateur utilisateur){

        utilisateurRepository.save(utilisateur);
    }

    public void deleteUser(long id){

        utilisateurRepository.deleteById(id);
    }

    public List<Utilisateur> getAllUser(){

        return utilisateurRepository.findAll();
    }

    public Utilisateur getUserByRole(Role role){

        return utilisateurRepository.findByRole(role);
    }

    public String generateToken(Utilisateur utilisateur){

        Instant instant= Instant.now();
        JwtClaimsSet jwtClaimsSet=JwtClaimsSet.builder()
                .issuedAt(instant)
                .expiresAt(instant.plus(10, ChronoUnit.HOURS))
                .subject(utilisateur.getUsername())
                .claim("role", utilisateur.getRole().getRoleName())
                .build();
        JwtEncoderParameters jwtEncoderParameters=JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS512).build(),
                jwtClaimsSet
        );
        return jwtEncoder.encode(jwtEncoderParameters).getTokenValue();
    }

    public void saveAction(Action action){
        action.setDate(new Date());
        actionRepository.save(action);
    }

    public List<Action> findActionByDate(Date firstDate, Date secondDate){

       return actionRepository.findByDateBetween(firstDate,secondDate);
    }

}

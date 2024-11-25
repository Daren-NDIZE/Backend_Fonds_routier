package com.api.backend.Fonds_routier.service;

import com.api.backend.Fonds_routier.DTO.UserDTO;
import com.api.backend.Fonds_routier.enums.UserRole;
import com.api.backend.Fonds_routier.model.Action;
import com.api.backend.Fonds_routier.model.Role;
import com.api.backend.Fonds_routier.model.Utilisateur;
import com.api.backend.Fonds_routier.model.VerificationCode;
import com.api.backend.Fonds_routier.repository.ActionRepository;
import com.api.backend.Fonds_routier.repository.UtilisateurRepository;
import com.api.backend.Fonds_routier.repository.VerificationCodeRepository;
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
import java.util.*;

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
    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

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
        utilisateur.setAdministration(userDTO.getAdministration());
        utilisateur.setTelephone(userDTO.getTelephone());

        utilisateurRepository.save(utilisateur);
    }

    public List<Utilisateur> getUserByAdministration(String administration){

        return utilisateurRepository.findByAdministration(administration);
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
                .claim("structure", utilisateur.getAdministration())
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

    public int generetecode(){

        Random random = new Random();
        int code = 100000 + random.nextInt(900000);

        return code;
    }

    public void saveVerification(VerificationCode verificationCode){

        Date dateActuelle = new Date();

        // Utiliser Calendar pour ajouter des minutes
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateActuelle);
        calendar.add(Calendar.MINUTE, 3);
        verificationCode.setExpiredAt(calendar.getTime());

        verificationCodeRepository.save(verificationCode);
    }
}

package com.api.backend.Fonds_routier.service;

import com.api.backend.Fonds_routier.enums.UserRole;
import com.api.backend.Fonds_routier.model.Utilisateur;
import com.api.backend.Fonds_routier.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AccountService {

    @Autowired
    JwtEncoder jwtEncoder;
    @Autowired
    private UtilisateurRepository utilisateurRepository;


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
        utilisateur.setUsername(update.getUsername());

        utilisateurRepository.save(utilisateur);
    }


    public void savaUser(Utilisateur utilisateur){

        utilisateurRepository.save(utilisateur);
    }

    public List<Utilisateur> getAllUser(long id){

        return utilisateurRepository.findByIdIsNot(id);
    }

    public String generateToken(Utilisateur utilisateur){

        Instant instant= Instant.now();
        JwtClaimsSet jwtClaimsSet=JwtClaimsSet.builder()
                .issuedAt(instant)
                .expiresAt(instant.plus(10, ChronoUnit.HOURS))
                .subject(utilisateur.getUsername())
                .claim("role",utilisateur.getRole())
                .claim("ref",utilisateur.getId())
                .build();
        JwtEncoderParameters jwtEncoderParameters=JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS512).build(),
                jwtClaimsSet
        );
        return jwtEncoder.encode(jwtEncoderParameters).getTokenValue();
    }
}

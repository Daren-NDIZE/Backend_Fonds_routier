package com.api.backend.Fonds_routier.config;

import com.api.backend.Fonds_routier.model.Role;
import com.api.backend.Fonds_routier.model.Utilisateur;
import com.api.backend.Fonds_routier.service.AccountService;
import com.api.backend.Fonds_routier.service.RoleService;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.spec.SecretKeySpec;
import java.util.List;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class securityConfig {

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {

        return httpSecurity
                .csrf(csrf->csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(ar->ar.requestMatchers("/login").permitAll())
                .authorizeHttpRequests(ar->ar.anyRequest().authenticated())
                .oauth2ResourceServer(oa->oa.jwt(Customizer.withDefaults()))
                .oauth2ResourceServer(oa->oa.jwt(j->j.jwtAuthenticationConverter(new CustomJwtAuthoritiesConverter() )))
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    @Bean
    JwtEncoder jwtEncoder(){

        String secretKey="avcmgàerk524ùmji@52fhv3fm8fhànvfçvcmgàerk524ùmji@52fhv3fm8fhc8vf";

        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey.getBytes())) ;
    }

    @Bean
    JwtDecoder jwtDecoder(){

        String secretKey="avcmgàerk524ùmji@52fhv3fm8fhànvfçvcmgàerk524ùmji@52fhv3fm8fhc8vf";

        SecretKeySpec secretKeySpec=new SecretKeySpec(secretKey.getBytes(),"RSA");
        return NimbusJwtDecoder.withSecretKey(secretKeySpec).macAlgorithm(MacAlgorithm.HS512).build();
    }


    @Bean
    CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration corsConfiguration =new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.addAllowedHeader("*");
        //corsConfiguration.setExposedHeaders(List.of("x-auth-token"));
        UrlBasedCorsConfigurationSource source=new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",corsConfiguration);

        return source;
    }

    @Bean
    CommandLineRunner initDatabase(AccountService accountService, RoleService roleService) {

        return args->{

            List<Utilisateur> utilisateurs=accountService.getAllUser();

            if(utilisateurs.isEmpty()){

                Role role= new Role();
                role.setRoleName("ADMIN");
                role.setDescription("Administrateur de l'application");
                roleService.saveRole(role);
                accountService.save(new Utilisateur(0,"fondsroutier ","FR","fondsroutier",0,"admin@gmail.com",role, passwordEncoder().encode(AccountService.defaultPassword)));
                System.out.println("data initialized");
            }
        };
    }
}

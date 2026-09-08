package com.hostelhub.identityservice.config;

import com.hostelhub.identityservice.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {


    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    AuthenticationProvider authenticationProvider(
            CustomUserDetailsService service,
            PasswordEncoder encoder
    ) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(service);

        provider.setPasswordEncoder(encoder);

        return provider;
    }
}
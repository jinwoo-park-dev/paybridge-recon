package com.paybridge.recon.support.security;

import com.paybridge.recon.support.config.PayBridgeReconProperties;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.Assert;

@Configuration
@EnableMethodSecurity
public class PayBridgeReconSecurityConfiguration {

    private final PayBridgeReconProperties properties;

    public PayBridgeReconSecurityConfiguration(PayBridgeReconProperties properties) {
        this.properties = properties;
    }

    @Bean
    SecurityFilterChain payBridgeReconSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                .requestMatchers(
                    "/operator/login",
                    "/error",
                    "/actuator/health",
                    "/api/system/info",
                    "/swagger-ui/**",
                    "/api-docs/**",
                    "/v3/api-docs/**"
                ).permitAll()
                .requestMatchers(HttpMethod.GET, "/css/**", "/js/**").permitAll()
                .anyRequest().hasRole("OPERATOR")
            )
            .formLogin(form -> form
                .loginPage("/operator/login")
                .loginProcessingUrl("/operator/login")
                .defaultSuccessUrl("/", true)
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/operator/logout")
                .logoutSuccessUrl("/operator/login?logout")
                .permitAll());

        return http.build();
    }

    @Bean
    UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        String username = properties.getSecurity().getOperatorUsername();
        String password = properties.getSecurity().getOperatorPassword();
        Assert.hasText(username, "paybridge-recon.security.operator-username must be configured");
        Assert.hasText(password, "paybridge-recon.security.operator-password must be configured");

        return new InMemoryUserDetailsManager(
            User.withUsername(username)
                .password(passwordEncoder.encode(password))
                .roles(properties.getSecurity().getRoles().toArray(String[]::new))
                .build()
        );
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

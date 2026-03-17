package FinalYearProject.StaffComplaintMgmtSystem.utils;


import FinalYearProject.StaffComplaintMgmtSystem.jwt.JwtAuthFilter;
import FinalYearProject.StaffComplaintMgmtSystem.jwt.JwtUtil;
import FinalYearProject.StaffComplaintMgmtSystem.repository.StaffIdentityRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * Updated SecurityConfig.
 * Key changes from your original:
 *  - Added @EnableMethodSecurity so @PreAuthorize works on controllers
 *  - Registered JwtAuthFilter before UsernamePasswordAuthenticationFilter
 *  - Changed session policy to STATELESS (JWT-based, no server sessions)
 *  - Added CORS for your Node.js frontend on port 3000
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // <-- REQUIRED for @PreAuthorize("hasAuthority('ADMIN')")
public class SecurityConfig {

    @Bean
    public JwtAuthFilter jwtAuthFilter(JwtUtil jwtUtil, StaffIdentityRepo repo) {
        return new JwtAuthFilter(jwtUtil, repo);
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable())
//                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//                .sessionManagement(sess ->
//                        sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )
//                .authorizeHttpRequests(auth -> auth
//                        // Public: login, signup
//                        .requestMatchers("/api/v1/staffcomplaintmgmtsystem/auth/**").permitAll()
//                        .requestMatchers("/api/v1/staffcomplaintmgmtsystem/ADMIN/**").permitAll()
//                        // Everything else requires authentication;
//                        // fine-grained RBAC handled by @PreAuthorize on controllers
//                        .anyRequest().authenticated()
//                )
//                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,JwtAuthFilter jwtAuthFilter) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sess ->
                        sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth  // ✅ NOT authorizeRequests()
                        .requestMatchers("/api/v1/staffcomplaintmgmtsystem/auth/**").permitAll()
                        .requestMatchers("/api/v1/staffcomplaintmgmtsystem/ADMIN/**").permitAll()
                        .anyRequest().authenticated()    // ✅ let @PreAuthorize handle role checks
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); // ✅ use the injected parameter

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
    }


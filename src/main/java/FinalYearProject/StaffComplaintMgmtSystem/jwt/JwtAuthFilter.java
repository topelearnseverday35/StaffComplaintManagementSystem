package FinalYearProject.StaffComplaintMgmtSystem.jwt;

import FinalYearProject.StaffComplaintMgmtSystem.entities.StaffIdentity;
import FinalYearProject.StaffComplaintMgmtSystem.repository.StaffIdentityRepo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final StaffIdentityRepo staffIdentityRepository;

    public JwtAuthFilter(JwtUtil jwtUtil, StaffIdentityRepo staffIdentityRepository) {
        this.jwtUtil = jwtUtil;
        this.staffIdentityRepository = staffIdentityRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

//        String requestPath = request.getRequestURI();
//        log.debug("JwtAuthFilter running for: {}", requestPath);
//
//        // ── Step 1: Read Authorization header ────────────────────
//        String authHeader = request.getHeader("Authorization");
//
////        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
////            log.warn("No Bearer token found in request to: {}", requestPath);
////            filterChain.doFilter(request, response);
////            return;
////        }
////
////        String token = authHeader.substring(7);
////        log.debug("Bearer token extracted, length: {}", token.length());
//
////        // ── Step 2: Validate the token ────────────────────────────
////        if (!jwtUtil.validateToken(token)) {
////            log.warn("Token validation FAILED for request to: {}", requestPath);
////            filterChain.doFilter(request, response);
////            return;
////        }
//
//        // Add in JwtAuthFilter after token validation
//
//        if (authHeader != null && authHeader.startsWith("Bearer ")) {
//            String token = authHeader.substring(7); // Extract token from header
//
//            List<String> rolesInToken = jwtUtil.extractRoles(token);
//        log.info(">>> Roles inside JWT: {}", rolesInToken);  // ROLE_LECTURER or ROLE_ROLE_LECTURER?
//
//        // ── Step 3: Extract email from token ──────────────────────
//        String email = jwtUtil.extractUsername(token);
//        log.info("Token valid — extracted email: {}", email);
//
//        if (email == null) {
//            log.warn("Email extracted from token is null — cannot authenticate");
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        // ── Step 4: Skip if already authenticated ─────────────────
//        if (SecurityContextHolder.getContext().getAuthentication() != null) {
//            log.debug("Security context already has authentication — skipping");
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        // ── Step 5: Look up StaffIdentity in database ─────────────
//        boolean existsInDb = staffIdentityRepository.findByStaffEmail(email).isPresent();
//        log.info("Staff found in DB for email [{}]: {}", email, existsInDb);
//
//        if (!existsInDb) {
//            log.warn("No StaffIdentity record found for email: {} — rejecting request", email);
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        // ── Step 6: Load staff and set authentication ─────────────
//        staffIdentityRepository.findByStaffEmail(email).ifPresent(staffIdentity -> {
//            log.info("Setting auth for: [{}] | Role: [{}] | Authorities: {}",
//                    email,
//                    staffIdentity.getRole(),
//                    staffIdentity.getAuthorities());
//
//            UsernamePasswordAuthenticationToken auth =
//                    new UsernamePasswordAuthenticationToken(
//                            staffIdentity,                  // full object as principal
//                            null,
//                            staffIdentity.getAuthorities()  // role-based authorities
//                    );
//            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//            log.info("Authorities being set: {}", staffIdentity.getAuthorities());
//            SecurityContextHolder.getContext().setAuthentication(auth);
//
//            log.info("Authentication successfully set for: {}", email);
//        });
//
//        // ── Step 7: Continue filter chain ─────────────────────────
//        filterChain.doFilter(request, response);
       log.info(">>> FILTER RUNNING for: " + request.getRequestURI());
        String authHeader = request.getHeader("Authorization");
        log.info(">>> Auth Header: " + authHeader);


        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);
                List<String> roles = jwtUtil.extractRoles(token);
                log.info(">>> Username: " + username);
                log.info(">>> Roles from token: " + roles);

                List<GrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                log.info("Authenticated user: {}, roles: {}", username, roles);

                staffIdentityRepository.findByStaffEmail(username).ifPresent(staffIdentity -> {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    staffIdentity,  // ✅ full object as principal
                                    null,
                                    authorities     // still from token
                            );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    log.info(">>> Auth set in SecurityContext: " + SecurityContextHolder.getContext().getAuthentication());
                });


            }
         else {
            System.out.println(">>> Token validation FAILED");
        }
    } else {
        System.out.println(">>> No Bearer token found");
    }


        filterChain.doFilter(request, response);
    }
}
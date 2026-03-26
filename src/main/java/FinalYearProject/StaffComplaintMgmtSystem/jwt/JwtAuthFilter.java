package FinalYearProject.StaffComplaintMgmtSystem.jwt;

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

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            log.warn("Invalid or expired JWT for request: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        // Skip if already authenticated in this request
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtUtil.extractUsername(token);
        if (email == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Roles stored in token without ROLE_ prefix ("HOD", "DEAN", "LECTURER")
        // Build GrantedAuthority list directly — no prefix manipulation needed
        List<String> roles = jwtUtil.extractRoles(token);
        List<GrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        log.debug("Authenticating {} with authorities: {}", email, authorities);

        // Load the full StaffIdentity object so ComplaintService can read
        // the department, role, and staffId from the security principal
        staffIdentityRepository.findByStaffEmail(email).ifPresent(staffIdentity -> {
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            staffIdentity,   // principal — full object, NOT just email string
                            null,
                            authorities      // authorities from the JWT
                    );
            SecurityContextHolder.getContext().setAuthentication(auth);
        });

        filterChain.doFilter(request, response);
    }
}

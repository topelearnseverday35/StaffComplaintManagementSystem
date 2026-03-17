package FinalYearProject.StaffComplaintMgmtSystem.jwt;



import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtUtil {

//    private final CustomUserDetailsService detailsService;

    @Value("${application.secret.key}")
    private String SecretKey;
    private static final long EXPIRATION = 1000 * 60 * 15; // 15 minutes
    private static final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 7; // 7 days


    private Key getSignInKey(){
        byte[] keyBytes = Decoders.BASE64.decode(SecretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    // ✅ Generate JWT token with roles claim
    public String  generateToken(UserDetails userDetails) {
        log.info("Generating JWT token for {}", userDetails.getUsername());

        // Convert authorities to a list of role strings
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        log.info("Authorities : {}", authorities);
        List<String> roles = authorities.stream()
                .map(authority -> "ROLE_" + authority.getAuthority())
                .collect(Collectors.toList());
        log.info("Roles : {}", roles);


        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ Extract username (subject)
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    // ✅ Extract roles safely
    public List<String> extractRoles(String token) {
        Object roles = getClaims(token).get("roles");
        if (roles instanceof List<?>) {
            return ((List<?>) roles).stream()
                    .filter(role -> role instanceof String)
                    .map(role -> (String) role)
                    .collect(Collectors.toList());
        }
        return List.of(); // fallback
    }

    // ✅ Validate token expiration
    public boolean validateToken(String token) {

        try {
            return !getClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    // ✅ Private helper to extract all claims
    private Claims getClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

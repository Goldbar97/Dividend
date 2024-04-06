package zerobase.dividend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import zerobase.dividend.service.MemberService;

import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TokenProvider {
    
    private static final String KEY_ROLES = "roles";
    private static final long TOKEN_EXPIRE_TIME = 1000 * 60 * 60; // 1 hour
    private final MemberService memberService;
    @Value("${spring.jwt.secret}")
    private String secretKey;
    
    public String generateToken(String username, List<String> roles) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + TOKEN_EXPIRE_TIME);
        
        return Jwts.builder()
                .claim(KEY_ROLES, roles)
                .subject(username)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }
    
    public Authentication getAuthentication(String jwt) {
        UserDetails userDetails = memberService.loadUserByUsername(
                getUsername(jwt));
        
        return new UsernamePasswordAuthenticationToken(
                userDetails, "", userDetails.getAuthorities());
    }
    
    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }
    
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser().setSigningKey(secretKey).build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
    
    public boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) return false;
        
        Claims claims = parseClaims(token);
        return !claims.getExpiration().before(new Date());
    }
}

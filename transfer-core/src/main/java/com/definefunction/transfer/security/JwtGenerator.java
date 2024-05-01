package com.definefunction.transfer.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.DecodingException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtGenerator {

    public String generateToken(Authentication authentication) {
        String username= authentication.getName();
        Date currentDate = new Date();
        Date expiryDate = new Date(currentDate.getTime()+ SecurityConstants.JWT_EXPIRATION);

        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(currentDate)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, SecurityConstants.JWT_SECERT)
                .compact();
        return token;
    }

    private String cleanBearerPrefix(String token) {
        // Check if the token starts with "Bearer "
        if (token != null && token.startsWith("Bearer ")) {
            // If yes, remove the "Bearer " prefix
            return token.substring("Bearer ".length());
        }
        // If no, return the original token
        return token;
    }

    public String getUsernameFromJWT(String token) {

        Claims claims = Jwts.parser()
                .setSigningKey(SecurityConstants.JWT_SECERT)
                .parseClaimsJws(cleanBearerPrefix(token))
                .getBody();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(SecurityConstants.JWT_SECERT).parseClaimsJws(cleanBearerPrefix(token));
            return true;
        } catch (ExpiredJwtException ex) {
            throw new AuthenticationCredentialsNotFoundException("JWT token is expired");
        } catch (MalformedJwtException ex) {
            throw new AuthenticationCredentialsNotFoundException("Malformed JWT token");
        } catch (SignatureException ex) {
            throw new AuthenticationCredentialsNotFoundException("Invalid JWT token signature");
        } catch (DecodingException ex) {
            throw new AuthenticationCredentialsNotFoundException("Invalid JWT token decoding");
        }
        catch (Exception ex) {
            throw new AuthenticationCredentialsNotFoundException("JWT token is not valid");
        }
    }
}

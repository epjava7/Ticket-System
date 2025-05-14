package com.example.ticketapp.service;

import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import io.jsonwebtoken.ExpiredJwtException;
import java.util.Date;

@Service
public class JwtService {
    
    @Value("${jwt.secret}")
    private String secretKey;

    public String generateToken(String username) {
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 5 * 60 * 1000)) // 5 minutes in miliseconds
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact();
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 1000)) // 7 days in miliseconds
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser()
            .setSigningKey(secretKey)
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }

    public boolean isValid(String token) {
        try {
            boolean valid = Jwts.parser().setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .before(new Date());
            return !valid;
        } catch (Exception e) {
            return false;
        } 
    }
}

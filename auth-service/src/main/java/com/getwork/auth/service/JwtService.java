package com.getwork.auth.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expires-in-ms:3600000}")
    private long jwtExpirationMs;

    // ------------------------------
    // 🔹 1. TOKEN GENERATION METHODS
    // ------------------------------

    public String generateToken(UserDetails userDetails) {
        // Basic token using Spring’s UserDetails
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        // Add custom claims if needed (e.g., phone, role)
        extraClaims.putIfAbsent("role", userDetails.getAuthorities().toString());
        extraClaims.putIfAbsent("username", userDetails.getUsername());
        return buildToken(extraClaims, userDetails.getUsername(), jwtExpirationMs);
    }

    // Overloaded method for microservices — no UserDetails required
    public String generateToken(String userId, String phone, String role) {
        Map<String, Object> claims = Map.of(
                "phone", phone,
                "role", role
        );
        return buildToken(claims, userId, jwtExpirationMs);
    }

    private String buildToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ------------------------------
    // 🔹 2. TOKEN VALIDATION METHODS
    // ------------------------------

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false; // invalid signature, expired, malformed, etc.
        }
    }

    // ------------------------------
    // 🔹 3. TOKEN EXTRACTION METHODS
    // ------------------------------

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return (String) extractAllClaims(token).get("role");
    }

    public String extractPhone(String token) {
        return (String) extractAllClaims(token).get("phone");
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ------------------------------
    // 🔹 4. KEY CONFIGURATION
    // ------------------------------

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ------------------------------
    // 🔹 5. EXPIRATION ACCESSOR
    // ------------------------------

    public long getExpirationTime() {
        return jwtExpirationMs;
    }
}

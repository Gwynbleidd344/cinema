package com.example.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class JwtService {

  private final Key signingKey;
  private final long expirationMs;

  public JwtService(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.expiration-ms}") long expirationMs) {
    this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());
    this.expirationMs = expirationMs;
  }

  public String generateToken(UUID userId, String email, String role) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + expirationMs);
    return Jwts.builder()
        .setSubject(userId.toString())
        .claim("email", email)
        .claim("role", role)
        .setIssuedAt(now)
        .setExpiration(expiry)
        .signWith(signingKey, SignatureAlgorithm.HS256)
        .compact();
  }

  public UUID extractUserId(String token) {
    return UUID.fromString(extractClaim(token, Claims::getSubject));
  }

  public String extractRole(String token) {
    return extractClaim(token, claims -> claims.get("role", String.class));
  }

  public boolean isTokenValid(String token) {
    try {
      Date expiration = extractClaim(token, Claims::getExpiration);
      return expiration.after(new Date());
    } catch (Exception e) {
      return false;
    }
  }

  private <T> T extractClaim(String token, Function<Claims, T> resolver) {
    Claims claims =
        Jwts.parser().setSigningKey(signingKey).build().parseClaimsJws(token).getBody();
    return resolver.apply(claims);
  }
}

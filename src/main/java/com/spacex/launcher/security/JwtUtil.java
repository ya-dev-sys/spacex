package com.spacex.launcher.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private final Key key;
    private final long expirationMs;
    private final String tokenPrefix;

    public JwtUtil(@Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs,
            @Value("${jwt.token-prefix:Bearer}") String tokenPrefix) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        this.tokenPrefix = tokenPrefix;
        logger.debug("JwtUtil initialized with tokenPrefix='{}' and expirationMs={}", tokenPrefix, expirationMs);
    }

    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);
        var roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        String token = Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        logger.info("Generated JWT for user='{}' expiresAt={}", userDetails.getUsername(), exp);
        return token;
    }

    public String extractUsername(String token) {
        try {
            String username = extractClaim(token, Claims::getSubject);
            logger.debug("Extracted username from token: {}", username);
            return username;
        } catch (Exception ex) {
            logger.warn("Failed to extract username from token: {}", ex.getMessage());
            throw ex;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = parseClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            boolean valid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);
            logger.debug("Token validation for user='{}' => {}", username, valid);
            return valid;
        } catch (JwtException | IllegalArgumentException ex) {
            logger.warn("Token validation error: {}", ex.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        final Date exp = extractClaim(token, Claims::getExpiration);
        boolean expired = exp.before(new Date());
        if (expired)
            logger.debug("Token is expired at {}", exp);
        return expired;
    }

    public String stripPrefix(String header) {
        if (header == null) {
            logger.trace("Authorization header is null");
            return null;
        }
        if (header.startsWith(tokenPrefix + " ")) {
            String stripped = header.substring((tokenPrefix + " ").length());
            logger.trace("Stripped token prefix");
            return stripped;
        }
        logger.trace("No token prefix present in header");
        return header;
    }
}

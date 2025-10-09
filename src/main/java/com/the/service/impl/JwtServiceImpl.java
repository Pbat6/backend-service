package com.the.service.impl;

import com.the.exception.InvalidDataException;
import com.the.service.JwtService;
import com.the.util.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.expiryMinute}")
    private long expiryMinute;

    @Value("${jwt.expiryDay}")
    private long expiryDay;

    @Value("${jwt.accessKey}")
    private String accessKey;

    @Value("${jwt.refreshKey}")
    private String refreshKey;

    @Value("${jwt.resetKey}")
    private String resetKey;

    private <T> T extractClaim(String token, TokenType type, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaim(token, type);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaim(String token, TokenType type) {
        return Jwts.parserBuilder().setSigningKey(getKey(type)).build().parseClaimsJws(token).getBody();
    }

    public Date extractExpiration(String token, TokenType type) {
        return extractClaim(token, type, Claims::getExpiration);
    }

    private Key getKey(TokenType type) {
        log.info("---------- getKey ----------");
        switch (type) {
            case ACCESS_TOKEN -> {
                return Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessKey));
            }
            case REFRESH_TOKEN -> {
                return Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshKey));
            }
            case RESET_TOKEN -> {
                return Keys.hmacShaKeyFor(Decoders.BASE64.decode(resetKey));
            }
            default -> throw new IllegalArgumentException("Invalid token type");
        }

    }

    @Override
    public String generateAccessToken(UserDetails user) {
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * expiryMinute))
                .signWith(getKey(TokenType.ACCESS_TOKEN), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String generateRefreshToken(UserDetails user) {
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * expiryDay))
                .signWith(getKey(TokenType.REFRESH_TOKEN), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String generateResetToken(UserDetails user) {
        Map<String, Object> claims = new HashMap<>();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * expiryMinute))
                .signWith(getKey(TokenType.RESET_TOKEN), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String extractUsername(String token, TokenType type) {
        try {
            return extractClaim(token, type, Claims::getSubject);
        }catch (Exception e){
            throw new InvalidDataException("Invalid token");
        }

    }

    public boolean isTokenExpired(String token, TokenType type) {
        try {
            return extractExpiration(token, type).before(new Date());
        }catch (Exception e){
            throw new InvalidDataException("Token is expired");
        }
    }
}

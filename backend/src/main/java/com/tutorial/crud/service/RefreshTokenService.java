package com.tutorial.crud.service;

import com.tutorial.crud.entity.RefreshToken;
import com.tutorial.crud.repository.RefreshTokenRepository;
import com.tutorial.crud.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Autowired
    UserRepository userRepository;

    public RefreshToken createRefreshToken(String username){
        // 1. Récupérer l'utilisateur (Assure-toi que findByUsername renvoie Optional ou gère le null)
        var user = userRepository.findByUsername(username);

        // 2. IMPORTANT : Supprimer l'ancien token s'il existe
        // Cela évite l'erreur SQL 23505 (Unique index violation)
        refreshTokenRepository.findByUserInfo(user).ifPresent(token -> {
            refreshTokenRepository.delete(token);
        });

        // 3. Créer le nouveau
        RefreshToken refreshToken = RefreshToken.builder()
                .userInfo(user) // Si c'est un Optional, utilise .get() ou change ton repo
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(600000))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    // Ajoute cette méthode pour ton endpoint /logout
    public void deleteByUsername(String username) {
        refreshTokenRepository.findByUserInfo(userRepository.findByUsername(username))
                .ifPresent(token -> refreshTokenRepository.delete(token));
    }

    public Optional<RefreshToken> findByToken(String token){
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token){
        if(token.getExpiryDate().compareTo(Instant.now())<0){
            refreshTokenRepository.delete(token);
            throw new RuntimeException(token.getToken() + " Refresh token is expired. Please make a new login..!");
        }
        return token;
    }

    @Transactional
    public void deleteByToken(String token) {
        // findByToken doit retourner un Optional<RefreshToken>
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshTokenRepository::delete);
    }
}
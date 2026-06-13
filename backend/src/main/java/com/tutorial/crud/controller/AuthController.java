package com.tutorial.crud.controller;

import com.tutorial.crud.entity.RefreshToken;
import com.tutorial.crud.dto.AuthRequestDTO;
import com.tutorial.crud.dto.JwtResponseDTO;
import com.tutorial.crud.dto.RefreshTokenRequestDTO;
import com.tutorial.crud.entity.UserInfo;
import com.tutorial.crud.entity.UserRole;
import com.tutorial.crud.repository.RoleRepository;
import com.tutorial.crud.repository.UserRepository;
import com.tutorial.crud.service.JwtService;
import com.tutorial.crud.service.RefreshTokenService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.management.openmbean.KeyAlreadyExistsException;

import java.util.List;

import static java.util.Objects.isNull;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    private AuthenticationManager authenticationManager;

    @PostMapping("/signup")
    public JwtResponseDTO registerUser(@RequestBody AuthRequestDTO authRequestDTO) {

        if (userRepository.findByUsername(authRequestDTO.getUsername()) != null) {
            throw new KeyAlreadyExistsException("Username already exist ...");
        }

        UserInfo user = new UserInfo();
        user.setUsername(authRequestDTO.getUsername());
        user.setPassword(passwordEncoder.encode(authRequestDTO.getPassword()));

        UserRole defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role ROLE_USER not initialized."));
        user.getRoles().add(defaultRole);

        userRepository.save(user);

        List<String> userRoles = user.getRoles().stream()
                .map(UserRole::getName)
                .toList();


        RefreshToken refreshToken = refreshTokenService.createRefreshToken(authRequestDTO.getUsername());
        String accessToken = jwtService.generateToken(user.getUsername(), userRoles);

        return JwtResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    @PostMapping("/login")
    public JwtResponseDTO authenticateAndGetToken(@RequestBody AuthRequestDTO authRequestDTO){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequestDTO.getUsername(), authRequestDTO.getPassword())
        );

        if(authentication.isAuthenticated()){
            UserInfo user = userRepository.findByUsername(authRequestDTO.getUsername());
            List<String> userRoles = user.getRoles().stream()
                    .map(UserRole::getName)
                    .toList();
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(authRequestDTO.getUsername());
            return JwtResponseDTO.builder()
                    .accessToken(jwtService.generateToken(user.getUsername(), userRoles))
                    .refreshToken(refreshToken.getToken())
                    .build();
        } else {
            throw new UsernameNotFoundException("invalid user request..!!");
        }
    }

    @PostMapping("/refreshToken")
    public JwtResponseDTO refreshToken(@RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO){
        return refreshTokenService.findByToken(refreshTokenRequestDTO.getRefreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUserInfo)
                .map(userInfo -> {
                    List<String> userRoles = userInfo.getRoles().stream()
                            .map(UserRole::getName)
                            .toList();
                    String accessToken = jwtService.generateToken(userInfo.getUsername(), userRoles);
                    return JwtResponseDTO.builder()
                            .accessToken(accessToken)
                            .refreshToken(refreshTokenRequestDTO.getRefreshToken())
                            .build();
                }).orElseThrow(() -> new RuntimeException("Refresh Token is not in DB..!!"));
    }

    @PostMapping("/logout")
    public void logout(@RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO) {
        refreshTokenService.deleteByToken(refreshTokenRequestDTO.getRefreshToken());
        System.out.println("Token refresh supprimé avec succès.");
    }

}

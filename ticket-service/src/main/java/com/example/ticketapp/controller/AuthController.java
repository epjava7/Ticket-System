package com.example.ticketapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.example.ticketapp.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import java.time.Duration;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import java.util.Map;

import com.example.ticketapp.domain.Employee;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping("/loginStatus")
    public ResponseEntity<?> getLoginStatus(Authentication auth) {
        if (auth != null && auth.isAuthenticated()) 
            return ResponseEntity.status(200).body("Logged In");
        return ResponseEntity.status(401).body("Not Logged In");
    }

    @PostMapping(value= "/login")
    public ResponseEntity<?> login(@RequestBody String employeeJson, HttpServletResponse res) {
        System.out.println("Received JSON: " + employeeJson);
        ObjectMapper objectMapper = new ObjectMapper();
        Employee employee;
        try {
            employee = objectMapper.readValue(employeeJson, Employee.class);
        } catch (Exception e) {
            // Log the deserialization error
            e.printStackTrace(); 
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error deserializing employee JSON: " + e.getMessage());
        }

        String email = employee.getEmail();
        try {
            var auth = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(email, employee.getPassword()));
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Wrong credentials");
        }

        String token = jwtService.generateToken(email);
        String refreshToken = jwtService.generateRefreshToken(email);

        var cookie = ResponseCookie.from("refreshToken", refreshToken)
            .httpOnly(true)
            .secure(true)
            .path("/api/auth/refresh")
            .maxAge(Duration.ofDays(7))
            .sameSite("None")
            .build();
    
        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.status(200).body(Map.of("token", token));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse res) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
            .httpOnly(true)
            .secure(true) 
            .path("/api/auth/refresh") 
            .maxAge(0)
            .sameSite("None") 
            .build();
        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok(Map.of("message", "success"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
        if (refreshToken != null && jwtService.isValid(refreshToken)) {
            String email = jwtService.extractUsername(refreshToken);
            String newToken = jwtService.generateToken(email);
            return ResponseEntity.status(200).body(Map.of("token", newToken));
        }
        return ResponseEntity.status(403).build();
    }

    @GetMapping("/isTokenValid")
    public ResponseEntity<?> isTokenValid(@RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Token invalid");
        }

        String jwt = token.substring(7);
        if (jwtService.isValid(jwt)) 
            return ResponseEntity.status(200).body("Token is valid");
        return ResponseEntity.status(403).body("Token invalid");
    }

}

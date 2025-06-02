package com.example.ticket_view_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.example.ticket_view_service.component.EmployeeServiceClient;

@Service
public class EmployeeUserDetailsService implements UserDetailsService {
    
    @Autowired
    private EmployeeServiceClient employeeServiceClient;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Map<String, Object> employeeDetails = employeeServiceClient.getEmployeeDetailsForAuth(email);
        if (employeeDetails == null) {
            throw new UsernameNotFoundException("Email not found");
        }

        String hashedPassword = (String) employeeDetails.get("password");
        List<String> roles = (List<String>) employeeDetails.get("roles");

        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority(role))
                .collect(Collectors.toList());

        return new User(email, hashedPassword, authorities);
    }
}

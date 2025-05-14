package com.example.ticketapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.HashSet;

import com.example.ticketapp.repository.EmployeeRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var employee = employeeRepository.findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException(email));

        var auths = new HashSet<GrantedAuthority>();
        for (String role: employee.getRoles()) {
            auths.add(new SimpleGrantedAuthority(role));
        }

        return new User(employee.getEmail(), employee.getPassword(), auths);
    }
}

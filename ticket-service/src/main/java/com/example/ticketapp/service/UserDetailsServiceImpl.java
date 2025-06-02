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
import java.util.Set;

import com.example.ticketapp.repository.EmployeeRepository;
import com.example.ticketapp.domain.Employee;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            Employee employee = employeeRepository.findByEmail(username).orElseThrow();
            Set<GrantedAuthority> auths = new HashSet<>();
            employee.getRoles().forEach(
                role -> auths.add(new SimpleGrantedAuthority(role)));
            return new User(employee.getEmail(), employee.getPassword(), auths);
    }
}

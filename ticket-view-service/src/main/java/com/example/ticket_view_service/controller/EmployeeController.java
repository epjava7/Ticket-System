package com.example.ticket_view_service.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.security.core.Authentication;

import com.example.ticket_view_service.component.EmployeeServiceClient;

@RestController
@RequestMapping("/app/api/employees")
public class EmployeeController {
    
    @Autowired
    private EmployeeServiceClient employeeServiceClient;

    @PostMapping("/signup")
    public ResponseEntity<?> signupEmployee(@RequestBody Map<String, Object> employeeData) {
        return employeeServiceClient.signupEmployee(employeeData);
    }

    @GetMapping("/details")
    public ResponseEntity<?> getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = null;
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            email = (String) principal;
        } else {
            email = authentication.getName(); 
        }

        return employeeServiceClient.getAuthenticatedEmployeeDetails(email);
    }

    @GetMapping("/all") 
    public ResponseEntity<?> getAllEmployees() {
        return employeeServiceClient.getAllEmployees();
    }
}

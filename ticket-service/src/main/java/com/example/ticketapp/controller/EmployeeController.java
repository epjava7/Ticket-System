package com.example.ticketapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.ticketapp.service.EmployeeService;
import com.example.ticketapp.domain.Employee;
import com.example.ticketapp.repository.RoleRepository;


@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    
    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private RoleRepository roleRepository;

    @PostMapping("/signup")
    public ResponseEntity<?> saveEmployee(@RequestBody Employee employee) {
        try {
            var savedEmployee = employeeService.saveEmployee(employee);
            savedEmployee.setPassword(null);
            return ResponseEntity.status(200).body(savedEmployee);
        } catch (RuntimeException e) {
            var res = new HashMap<String, String>();
            String message = e.getMessage();
            if (message.equals("Email exists")) {
                res.put("emailError", "Email already exists. Try Again!");
            } else if (message.equals("Name exists")) {
                res.put("nameError", "UserName already exists. Try Again!");
            } else {
                res.put("error", "Failed");
            }
            return ResponseEntity.status(200).body(res);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllEmployees() {

        var employeeList = employeeService.getAllEmployees();
        var res = new ArrayList<Map<String, Object>>();
        for (var e: employeeList) {
            var m = new HashMap<String,Object>();
            m.put("id",    e.getId());
            m.put("name",  e.getName());
            m.put("email", e.getEmail());
            var roles = e.getRoles();
            var roleSet = new HashSet<String>();
            for (String roleName: roles) {
                roleSet.add(roleName);
            }
            m.put("roles", roleSet);
            res.add(m);
        }
        return ResponseEntity.status(200).body(res);
    }

    @PutMapping("/role/{employeeId}")
    public ResponseEntity<?> updateRole(@PathVariable Long employeeId, @RequestBody String role) {
        var employee = employeeService.updateEmployeeRole(employeeId, role);
        employee.setPassword(null);
        return ResponseEntity.status(200).body(employee);
    }

    @GetMapping("/getDetails") 
    public ResponseEntity<?> getDetails(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        String email = auth.getName();
        try {
            var employee = employeeService.findEmployeeByEmail(email);
            employee.setPassword(null); 
            return ResponseEntity.ok(employee);

        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body("User Details not Found");
        }
    }

}

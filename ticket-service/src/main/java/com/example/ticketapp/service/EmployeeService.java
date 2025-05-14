package com.example.ticketapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import com.example.ticketapp.repository.EmployeeRepository;
import com.example.ticketapp.repository.RoleRepository;
import com.example.ticketapp.domain.Employee;
import com.example.ticketapp.domain.Role;


@Service
public class EmployeeService {
    
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Employee saveEmployee(Employee employee) {
        String email = employee.getEmail();
        if (employeeRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email exists");
        }

        String name = employee.getName();
        if (employeeRepository.findByName(name).isPresent()) {
            throw new RuntimeException("Name exists");
        }

        String hashedPassword = passwordEncoder.encode(employee.getPassword());
        employee.setPassword(hashedPassword);
        employee.setRoles(Set.of("USER"));
        var savedEmployee = employeeRepository.save(employee);
        savedEmployee.setPassword(null); 
        return savedEmployee;
    }

    public Employee findEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(email + " not found"));
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee updateEmployeeRole(Long employeeId, String roleName) {

        var roles = Set.of("USER", "MANAGER", "ADMIN");
        if (!roles.contains(roleName)) {
            throw new RuntimeException("Invalid Role");
        }

        var employee = employeeRepository.findById(employeeId).orElseThrow();
        employee.setPassword(null);

        var rolesAssign = new HashSet<String>();
        rolesAssign.add("USER");

        if (roleName.equals("MANAGER")) {
            rolesAssign.add("MANAGER");
        }
        if (roleName.equals("ADMIN")) {
            rolesAssign.add("MANAGER");
            rolesAssign.add("ADMIN");
        }

        employee.setRoles(rolesAssign);
        return employeeRepository.save(employee);
    }

}

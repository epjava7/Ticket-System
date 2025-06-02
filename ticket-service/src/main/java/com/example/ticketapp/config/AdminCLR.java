package com.example.ticketapp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Set;

import com.example.ticketapp.repository.EmployeeRepository;
import com.example.ticketapp.domain.Role;
import com.example.ticketapp.domain.Employee;
import com.example.ticketapp.repository.RoleRepository;

@Component
public class AdminCLR implements CommandLineRunner{
    
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) {

        if (roleRepository.findByName("ADMIN").isEmpty()) {
            roleRepository.save(new Role("ADMIN"));
        }
        if (roleRepository.findByName("MANAGER").isEmpty()) {
            roleRepository.save(new Role("MANAGER"));
        }
        if (roleRepository.findByName("USER").isEmpty()) {
            roleRepository.save(new Role("USER"));
        }

        String adminEmail = "admin@admin.com";
        if (employeeRepository.findByEmail(adminEmail).isEmpty() && employeeRepository.findByName("Admin").isEmpty()) {
            employeeRepository.save(
                new Employee("Admin", adminEmail, passwordEncoder.encode("admin"), Set.of("ADMIN", "MANAGER", "USER"))
            );
        }

        adminEmail = "youshocked15@gmail.com";
        if (employeeRepository.findByEmail(adminEmail).isEmpty() && employeeRepository.findByName("Admin2").isEmpty()) {
            employeeRepository.save(
                new Employee("Admin2", adminEmail, passwordEncoder.encode("admin"), Set.of("ADMIN", "MANAGER", "USER"))
            );
        }

        String managerEmail = "manager@manager.com";
        if (employeeRepository.findByEmail(managerEmail).isEmpty() && employeeRepository.findByName("Manager").isEmpty()) {
            employeeRepository.save(
                new Employee("Manager", managerEmail, passwordEncoder.encode("manager"), Set.of("MANAGER", "USER"))
            );
        }

        String userEmail = "user@user.com";
        if (employeeRepository.findByEmail(userEmail).isEmpty() && employeeRepository.findByName("USER").isEmpty()) {
            employeeRepository.save(
                new Employee("User", userEmail, passwordEncoder.encode("user"), Set.of("USER"))
            );
        }
    }
}

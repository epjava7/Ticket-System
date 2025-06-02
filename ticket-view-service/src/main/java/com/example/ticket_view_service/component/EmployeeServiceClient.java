package com.example.ticket_view_service.component;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.List;
import java.util.Map;

@Component
public class EmployeeServiceClient {
    
    private static final String BASE_URL = "http://localhost:8081/api/employees";

    @CacheEvict(value = "allEmployees", allEntries = true)
    public ResponseEntity<?> signupEmployee(Map<String, Object> employeeData) {
        RestTemplate restTemplate = new RestTemplate();
        String url = BASE_URL + "/signup";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(employeeData, headers);
        return restTemplate.postForEntity(url, requestEntity, Map.class); 
    }

    @Cacheable(value = "employeeAuthDetails", key = "#email")
    public Map<String, Object> getEmployeeDetailsForAuth(String email) {
        String url = BASE_URL + "/detailsForAuth/" + email;

        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        return response;
    }

    @Cacheable(value = "employeeDetails", key = "#email")
    public ResponseEntity<?> getAuthenticatedEmployeeDetails(String email) {
        String url = BASE_URL + "/getDetails/" + email;
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForEntity(url, Map.class);
    }

    @Cacheable("allEmployees")
    public ResponseEntity<?> getAllEmployees() {
        String url = BASE_URL  + "/all";
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForEntity(url, List.class);
    }
}

package com.example.ticket_view_service.component;

import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource; 
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;


@Component
public class TicketServiceClient {
    
    private static final String BASE_URL = "http://localhost:8081/api/tickets";

    public ResponseEntity<?> createTicket(JsonNode ticket, List<MultipartFile> files) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("ticket", ticket);

        if (files != null) {
            for (MultipartFile file : files) {
                body.add("files", file.getResource());
            }
        }

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        return restTemplate.postForEntity(BASE_URL, requestEntity, JsonNode.class);
    }

    @Cacheable("ticketCategories")
    public ResponseEntity<List<String>> getAllUniqueCategories() {
        RestTemplate restTemplate = new RestTemplate();
        String url = BASE_URL + "/categories";
        ResponseEntity<String[]> responseEntity = restTemplate.getForEntity(url, String[].class);
        List<String> categories = Arrays.asList(responseEntity.getBody());
        return new ResponseEntity<>(categories, responseEntity.getHeaders(), responseEntity.getStatusCode());
    }

    @Cacheable("allTickets")
    public ResponseEntity<List<JsonNode>> getAllTickets() {
        RestTemplate restTemplate = new RestTemplate();
        String url = BASE_URL; 
        ResponseEntity<JsonNode[]> responseEntity = restTemplate.getForEntity(url, JsonNode[].class);
        
        List<JsonNode> tickets = null;
        if (responseEntity.getBody() != null) {
            tickets = Arrays.asList(responseEntity.getBody());
        }
        
        return new ResponseEntity<>(tickets, responseEntity.getHeaders(), responseEntity.getStatusCode());
    }

    public ResponseEntity<Resource> downloadFile(Long id, String fileName) {
        RestTemplate restTemplate = new RestTemplate();
        String urlTemplate = UriComponentsBuilder.fromHttpUrl(BASE_URL + "/download/{id}/{fileName}")
                .encode() 
                .toUriString(); 
        return restTemplate.getForEntity(urlTemplate, Resource.class, id, fileName);
    }

    @Cacheable(value = "ticketById", key = "#id")
    public ResponseEntity<JsonNode> getTicketById(Long id) { 
        RestTemplate restTemplate = new RestTemplate();
        String url = BASE_URL + "/" + id;
        return restTemplate.getForEntity(url, JsonNode.class);
    }

    public ResponseEntity<?> updateTicketStatus(Long id, String newStatus, String comments, Long actionById) {
        RestTemplate restTemplate = new RestTemplate();
        String url = BASE_URL + "/status/" + id; 
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("status", newStatus);
        body.put("comments", comments);
        body.put("actionById", actionById);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        
        return restTemplate.exchange(url, HttpMethod.PUT, requestEntity, JsonNode.class); 
    }

    @Cacheable(value = "ticketHistory", key = "#id")
    public ResponseEntity<?> getTicketHistoryById(Long id) {
        RestTemplate restTemplate = new RestTemplate();
        String url = BASE_URL + "/history/" + id; 
        return restTemplate.getForEntity(url, JsonNode.class);
    }
}

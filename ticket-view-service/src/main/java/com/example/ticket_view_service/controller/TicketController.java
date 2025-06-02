package com.example.ticket_view_service.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import com.example.ticket_view_service.component.TicketServiceClient;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


@RestController
@RequestMapping("/app/api/tickets")
public class TicketController {
    
    @Autowired
    private TicketServiceClient ticketServiceClient;

    @PostMapping
    @Caching(evict = {
        @CacheEvict(value = "allTickets", allEntries = true),
        @CacheEvict(value = "ticketCategories", allEntries = true)
    })
    public ResponseEntity<?> createTicket(@RequestPart("ticket") JsonNode ticket,
                                          @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return ticketServiceClient.createTicket(ticket, files);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllUniqueCategories() {
        return ticketServiceClient.getAllUniqueCategories();
    }

    @GetMapping
    public ResponseEntity<List<JsonNode>> getAllTickets() {
        return ticketServiceClient.getAllTickets();
    }

    @GetMapping("/download/{id}/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id, @PathVariable String fileName) {
        return ticketServiceClient.downloadFile(id, fileName);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JsonNode> getTicketById(@PathVariable Long id) {
        return ticketServiceClient.getTicketById(id);
    }

    @PutMapping("/status/{id}")
    @Caching(evict = {
        @CacheEvict(value = "allTickets", allEntries = true),
        @CacheEvict(value = "ticketById", key = "#id"),
        @CacheEvict(value = "ticketHistory", key = "#id")
    })
    public ResponseEntity<?> updateTicketStatus(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        String newStatus = (String) payload.get("status");
        String comments = (String) payload.get("comments"); 
        Number actionByIdNum = (Number) payload.get("actionById");
        Long actionById = null;
        if (actionByIdNum != null) {
            actionById = actionByIdNum.longValue();
        }
        
        return ticketServiceClient.updateTicketStatus(id, newStatus, comments, actionById);
    }

    @GetMapping("/history/{id}")
    public ResponseEntity<?> getTicketHistoryById(@PathVariable Long id) {
        return ticketServiceClient.getTicketHistoryById(id);
    }
}

package com.example.ticketapp.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PathVariable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;

import com.example.ticketapp.service.TicketService;
import com.example.ticketapp.domain.Ticket;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> createTicket(@RequestPart("ticket") Ticket ticket, 
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {

        if (ticketService.ticketExists(ticket.getTitle())) {
            return ResponseEntity.status(200).body(Map.of("error", "Title Exists"));
        }

        try {
            Ticket savedTicket = ticketService.saveTicket(ticket, files);
            return ResponseEntity.status(200).body(savedTicket);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<?> getAllUniqueCategories() {
        List<String> categories = ticketService.getAllUniqueCategories();
        return ResponseEntity.status(200).body(categories);
    }

    @GetMapping
    public ResponseEntity<?> getAllTickets() {
        List<Ticket> tickets = ticketService.getAllTickets();
        return ResponseEntity.status(200).body(tickets);
    }

    @GetMapping("/download/{id}/{fileName:.+}")
    public ResponseEntity<?> downloadFile(@PathVariable Long id, @PathVariable String fileName) {
        Ticket ticket = ticketService.getTicketById(id);
        String filePath = ticket.getFileAttachmentPaths().stream()
            .filter(p -> Paths.get(p).getFileName().toString().equals(fileName))
            .findFirst()
            .orElseThrow();

        try { 
            Path file = Paths.get(filePath);
            Resource resource = new UrlResource(file.toUri());
            String contentType = Files.probeContentType(file);

            return ResponseEntity.status(200).contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFileName() + "\"").body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTicket(@PathVariable Long id) {
        Ticket ticket = ticketService.getTicketById(id);
        return ResponseEntity.status(200).body(ticket);
    }

}

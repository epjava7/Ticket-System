package com.example.ticketapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.IOException;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.ArrayList;
import java.util.List;

import com.example.ticketapp.repository.TicketRepository;
import com.example.ticketapp.repository.TicketHistoryRepository;
import com.example.ticketapp.domain.Employee;
import com.example.ticketapp.domain.Ticket;
import com.example.ticketapp.repository.EmployeeRepository;
import com.example.ticketapp.domain.TicketHistory;

@Service
public class TicketService {
    
    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketHistoryRepository ticketHistoryRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Path fileStorageLocation;

    @Autowired
    public void FileController() throws IOException {
        this.fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();
        Files.createDirectories(this.fileStorageLocation);
    }

    public boolean ticketExists(String title) {
        return ticketRepository.findByTitle(title).isPresent();
    }

    public List<String> getAllUniqueCategories() {
        return ticketRepository.findDistinctCategories();
    }

    public List<Ticket> getAllTickets() {
        var ticketList = ticketRepository.findAll();
        for (Ticket t: ticketList) {
            var employee = employeeRepository.findById(t.getCreatedById()).orElseThrow();
            t.setCreatedByName(employee.getName());
        }
        return ticketList;
    }

    public Ticket getTicketById(Long id) {
        var ticket = ticketRepository.findById(id).orElseThrow();
        ticket.setCreatedByName(employeeRepository.findById(ticket.getCreatedById()).orElseThrow().getName());
        return ticket;
    }

    public Ticket saveTicket(Ticket ticket, List<MultipartFile> files) throws IOException{
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String email  = authentication.getName();
        Employee employee = employeeRepository.findByEmail(email).orElseThrow();
        ticket.setCreatedById(employee.getId());

        if (files != null) {
            var paths = new ArrayList<String>();
            for (MultipartFile f: files) {
                paths.add(storeFile(f));
            }
            ticket.setFileAttachmentPaths(paths);
        }

        Ticket savedTicket = ticketRepository.save(ticket);

        TicketHistory ticketHistory = new TicketHistory();
        ticketHistory.setTicketId(savedTicket.getId());
        ticketHistory.setAction(savedTicket.getStatus());
        ticketHistory.setActionById(employee.getId());
        ticketHistory.setActionDate(savedTicket.getCreationDate());
        ticketHistory.setComments("Ticket opened by " + employee.getName());
        ticketHistoryRepository.save(ticketHistory);

        return savedTicket;
    }

    public String storeFile(MultipartFile file) throws IOException {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        Path targetLocation = fileStorageLocation.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        return targetLocation.toString();
    }

}

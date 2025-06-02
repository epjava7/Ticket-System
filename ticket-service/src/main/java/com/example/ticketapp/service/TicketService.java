package com.example.ticketapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Comparator;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.Document;

import com.example.ticketapp.repository.TicketRepository;
import com.example.ticketapp.repository.TicketHistoryRepository;
import com.example.ticketapp.domain.Employee;
import com.example.ticketapp.domain.Ticket;
import com.example.ticketapp.repository.EmployeeRepository;
import com.example.ticketapp.domain.TicketHistory;
import com.example.ticketapp.domain.TicketStatus;
import com.example.ticketapp.dto.TicketEventDto;

@Service
public class TicketService {
    
    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketHistoryRepository ticketHistoryRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TicketEventPublisher ticketEventPublisher;

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

    // @Cacheable("allTickets")
    public List<Ticket> getAllTickets() {
        var ticketList = ticketRepository.findAll();
        for (Ticket t : ticketList) {
            var employee = employeeRepository.findById(t.getCreatedById()).orElseThrow();
            t.setCreatedByName(employee.getName());

            List<TicketHistory> histories = ticketHistoryRepository.findByTicketId(t.getId());
            if (histories != null && !histories.isEmpty()) {
                Optional<TicketHistory> latestHistoryOpt = histories.stream()
                        .max(Comparator.comparing(TicketHistory::getActionDate));
                
                if (latestHistoryOpt.isPresent()) {
                    TicketHistory latestHistory = latestHistoryOpt.get();
                    t.setLastUpdateDate(latestHistory.getActionDate());
                    if (latestHistory.getActionById() != null) {
                        Employee lastUpdater = employeeRepository.findById(latestHistory.getActionById()).orElse(null);
                        t.setLastUpdatedByName(lastUpdater != null ? lastUpdater.getName() : "Deleted User");
                    } else {
                        if (latestHistory.getActionByName() != null) {
                            t.setLastUpdatedByName(latestHistory.getActionByName());
                        } else {
                            t.setLastUpdatedByName("System Action");
                        }
                    }
                } else {
                    t.setLastUpdateDate(t.getCreationDate());
                    t.setLastUpdatedByName(t.getCreatedByName());
                }
            } else {
                t.setLastUpdateDate(t.getCreationDate());
                t.setLastUpdatedByName(t.getCreatedByName());
            }
        }
        return ticketList;
    }

    // @Cacheable("tickets")
    public Ticket getTicketById(Long id) {
        var ticket = ticketRepository.findById(id).orElseThrow();
        ticket.setCreatedByName(employeeRepository.findById(ticket.getCreatedById()).orElseThrow().getName());
        return ticket;
    }

    // @CacheEvict(value = "allTickets", allEntries = true)
    public Ticket saveTicket(Ticket ticket, List<MultipartFile> files) throws IOException{
        Employee employee = employeeRepository.findById(ticket.getCreatedById()).orElseThrow();
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

        TicketEventDto event = new TicketEventDto(
                "TICKET_CREATED",
                savedTicket.getId(),
                employee.getEmail() 
        );
        event.setTicketTitle(savedTicket.getTitle());
        event.setTicketStatus(savedTicket.getStatus().toString());

        List<Employee> allEmployees = employeeRepository.findAll();

        List<String> managerEmails = allEmployees.stream()
                .filter(e -> e.getRoles().contains("MANAGER"))
                .map(Employee::getEmail)
                .collect(Collectors.toList());
        event.setNotifyManagerEmails(managerEmails); 
        
        ticketEventPublisher.publishTicketEvent(event);

        savedTicket.setCreatedByName(employee.getName());
        return savedTicket;
    }

    // @Caching(evict = {
        // @CacheEvict(value = "tickets", key = "#id"),
        // @CacheEvict(value = "allTickets", allEntries = true)
    // })
    public Ticket updateTicketStatus(Long id, String newStatus, String comments, Long actionById) {
        Ticket ticket = ticketRepository.findById(id).orElseThrow();
        Employee actionByEmployee = employeeRepository.findById(actionById).orElseThrow();
        Employee createdByEmployee = employeeRepository.findById(ticket.getCreatedById()).orElseThrow();

        TicketStatus newStatusValue;
        try {
            newStatusValue = TicketStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Status not valid");
        }

        ticket.setStatus(newStatusValue);
        Ticket updatedTicket = ticketRepository.save(ticket);

        TicketHistory ticketHistory = new TicketHistory();
        ticketHistory.setTicketId(updatedTicket.getId());
        ticketHistory.setAction(updatedTicket.getStatus()); 
        ticketHistory.setActionById(actionById);
        ticketHistory.setActionDate(Instant.now());
        String commentPrefix = "Changed Ticket Status to " + newStatusValue;
        ticketHistory.setComments(comments != null && !comments.isEmpty() ? commentPrefix + ": " + comments : commentPrefix);
        ticketHistoryRepository.save(ticketHistory);

        String eventType = ""; 
        byte[] pdfAttachment = null;
        String pdfFileName = null;
        switch (newStatusValue) {
            case REOPENED:
                eventType = "TICKET_REOPENED"; 
                break;    
            case APPROVED:
                eventType = "TICKET_APPROVED";
                break;
            case REJECTED:
                eventType = "TICKET_REJECTED";
                break;
            case RESOLVED:
                eventType = "TICKET_RESOLVED";
                List<TicketHistory> historyList = getTicketHistory(updatedTicket.getId());
                try {
                    pdfAttachment = generateTicketHistoryPdfWithIText(updatedTicket, historyList, createdByEmployee);
                    pdfFileName = "Ticket_" + updatedTicket.getId() + "_History.pdf";
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
                break;
            case CLOSED:
                eventType = "TICKET_CLOSED";
                break;
        }

        TicketEventDto event = new TicketEventDto(
                eventType,
                updatedTicket.getId(),
                createdByEmployee.getEmail() 
        );

        event.setTicketTitle(updatedTicket.getTitle());
        event.setTicketStatus(updatedTicket.getStatus().toString());
        event.setManagerEmail(actionByEmployee.getEmail());

        if (pdfAttachment != null) {
            event.setPdfAttachment(pdfAttachment);
            event.setPdfFileName(pdfFileName);
        }

        if (comments != null && !comments.isEmpty()) {
            event.setAdditionalDetails(Map.of("comments", comments, "actionBy", actionByEmployee.getName()));
        } else {
            event.setAdditionalDetails(Map.of("actionBy", actionByEmployee.getName()));
        }

        if (newStatusValue == TicketStatus.APPROVED || newStatusValue == TicketStatus.REOPENED) {
            List<String> adminEmails = employeeRepository.findAll().stream()
                    .filter(e -> e.getRoles().contains("ADMIN"))
                    .map(Employee::getEmail)
                    .collect(Collectors.toList());
            event.setNotifyAdminEmails(adminEmails);
        }

        ticketEventPublisher.publishTicketEvent(event);

        updatedTicket.setCreatedByName(createdByEmployee.getName());
        return updatedTicket;
    }

    private byte[] generateTicketHistoryPdfWithIText(Ticket ticket, List<TicketHistory> historyList, Employee createdByEmployee) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        document.add(new Paragraph("Ticket").setFont(boldFont).setFontSize(14));
        document.add(new Paragraph("\n")); 

        String createdByName = createdByEmployee.getName();

        document.add(new Paragraph().add(new Text("Title: ")).add(ticket.getTitle()).setFont(font).setFontSize(10));
        document.add(new Paragraph().add(new Text("Status: ")).add(ticket.getStatus().toString()).setFont(font).setFontSize(10));
        document.add(new Paragraph().add(new Text("Priority: ")).add(ticket.getPriority().toString()).setFont(font).setFontSize(10));
        document.add(new Paragraph().add(new Text("Category: ")).add(ticket.getCategory() != null ? ticket.getCategory() : "N/A").setFont(font).setFontSize(10));
        document.add(new Paragraph().add(new Text("Created By: ")).add(createdByName).setFont(font).setFontSize(10));
        document.add(new Paragraph().add(new Text("Creation Date: ")).add(ticket.getCreationDate() != null ? ticket.getCreationDate().toString() : "N/A").setFont(font).setFontSize(10));
        document.add(new Paragraph().add(new Text("Description: ")).add(ticket.getDescription() != null ? ticket.getDescription() : "N/A").setFont(font).setFontSize(10));
        document.add(new Paragraph("\n"));

        document.add(new Paragraph("Ticket History").setFontSize(12));
        document.add(new LineSeparator(new SolidLine(1f)));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (TicketHistory history : historyList) {
            String actionDateStr = history.getActionDate().atZone(java.time.ZoneId.systemDefault()).format(formatter);
            String actionByName = history.getActionByName();

            document.add(new Paragraph()
                    .add(new Text("Status: "))
                    .add(history.getAction().toString())
                    .setFont(font).setFontSize(9));
            document.add(new Paragraph()
                    .add(new Text("By: "))
                    .add(actionByName)
                    .setFont(font).setFontSize(9));
            document.add(new Paragraph()
                    .add(new Text("Date: "))
                    .add(actionDateStr)
                    .setFont(font).setFontSize(9));
            document.add(new Paragraph()
                    .add(new Text("Comments: "))
                    .add(history.getComments() != null ? history.getComments() : "")
                    .setFont(font).setFontSize(9));
            document.add(new LineSeparator(new SolidLine(0.5f)).setMarginTop(5).setMarginBottom(5));
        }

        document.close();
        return baos.toByteArray();
    }

    // @Cacheable(value = "ticketHistory", key = "#ticketId")
    public List<TicketHistory> getTicketHistory(Long ticketId) {
        List<TicketHistory> historyList = ticketHistoryRepository.findByTicketId(ticketId);
        for (TicketHistory history : historyList) {
            if (history.getActionById() != null) {
                Employee employee = employeeRepository.findById(history.getActionById()).orElse(null);
                if (employee != null) {
                    history.setActionByName(employee.getName());
                } else {
                    history.setActionByName("Deleted User");
                }
            } else {
                history.setActionByName("System Action");
            }
        }
        return historyList;
    }

    public String storeFile(MultipartFile file) throws IOException {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        fileName = System.currentTimeMillis() + "_" + fileName;
        Path targetLocation = fileStorageLocation.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        return targetLocation.toString();
    }

}

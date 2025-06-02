package com.example.ticketapp.service;

import com.example.ticketapp.domain.Employee;
import com.example.ticketapp.domain.Ticket;
import com.example.ticketapp.domain.TicketHistory;
import com.example.ticketapp.domain.TicketStatus;
import com.example.ticketapp.dto.TicketEventDto;
import com.example.ticketapp.repository.EmployeeRepository;
import com.example.ticketapp.repository.TicketHistoryRepository;
import com.example.ticketapp.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ScheduledTasks {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TicketHistoryRepository ticketHistoryRepository;

    @Autowired
    private TicketEventPublisher ticketEventPublisher;

    @Scheduled(cron = "0 0 * * * ?")
    public void notifyManagerForOldPendingTickets() {
        Instant sevenDaysAgo = Instant.now().minus(7, ChronoUnit.DAYS);
        List<TicketStatus> pendingStatuses = Arrays.asList(TicketStatus.OPEN, TicketStatus.REOPENED);

        List<Ticket> allTickets = ticketRepository.findAll();
        List<Ticket> pendingTickets = allTickets.stream()
            .filter(ticket -> {
                if (!pendingStatuses.contains(ticket.getStatus())) {
                    return false;
                }
                if (ticket.getStatus() == TicketStatus.REOPENED) {
                    Optional<TicketHistory> lastReopenedEntry = ticketHistoryRepository.findByTicketId(ticket.getId()).stream()
                        .filter(h -> h.getAction() == TicketStatus.REOPENED)
                        .max(Comparator.comparing(TicketHistory::getActionDate));
                    return lastReopenedEntry.map(history -> history.getActionDate().isBefore(sevenDaysAgo)).orElse(false); 
                } else {
                    return ticket.getCreationDate().isBefore(sevenDaysAgo);
                }
            })
            .collect(Collectors.toList());

        for (Ticket ticket : pendingTickets) {
            Long creatorId = ticket.getCreatedById();
            Employee creator = employeeRepository.findById(creatorId).orElse(null);
            
            TicketEventDto event = new TicketEventDto("PENDING_TICKET_REMINDER", ticket.getId(), null);
            event.setTicketTitle(ticket.getTitle());

            boolean specificManagerNotified = false;
            if (creator != null && creator.getManagerId() != null) {
                 Optional<Employee> managerOpt = employeeRepository.findById(creator.getManagerId());
                 if (managerOpt.isPresent()) {
                     Employee manager = managerOpt.get();
                     event.setManagerEmail(manager.getEmail());
                     ticketEventPublisher.publishTicketEvent(event);
                     specificManagerNotified = true;
                 }
            }
            
            if (!specificManagerNotified) {
                List<String> allManagerEmails = employeeRepository.findAll().stream()
                    .filter(e -> e.getRoles() != null && e.getRoles().contains("MANAGER"))
                    .map(Employee::getEmail)
                    .collect(Collectors.toList());
                
                event.setNotifyManagerEmails(allManagerEmails); 
                event.setManagerEmail(null); 
                ticketEventPublisher.publishTicketEvent(event);

            }
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void autoCloseResolvedTickets() {
        Instant fiveDaysAgo = Instant.now().minus(5, ChronoUnit.DAYS);
        
        List<Ticket> resolvedTickets = ticketRepository.findAll().stream()
            .filter(ticket -> ticket.getStatus() == TicketStatus.RESOLVED)
            .collect(Collectors.toList());

        for (Ticket ticket : resolvedTickets) {
            List<TicketHistory> historyList = ticketHistoryRepository.findByTicketId(ticket.getId());
            Optional<TicketHistory> lastResolvedEntry = historyList.stream()
                .filter(h -> h.getAction() == TicketStatus.RESOLVED)
                .max(Comparator.comparing(TicketHistory::getActionDate));

            if (lastResolvedEntry.isPresent() && lastResolvedEntry.get().getActionDate().isBefore(fiveDaysAgo)) {
                ticket.setStatus(TicketStatus.CLOSED);
                ticketRepository.save(ticket);

                TicketHistory autoCloseHistory = new TicketHistory();
                autoCloseHistory.setTicketId(ticket.getId());
                autoCloseHistory.setAction(TicketStatus.CLOSED);
                autoCloseHistory.setActionDate(Instant.now());
                autoCloseHistory.setComments("Ticket auto-closed after 5 days in RESOLVED status.");
                ticketHistoryRepository.save(autoCloseHistory);

                Employee user = employeeRepository.findById(ticket.getCreatedById()).orElse(null);
                if (user != null) {
                    TicketEventDto event = new TicketEventDto("TICKET_AUTO_CLOSED", ticket.getId(), user.getEmail());
                    event.setTicketTitle(ticket.getTitle());
                    event.setTicketStatus(TicketStatus.CLOSED.toString());
                    ticketEventPublisher.publishTicketEvent(event);
                }
            }
        }
    }
}
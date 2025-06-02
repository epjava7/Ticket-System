package com.example.notification_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

import com.example.notification_service.dto.TicketEventDto;

@Component
public class TicketNotificationListener {

    @Autowired
    private EmailService emailService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @JmsListener(destination = "${app.messaging.ticket-notifications-queue}")
    public void receiveTicketEvent(TicketEventDto event) {
        String subject;
        String body;
        String userEmail = event.getUserEmail(); 

        String ticketUrlBase = "http://localhost:8082/ticket?id=";
        String ticketUrl = ticketUrlBase + event.getTicketId();

        switch (event.getEventType()) {
            case "TICKET_CREATED":
                subject = "Ticket Created: " + event.getTicketTitle();
                body = "Ticket: " + event.getTicketTitle() + "' (ID: " + event.getTicketId() + ") created " + "\n\n" + ticketUrl;
                emailService.sendSimpleEmail(userEmail, subject, body);

                if (event.getNotifyManagerEmails() != null && !event.getNotifyManagerEmails().isEmpty()) {
                    String managerSubject = "Ticket Created: " + event.getTicketTitle();
                    String managerBody = "A new ticket '" + event.getTicketTitle() + " was created by " + userEmail + "\n\n" + ticketUrl;
                    for (String managerEmail : event.getNotifyManagerEmails()) {
                        emailService.sendSimpleEmail(managerEmail, managerSubject, managerBody);
                    }
                }
                break;

            case "TICKET_APPROVED":
                subject = "Ticket Approved: " + event.getTicketTitle();
                body = "Your ticket '" + event.getTicketTitle() + " was APPROVED" + "\n\n" + ticketUrl;
                emailService.sendSimpleEmail(userEmail, subject, body);

                if (event.getNotifyAdminEmails() != null && !event.getNotifyAdminEmails().isEmpty()) {
                    String adminSubject = "Ticket Approved: " + event.getTicketTitle();
                    String adminBody = "The ticket '" + event.getTicketTitle() + " created by " + userEmail + " was APPROVED" + "\n\n" + ticketUrl;
                    for (String adminEmail : event.getNotifyAdminEmails()) {
                        emailService.sendSimpleEmail(adminEmail, adminSubject, adminBody);
                    }
                }
                break;

            case "TICKET_REJECTED":
                subject = "Ticket Rejected: " + event.getTicketTitle();
                body = "Your ticket '" + event.getTicketTitle() + " was REJECTED" + "\n\n" + ticketUrl;
                emailService.sendSimpleEmail(userEmail, subject, body);
                break;

            case "TICKET_RESOLVED":
                subject = "Ticket Resolved: " + event.getTicketTitle();
                body = "Your ticket '" + event.getTicketTitle() + " was RESOLVED" + "\n\n" + ticketUrl;
                if (event.getPdfAttachment() != null && event.getPdfFileName() != null) {
                    emailService.sendEmailWithAttachment(userEmail, subject, body, event.getPdfAttachment(), event.getPdfFileName());
                } else {
                    emailService.sendSimpleEmail(userEmail, subject, body);
                }
                break;

            case "TICKET_REOPENED":
                subject = "Ticket Reopened: " + event.getTicketTitle();
                body = "Your ticket '" + event.getTicketTitle() + " was REOPENED" + "\n\n" + ticketUrl;
                emailService.sendSimpleEmail(userEmail, subject, body);

                if (event.getNotifyAdminEmails() != null && !event.getNotifyAdminEmails().isEmpty()) {
                    String adminSubject = "Ticket Reopened: " + event.getTicketTitle();
                    String adminBody = "The ticket '" + event.getTicketTitle() + " created by " + userEmail + " was REOPENED" + "\n\n" + ticketUrl;
                    for (String adminEmail : event.getNotifyAdminEmails()) {
                        emailService.sendSimpleEmail(adminEmail, adminSubject, adminBody);
                    }
                }
                break;

            case "TICKET_CLOSED":
                subject = "Ticket Closed: " + event.getTicketTitle();
                body = "Your ticket '" + event.getTicketTitle() + " was CLOSED" + "\n\n" + ticketUrl;
                emailService.sendSimpleEmail(userEmail, subject, body);
                break;

            case "PENDING_TICKET_REMINDER":
            subject = "Ticket Requires Attention: " + event.getTicketTitle();
            body = "This is a reminder that the ticket '" + event.getTicketTitle() + " is pending and requires your attention.\n\n" + ticketUrl;
            if (event.getManagerEmail() != null && !event.getManagerEmail().isEmpty()) {
                emailService.sendSimpleEmail(event.getManagerEmail(), subject, body);
                System.out.println("Sent PENDING_TICKET_REMINDER to specific manager: " + event.getManagerEmail());
            } else if (event.getNotifyManagerEmails() != null && !event.getNotifyManagerEmails().isEmpty()) {
                for (String managerEmail : event.getNotifyManagerEmails()) {
                    emailService.sendSimpleEmail(managerEmail, subject, body);
                }
                System.out.println("Sent PENDING_TICKET_REMINDER to all managers.");
            } else {
                System.err.println("No manager email provided for PENDING_TICKET_REMINDER for ticket ID: " + event.getTicketId());
            }
            break;

            case "TICKET_AUTO_CLOSED":
                subject = "Ticket Auto-Closed: " + event.getTicketTitle();
                body = "Your ticket '" + event.getTicketTitle() + " has been automatically CLOSED after 5 days in RESOLVED status.\n\n" + ticketUrl;
                if (userEmail != null && !userEmail.isEmpty()) {
                    emailService.sendSimpleEmail(userEmail, subject, body);
                } else {
                    System.err.println("No user email for TICKET_AUTO_CLOSED for ticket ID: " + event.getTicketId());
                }
                break;
        }

        Map<String, Object> statusUpdateMap = new HashMap<>();
        statusUpdateMap.put("ticketId", event.getTicketId());
        statusUpdateMap.put("ticketStatus", event.getTicketStatus());
        messagingTemplate.convertAndSend("/topic/ticket-notifications", statusUpdateMap);

    }
    
}
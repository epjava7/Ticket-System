package com.example.notification_service.dto;

import java.io.Serializable;
import java.util.Map;
import java.util.List;

public class TicketEventDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String eventType; 
    private Long ticketId;
    private String userEmail;
    private String managerEmail;
    private String ticketTitle;
    private String ticketStatus;
    private byte[] pdfAttachment;
    private String pdfFileName;
    private List<String> notifyManagerEmails;
    private List<String> notifyAdminEmails;
    private Map<String, Object> additionalDetails;

    public TicketEventDto() {}

    public TicketEventDto(String eventType, Long ticketId, String userEmail) {
        this.eventType = eventType;
        this.ticketId = ticketId;
        this.userEmail = userEmail;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getTicketTitle() {
        return ticketTitle;
    }

    public void setTicketTitle(String ticketTitle) {
        this.ticketTitle = ticketTitle;
    }

    public String getTicketStatus() {
        return ticketStatus;
    }

    public void setTicketStatus(String ticketStatus) {
        this.ticketStatus = ticketStatus;
    }

    public byte[] getPdfAttachment() {
        return pdfAttachment;
    }

    public void setPdfAttachment(byte[] pdfAttachment) {
        this.pdfAttachment = pdfAttachment;
    }

    public String getPdfFileName() {
        return pdfFileName;
    }

    public void setPdfFileName(String pdfFileName) {
        this.pdfFileName = pdfFileName;
    }

    public List<String> getNotifyManagerEmails() {
        return notifyManagerEmails;
    }

    public void setNotifyManagerEmails(List<String> notifyManagerEmails) {
        this.notifyManagerEmails = notifyManagerEmails;
    }

    public List<String> getNotifyAdminEmails() {
        return notifyAdminEmails;
    }

    public void setNotifyAdminEmails(List<String> notifyAdminEmails) {
        this.notifyAdminEmails = notifyAdminEmails;
    }

    public Map<String, Object> getAdditionalDetails() {
        return additionalDetails;
    }

    public void setAdditionalDetails(Map<String, Object> additionalDetails) {
        this.additionalDetails = additionalDetails;
    }

    public String getManagerEmail() {
        return managerEmail;
    }

    public void setManagerEmail(String managerEmail) {
        this.managerEmail = managerEmail;
    }
}


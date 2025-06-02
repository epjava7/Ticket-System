package com.example.ticketapp.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.Instant;
import jakarta.persistence.Transient;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.Lob;

@Entity
public class TicketHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long ticketId;

    @Enumerated(EnumType.STRING)
    private TicketStatus action;

    private Long actionById;

    private Instant actionDate;

    @Lob
    private String comments;

    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String actionByName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticket) {
        this.ticketId = ticket;
    }

    public TicketStatus getAction() {
        return action;
    }

    public void setAction(TicketStatus action) {
        this.action = action;
    }

    public Long getActionById() {
        return actionById;
    }

    public void setActionById(Long actionById) {
        this.actionById = actionById;
    }

    public Instant getActionDate() {
        return actionDate;
    }

    public void setActionDate(Instant actionDate) {
        this.actionDate = actionDate;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getActionByName() {
        return actionByName;
    }

    public void setActionByName(String actionByName) {
        this.actionByName = actionByName;
    }
}

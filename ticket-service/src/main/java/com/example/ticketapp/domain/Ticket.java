package com.example.ticketapp.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.Transient;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.PrePersist;


@Entity
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Lob
    private String description;

    private Long createdById;

    private Long assigneeId;

    @Enumerated(EnumType.STRING)
    private TicketPriority priority;

    @Enumerated(EnumType.STRING)
    private TicketStatus status;
    
    private Instant creationDate;
    private String category;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ticket_files", joinColumns = @JoinColumn(name = "ticketId"))
    @Column(name = "filePath")
    private List<String> fileAttachmentPaths;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ticket_history_ids", joinColumns = @JoinColumn(name = "ticketId"))
    @Column(name = "historyId")
    private List<Long> historyIds = new ArrayList<>();

    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String createdByName;

    @PrePersist
    protected void onCreate() {
        this.creationDate = Instant.now();
        if (this.status == null) {
            this.status = TicketStatus.OPEN;
        }
        if (this.priority == null) {
            this.priority = TicketPriority.LOW;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssignee(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getFileAttachmentPaths() {
        return fileAttachmentPaths;
    }

    public void setFileAttachmentPaths(List<String> fileAttachmentPaths) {
        this.fileAttachmentPaths = fileAttachmentPaths;
    }

    public List<Long> getHistory() {
        return historyIds;
    }

    public void setHistory(List<Long> history) {
        this.historyIds = historyIds;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public List<Long> getHistoryIds() {
        return historyIds;
    }

    public void setHistoryIds(List<Long> historyIds) {
        this.historyIds = historyIds;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }
}

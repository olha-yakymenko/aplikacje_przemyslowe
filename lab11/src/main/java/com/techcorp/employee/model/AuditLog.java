package com.techcorp.employee.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private LocalDateTime eventDate;

    @Column(length = 100)
    private String eventType;

    @Column(length = 100)
    private String affectedEntity;

    private Long entityId;

    // Konstruktory
    public AuditLog() {
        this.eventDate = LocalDateTime.now();
    }

    public AuditLog(String message) {
        this();
        this.message = message;
    }

    public AuditLog(String message, String eventType, String affectedEntity, Long entityId) {
        this();
        this.message = message;
        this.eventType = eventType;
        this.affectedEntity = affectedEntity;
        this.entityId = entityId;
    }

    // Gettery i settery
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getEventDate() { return eventDate; }
    public void setEventDate(LocalDateTime eventDate) { this.eventDate = eventDate; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getAffectedEntity() { return affectedEntity; }
    public void setAffectedEntity(String affectedEntity) { this.affectedEntity = affectedEntity; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    @Override
    public String toString() {
        return String.format(
                "AuditLog{id=%d, eventDate=%s, eventType='%s', message='%s'}",
                id, eventDate, eventType, message
        );
    }
}
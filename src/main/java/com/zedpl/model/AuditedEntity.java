package com.zedpl.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDateTime;

/**
 * Shared createdAt/updatedAt handling via JPA lifecycle callbacks.
 * Chosen over Spring Data auditing because it needs no extra configuration
 * and no authenticated user (we only audit time, not "who").
 * The DB also has DEFAULT CURRENT_TIMESTAMP for rows inserted outside JPA.
 */
@MappedSuperclass
public abstract class AuditedEntity {

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

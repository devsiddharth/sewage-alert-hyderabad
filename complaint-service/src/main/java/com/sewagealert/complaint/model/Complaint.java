package com.sewagealert.complaint.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "complaints")
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    // latitude/longitude: GPS coordinates captured from the citizen's device when submitting the complaint
    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComplaintStatus status;  // PENDING, IN_PROGRESS, RESOLVED, REJECTED

    @Enumerated(EnumType.STRING)
    private ComplaintPriority priority;  // LOW, MEDIUM, HIGH, CRITICAL

    // createdBy: References the user ID from auth-service (not a JPA FK — loose coupling between microservices)
    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    // assignedTo: References the authority user ID who is handling this complaint
    @Column(name = "assigned_to")
    private Long assignedTo;

    // resolutionRemarks: Notes added by the authority when resolving or rejecting the complaint
    @Column(name = "resolution_remarks", columnDefinition = "TEXT")
    private String resolutionRemarks;

    // images: One complaint can have multiple images — cascade ALL so deleting a complaint removes its images
    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComplaintImage> images = new ArrayList<>();

    // history: Tracks all status changes with timestamps — ordered by updatedAt for chronological view
    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("updatedAt ASC")
    private List<ComplaintHistory> history = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Complaint() {}

    // Helper method: Adds an image to the complaint and sets the bidirectional relationship
    public void addImage(ComplaintImage image) {
        images.add(image);
        image.setComplaint(this);
    }

    // Helper method: Adds a history entry and sets the bidirectional relationship
    public void addHistory(ComplaintHistory historyEntry) {
        history.add(historyEntry);
        historyEntry.setComplaint(this);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public ComplaintStatus getStatus() { return status; }
    public void setStatus(ComplaintStatus status) { this.status = status; }

    public ComplaintPriority getPriority() { return priority; }
    public void setPriority(ComplaintPriority priority) { this.priority = priority; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public Long getAssignedTo() { return assignedTo; }
    public void setAssignedTo(Long assignedTo) { this.assignedTo = assignedTo; }

    public String getResolutionRemarks() { return resolutionRemarks; }
    public void setResolutionRemarks(String resolutionRemarks) { this.resolutionRemarks = resolutionRemarks; }

    public List<ComplaintImage> getImages() { return images; }
    public void setImages(List<ComplaintImage> images) { this.images = images; }

    public List<ComplaintHistory> getHistory() { return history; }
    public void setHistory(List<ComplaintHistory> history) { this.history = history; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

package com.sewagealert.complaint.dto;

import com.sewagealert.complaint.model.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// ComplaintResponse: DTO returned to API consumers — flattens and sanitizes the entity data
public class ComplaintResponse {

    private Long id;
    private String title;
    private String description;
    private Double latitude;
    private Double longitude;
    private String status;
    private String priority;
    private Long createdBy;
    private Long assignedTo;
    private String resolutionRemarks;
    private List<String> imageUrls;
    private List<HistoryEntry> history;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ComplaintResponse() {}

    // fromEntity: Converts the JPA entity (with its relationships) into a clean, flat response DTO
    public static ComplaintResponse fromEntity(Complaint complaint) {
        ComplaintResponse response = new ComplaintResponse();
        response.setId(complaint.getId());
        response.setTitle(complaint.getTitle());
        response.setDescription(complaint.getDescription());
        response.setLatitude(complaint.getLatitude());
        response.setLongitude(complaint.getLongitude());
        response.setStatus(complaint.getStatus().name());
        response.setPriority(complaint.getPriority() != null ? complaint.getPriority().name() : null);
        response.setCreatedBy(complaint.getCreatedBy());
        response.setAssignedTo(complaint.getAssignedTo());
        response.setResolutionRemarks(complaint.getResolutionRemarks());

        // Maps the list of ComplaintImage entities to a simple list of URL strings
        response.setImageUrls(
            complaint.getImages().stream()
                .map(ComplaintImage::getImageUrl)
                .collect(Collectors.toList())
        );

        // Maps the list of ComplaintHistory entities to lightweight HistoryEntry DTOs
        response.setHistory(
            complaint.getHistory().stream()
                .map(h -> new HistoryEntry(h.getStatus().name(), h.getRemarks(), h.getUpdatedBy(), h.getUpdatedAt()))
                .collect(Collectors.toList())
        );

        response.setCreatedAt(complaint.getCreatedAt());
        response.setUpdatedAt(complaint.getUpdatedAt());
        return response;
    }

    // Inner DTO: Lightweight representation of a single history entry — avoids exposing the full entity
    public static class HistoryEntry {
        private String status;
        private String remarks;
        private Long updatedBy;
        private LocalDateTime updatedAt;

        public HistoryEntry(String status, String remarks, Long updatedBy, LocalDateTime updatedAt) {
            this.status = status;
            this.remarks = remarks;
            this.updatedBy = updatedBy;
            this.updatedAt = updatedAt;
        }

        public String getStatus() { return status; }
        public String getRemarks() { return remarks; }
        public Long getUpdatedBy() { return updatedBy; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public Long getAssignedTo() { return assignedTo; }
    public void setAssignedTo(Long assignedTo) { this.assignedTo = assignedTo; }

    public String getResolutionRemarks() { return resolutionRemarks; }
    public void setResolutionRemarks(String resolutionRemarks) { this.resolutionRemarks = resolutionRemarks; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public List<HistoryEntry> getHistory() { return history; }
    public void setHistory(List<HistoryEntry> history) { this.history = history; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

package com.sewagealert.complaint.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "complaint_images")
@Getter
@Setter
@NoArgsConstructor
public class ComplaintImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // complaint: Many images belong to one complaint — the owning side is Complaint, so we use @ManyToOne here
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    // imageUrl: Public object-storage URL of the uploaded image (e.g. Cloudinary).
    // Only URLs are persisted — image bytes are never stored in the database.
    @Column(name = "image_url", nullable = false, length = 2048)
    private String imageUrl;

    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }


    public ComplaintImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

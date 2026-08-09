package com.sewagealert.complaint.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * ImageStorageService: Abstraction over the object-storage provider (Cloudinary today,
 * AWS S3 tomorrow). Complaint business logic depends only on this interface, so swapping
 * providers never touches {@code ComplaintService} — only a new implementation is added.
 *
 * <p>Contract: {@code upload} persists the file bytes and returns the public URL that should
 * be stored in the database; {@code delete} removes the object behind a previously returned
 * URL. Implementations must never persist image bytes themselves — the database stores URLs only.
 */
public interface ImageStorageService {

    /**
     * Uploads an image file to object storage.
     *
     * @param file the validated image file (non-empty, supported content type, within size limits)
     * @return the publicly accessible image URL to persist
     * @throws com.sewagealert.complaint.exception.ImageStorageException if the provider rejects the upload
     */
    String upload(MultipartFile file);

    /**
     * Deletes the object referenced by {@code imageUrl}. Best-effort — the caller decides
     * whether a provider failure should fail the surrounding operation.
     *
     * @param imageUrl a URL previously returned by {@link #upload(MultipartFile)}
     */
    void delete(String imageUrl);
}

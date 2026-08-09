package com.sewagealert.complaint.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.sewagealert.complaint.exception.ImageStorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * CloudinaryStorageService: {@link ImageStorageService} backed by Cloudinary.
 *
 * <p>Images are uploaded to the {@code complaints} folder and only the returned
 * {@code secure_url} is handed back for persistence — image bytes never reach MySQL.
 * Credentials come from the environment via {@code cloudinary.*} properties.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryStorageService implements ImageStorageService {

    /** Cloudinary folder under which all complaint images are stored. */
    public static final String FOLDER = "complaints";

    private final Cloudinary cloudinary;

    @Override
    public String upload(MultipartFile file) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", FOLDER,
                            "resource_type", "image"
                    )
            );
            String secureUrl = (String) result.get("secure_url");
            if (secureUrl == null || secureUrl.isBlank()) {
                throw new ImageStorageException("Cloudinary returned no secure_url for the uploaded image");
            }
            log.info("Image uploaded to Cloudinary: {}", secureUrl);
            return secureUrl;
        } catch (IOException ex) {
            log.error("Failed to upload complaint image to Cloudinary", ex);
            throw new ImageStorageException("Image upload failed. Please try again later.");
        }
    }

    @Override
    public void delete(String imageUrl) {
        try {
            String publicId = extractPublicId(imageUrl);
            if (publicId == null) {
                log.warn("Skipping Cloudinary delete — could not extract public_id from URL: {}", imageUrl);
                return;
            }
            Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Deleted Cloudinary image {} (public_id: {}) — result: {}", imageUrl, publicId, result.get("result"));
        } catch (IOException ex) {
            log.error("Failed to delete Cloudinary image: {}", imageUrl, ex);
            throw new ImageStorageException("Image deletion failed for: " + imageUrl);
        }
    }

    /**
     * Extracts the Cloudinary {@code public_id} from a delivery URL so {@code destroy}
     * can be called. Handles the default delivery format:
     *
     * <pre>https://res.cloudinary.com/&lt;cloud&gt;/image/upload/v1614000000/complaints/photo.jpg
     *                                                      └────────── public_id: complaints/photo</pre>
     *
     * <p>Package-private static so the parsing logic is directly unit-testable without
     * any Cloudinary calls.
     */
    static String extractPublicId(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return null;

        // 1. Keep everything after the delivery-type marker (/image/upload/). Without
        //    the marker the public_id cannot be derived reliably — return null so the
        //    caller skips deletion instead of destroying a bogus public_id.
        String marker = "/image/upload/";
        int markerIndex = imageUrl.indexOf(marker);
        if (markerIndex < 0) return null;
        String path = imageUrl.substring(markerIndex + marker.length());

        // 2. Drop a leading version segment (v<digits>/) when present.
        if (path.startsWith("v")) {
            int slash = path.indexOf('/');
            if (slash > 0) {
                String version = path.substring(0, slash);
                if (version.length() > 1 && version.substring(1).chars().allMatch(Character::isDigit)) {
                    path = path.substring(slash + 1);
                }
            } else if (path.length() > 1 && path.substring(1).chars().allMatch(Character::isDigit)) {
                path = ""; // whole path was just a version token — nothing to delete
            }
        }

        // 3. Strip the file extension (last dot that is not part of a directory name).
        int dot = path.lastIndexOf('.');
        if (dot > 0 && path.indexOf('/', dot) < 0) {
            path = path.substring(0, dot);
        }
        return path.isEmpty() ? null : path;
    }
}

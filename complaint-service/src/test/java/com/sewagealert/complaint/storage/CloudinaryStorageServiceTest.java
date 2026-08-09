package com.sewagealert.complaint.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.sewagealert.complaint.exception.ImageStorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * CloudinaryStorageServiceTest: Unit tests for the object-storage implementation.
 * The Cloudinary SDK client is mocked — no real network/credentials involved.
 */
class CloudinaryStorageServiceTest {

    private Cloudinary cloudinary;
    private Uploader uploader;
    private CloudinaryStorageService service;

    @BeforeEach
    void setUp() {
        cloudinary = mock(Cloudinary.class);
        uploader = mock(Uploader.class);
        when(cloudinary.uploader()).thenReturn(uploader);
        service = new CloudinaryStorageService(cloudinary);
    }

    // --- upload -------------------------------------------------------------

    @Test
    void uploadReturnsSecureUrlWhenProviderSucceeds() throws Exception {
        when(uploader.upload(any(byte[].class), anyMap()))
                .thenReturn(Map.of("secure_url", "https://res.cloudinary.com/demo/image/upload/v1/complaints/photo.jpg"));

        MockMultipartFile file = new MockMultipartFile("images", "photo.jpg", "image/jpeg", new byte[]{1, 2, 3});
        String url = service.upload(file);

        assertEquals("https://res.cloudinary.com/demo/image/upload/v1/complaints/photo.jpg", url);
        verify(uploader).upload(eq(new byte[]{1, 2, 3}), anyMap());
    }

    @Test
    void uploadFailureThrowsImageStorageExceptionAndIsNotSwallowed() throws Exception {
        when(uploader.upload(any(byte[].class), anyMap()))
                .thenThrow(new IOException("connection refused"));

        MockMultipartFile file = new MockMultipartFile("images", "photo.jpg", "image/jpeg", new byte[]{1, 2, 3});

        assertThrows(ImageStorageException.class, () -> service.upload(file));
    }

    @Test
    void uploadWithMissingSecureUrlThrowsImageStorageException() throws Exception {
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(Map.of("public_id", "complaints/photo"));

        MockMultipartFile file = new MockMultipartFile("images", "photo.jpg", "image/jpeg", new byte[]{1, 2, 3});

        assertThrows(ImageStorageException.class, () -> service.upload(file));
    }

    // --- delete -------------------------------------------------------------

    @Test
    void deleteCallsDestroyWithPublicIdParsedFromUrl() throws Exception {
        when(uploader.destroy(anyString(), anyMap())).thenReturn(Map.of("result", "ok"));

        service.delete("https://res.cloudinary.com/demo/image/upload/v1614000000/complaints/photo.jpg");

        verify(uploader).destroy(eq("complaints/photo"), anyMap());
    }

    @Test
    void deleteFailureThrowsImageStorageException() throws Exception {
        when(uploader.destroy(anyString(), anyMap())).thenThrow(new IOException("gone"));

        assertThrows(ImageStorageException.class,
                () -> service.delete("https://res.cloudinary.com/demo/image/upload/complaints/photo.jpg"));
    }

    // --- extractPublicId (pure URL parsing) ---------------------------------

    @Test
    void extractPublicIdStripsVersionAndExtension() {
        assertEquals("complaints/photo",
                CloudinaryStorageService.extractPublicId(
                        "https://res.cloudinary.com/demo/image/upload/v1614000000/complaints/photo.jpg"));
    }

    @Test
    void extractPublicIdHandlesUrlsWithoutVersionSegment() {
        assertEquals("complaints/photo",
                CloudinaryStorageService.extractPublicId(
                        "https://res.cloudinary.com/demo/image/upload/complaints/photo.png"));
    }

    @Test
    void extractPublicIdHandlesRootLevelImages() {
        assertEquals("photo",
                CloudinaryStorageService.extractPublicId(
                        "https://res.cloudinary.com/demo/image/upload/photo.webp"));
    }

    @Test
    void extractPublicIdReturnsNullForNonCloudinaryUrls() {
        // No /image/upload/ marker — the public_id cannot be derived; deletion is skipped.
        assertNull(CloudinaryStorageService.extractPublicId("https://cdn.example.com/x/y.jpg"));
    }

    @Test
    void extractPublicIdHandlesNullAndBlank() {
        assertNull(CloudinaryStorageService.extractPublicId(null));
        assertNull(CloudinaryStorageService.extractPublicId("   "));
    }
}

package com.sewagealert.complaint.service.impl;

import com.sewagealert.complaint.client.AuthServiceClient;
import com.sewagealert.complaint.client.UserServiceClient;
import com.sewagealert.complaint.config.UploadProperties;
import com.sewagealert.complaint.dto.ApiResponse;
import com.sewagealert.complaint.dto.ComplaintRequest;
import com.sewagealert.complaint.dto.ComplaintResponse;
import com.sewagealert.complaint.dto.UserProfileResponse;
import com.sewagealert.complaint.exception.ImageStorageException;
import com.sewagealert.complaint.exception.InvalidImageException;
import com.sewagealert.complaint.model.Complaint;
import com.sewagealert.complaint.model.ComplaintImage;
import com.sewagealert.complaint.producer.NotificationEventProducer;
import com.sewagealert.complaint.repository.ComplaintRepository;
import com.sewagealert.complaint.storage.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ComplaintServiceImplTest: Complaint creation with mocked object storage.
 * Verifies uploads are called before persistence, only URLs reach the entity,
 * failed uploads clean up and persist nothing, validation rejects bad files,
 * and deletion removes stored objects.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ComplaintServiceImplTest {

    private static final Long USER_ID = 1L;
    private static final String URL1 = "https://res.cloudinary.com/demo/image/upload/v1/complaints/photo1.jpg";
    private static final String URL2 = "https://res.cloudinary.com/demo/image/upload/v1/complaints/photo2.png";
    private static final String URL3 = "https://res.cloudinary.com/demo/image/upload/v1/complaints/photo3.webp";

    @Mock private ComplaintRepository complaintRepository;
    @Mock private UserServiceClient userServiceClient;
    @Mock private AuthServiceClient authServiceClient;
    @Mock private NotificationEventProducer notificationEventProducer;
    @Mock private ImageStorageService imageStorageService;

    private UploadProperties uploadProperties;
    private ComplaintServiceImpl service;

    @BeforeEach
    void setUp() {
        uploadProperties = new UploadProperties();
        service = new ComplaintServiceImpl(complaintRepository, userServiceClient, authServiceClient,
                notificationEventProducer, imageStorageService, uploadProperties);
        when(userServiceClient.getUserProfile(USER_ID))
                .thenReturn(ApiResponse.success("ok", new UserProfileResponse()));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private ComplaintRequest request() {
        ComplaintRequest request = new ComplaintRequest();
        request.setTitle("Sewage overflow");
        request.setDescription("Leak near the school");
        request.setLatitude(17.3850);
        request.setLongitude(78.4867);
        return request;
    }

    // --- creation: zero images ----------------------------------------------

    @Test
    void createWithZeroImagesPersistsComplaintWithoutImages() {
        ComplaintResponse response = service.createComplaint(USER_ID, request(), null);

        assertNotNull(response);
        verifyNoInteractions(imageStorageService);

        ArgumentCaptor<Complaint> captor = ArgumentCaptor.forClass(Complaint.class);
        verify(complaintRepository).save(captor.capture());
        assertTrue(captor.getValue().getImages().isEmpty());
    }

    // --- creation: one image -------------------------------------------------

    @Test
    void createWithOneImageUploadsAndPersistsOnlyUrl() {
        MockMultipartFile image = new MockMultipartFile("images", "photo.jpg", "image/jpeg", new byte[]{1, 2, 3});
        when(imageStorageService.upload(image)).thenReturn(URL1);

        service.createComplaint(USER_ID, request(), new MockMultipartFile[]{image});

        verify(imageStorageService).upload(image);

        ArgumentCaptor<Complaint> captor = ArgumentCaptor.forClass(Complaint.class);
        verify(complaintRepository).save(captor.capture());
        List<String> urls = captor.getValue().getImages().stream().map(ComplaintImage::getImageUrl).toList();

        // The persisted value is exactly the object-storage URL — no Base64 payloads.
        assertEquals(List.of(URL1), urls);
        assertTrue(urls.stream().noneMatch(u -> u.startsWith("data:image") || u.toLowerCase().contains("base64")));
    }

    // --- creation: multiple images -------------------------------------------

    @Test
    void createWithMultipleImagesUploadsAllAndPersistsAllUrls() {
        MockMultipartFile img1 = new MockMultipartFile("images", "a.jpg", "image/jpeg", new byte[]{1});
        MockMultipartFile img2 = new MockMultipartFile("images", "b.png", "image/png", new byte[]{2});
        MockMultipartFile img3 = new MockMultipartFile("images", "c.webp", "image/webp", new byte[]{3});
        when(imageStorageService.upload(img1)).thenReturn(URL1);
        when(imageStorageService.upload(img2)).thenReturn(URL2);
        when(imageStorageService.upload(img3)).thenReturn(URL3);

        service.createComplaint(USER_ID, request(), new MockMultipartFile[]{img1, img2, img3});

        verify(imageStorageService).upload(img1);
        verify(imageStorageService).upload(img2);
        verify(imageStorageService).upload(img3);

        ArgumentCaptor<Complaint> captor = ArgumentCaptor.forClass(Complaint.class);
        verify(complaintRepository).save(captor.capture());
        List<String> urls = captor.getValue().getImages().stream().map(ComplaintImage::getImageUrl).toList();
        assertEquals(List.of(URL1, URL2, URL3), urls);
    }

    // --- creation: failed upload ---------------------------------------------

    @Test
    void createWithFailedUploadDeletesUploadedImagesAndPersistsNothing() {
        MockMultipartFile img1 = new MockMultipartFile("images", "a.jpg", "image/jpeg", new byte[]{1});
        MockMultipartFile img2 = new MockMultipartFile("images", "b.jpg", "image/jpeg", new byte[]{2});
        when(imageStorageService.upload(img1)).thenReturn(URL1);
        when(imageStorageService.upload(img2)).thenThrow(new ImageStorageException("Cloudinary unavailable"));

        assertThrows(ImageStorageException.class,
                () -> service.createComplaint(USER_ID, request(), new MockMultipartFile[]{img1, img2}));

        // Already-uploaded object is removed so nothing is orphaned…
        verify(imageStorageService).delete(URL1);
        // …and no complaint/image rows are persisted.
        verify(complaintRepository, never()).save(any());
    }

    // --- creation: validation -------------------------------------------------

    @Test
    void createWithUnsupportedContentTypeRejectedBeforeAnyUpload() {
        MockMultipartFile image = new MockMultipartFile("images", "notes.txt", "text/plain", new byte[]{1, 2, 3});

        assertThrows(InvalidImageException.class,
                () -> service.createComplaint(USER_ID, request(), new MockMultipartFile[]{image}));

        verifyNoInteractions(imageStorageService);
        verify(complaintRepository, never()).save(any());
    }

    @Test
    void createWithEmptyFileRejectedBeforeAnyUpload() {
        MockMultipartFile image = new MockMultipartFile("images", "empty.jpg", "image/jpeg", new byte[0]);

        assertThrows(InvalidImageException.class,
                () -> service.createComplaint(USER_ID, request(), new MockMultipartFile[]{image}));

        verifyNoInteractions(imageStorageService);
        verify(complaintRepository, never()).save(any());
    }

    @Test
    void createWithOversizedFileRejectedBeforeAnyUpload() {
        uploadProperties.setMaxFileSize(DataSize.ofKilobytes(1));
        MockMultipartFile image = new MockMultipartFile("images", "big.jpg", "image/jpeg", new byte[2048]);

        assertThrows(InvalidImageException.class,
                () -> service.createComplaint(USER_ID, request(), new MockMultipartFile[]{image}));

        verifyNoInteractions(imageStorageService);
        verify(complaintRepository, never()).save(any());
    }

    // --- deletion --------------------------------------------------------------

    @Test
    void deleteComplaintRemovesStoredImagesAndComplaint() {
        Complaint complaint = new Complaint();
        complaint.setId(10L);
        complaint.addImage(new ComplaintImage(URL1));
        complaint.addImage(new ComplaintImage(URL2));
        when(complaintRepository.findById(10L)).thenReturn(Optional.of(complaint));

        service.deleteComplaint(10L);

        verify(imageStorageService).delete(URL1);
        verify(imageStorageService).delete(URL2);
        verify(complaintRepository).delete(complaint);
    }

    @Test
    void deleteComplaintProceedsWhenStorageDeleteFails() {
        Complaint complaint = new Complaint();
        complaint.setId(10L);
        complaint.addImage(new ComplaintImage(URL1));
        when(complaintRepository.findById(10L)).thenReturn(Optional.of(complaint));
        doThrow(new ImageStorageException("provider down")).when(imageStorageService).delete(URL1);

        service.deleteComplaint(10L); // best-effort — must not throw

        verify(complaintRepository).delete(complaint);
    }
}

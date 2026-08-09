package com.sewagealert.complaint.controller;

import com.sewagealert.complaint.dto.ComplaintRequest;
import com.sewagealert.complaint.dto.ComplaintResponse;
import com.sewagealert.complaint.exception.GlobalExceptionHandler;
import com.sewagealert.complaint.exception.InvalidImageException;
import com.sewagealert.complaint.service.ComplaintService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ComplaintControllerTest: API-level tests for multipart complaint creation.
 * The storage service is mocked inside the service mock — no Cloudinary calls.
 */
@ExtendWith(MockitoExtension.class)
class ComplaintControllerTest {

    private static final String URL1 = "https://res.cloudinary.com/demo/image/upload/v1/complaints/photo1.jpg";
    private static final String URL2 = "https://res.cloudinary.com/demo/image/upload/v1/complaints/photo2.jpg";

    @Mock private ComplaintService complaintService;
    @InjectMocks private ComplaintController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void createComplaintWithImagesReturnsCreatedAndUrls() throws Exception {
        ComplaintResponse response = new ComplaintResponse();
        response.setId(1L);
        response.setImageUrls(List.of(URL1, URL2));
        when(complaintService.createComplaint(eq(1L), any(ComplaintRequest.class), any()))
                .thenReturn(response);

        MockMultipartFile image = new MockMultipartFile("images", "photo.jpg", "image/jpeg", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/v1/complaints")
                        .file(image)
                        .param("title", "Sewage overflow")
                        .param("description", "Leak near road")
                        .param("latitude", "17.3850")
                        .param("longitude", "78.4867")
                        .header("X-Auth-User-Id", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.imageUrls[0]").value(URL1))
                .andExpect(jsonPath("$.data.imageUrls[1]").value(URL2));
    }

    @Test
    void createComplaintWithoutImagesReturnsCreatedWithEmptyUrls() throws Exception {
        ComplaintResponse response = new ComplaintResponse();
        response.setId(2L);
        response.setImageUrls(List.of());
        when(complaintService.createComplaint(eq(1L), any(ComplaintRequest.class), any()))
                .thenReturn(response);

        mockMvc.perform(multipart("/api/v1/complaints")
                        .param("title", "Blocked drain")
                        .param("description", "In front of the gate")
                        .param("latitude", "17.4400")
                        .param("longitude", "78.3800")
                        .header("X-Auth-User-Id", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.imageUrls").isArray())
                .andExpect(jsonPath("$.data.imageUrls").isEmpty());
    }

    @Test
    void createComplaintWithMissingTitleReturns400() throws Exception {
        mockMvc.perform(multipart("/api/v1/complaints")
                        .param("description", "No title here")
                        .param("latitude", "17.3850")
                        .param("longitude", "78.4867")
                        .header("X-Auth-User-Id", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createComplaintWithInvalidImageTypeReturns400() throws Exception {
        when(complaintService.createComplaint(eq(1L), any(ComplaintRequest.class), any()))
                .thenThrow(new InvalidImageException(
                        "Image 1 has an unsupported type. Only JPG, PNG and WEBP images are allowed."));

        MockMultipartFile textFile = new MockMultipartFile("images", "notes.txt", "text/plain", new byte[]{1});

        mockMvc.perform(multipart("/api/v1/complaints")
                        .file(textFile)
                        .param("title", "Overflow")
                        .param("description", "desc")
                        .param("latitude", "17.3850")
                        .param("longitude", "78.4867")
                        .header("X-Auth-User-Id", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Image 1 has an unsupported type. Only JPG, PNG and WEBP images are allowed."));
    }
}

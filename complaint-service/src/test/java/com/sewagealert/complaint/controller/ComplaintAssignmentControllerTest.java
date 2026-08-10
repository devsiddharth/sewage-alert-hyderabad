package com.sewagealert.complaint.controller;

import com.sewagealert.complaint.dto.AssignComplaintRequest;
import com.sewagealert.complaint.dto.ComplaintResponse;
import com.sewagealert.complaint.dto.ComplaintStatusRequest;
import com.sewagealert.complaint.exception.ForbiddenException;
import com.sewagealert.complaint.exception.GlobalExceptionHandler;
import com.sewagealert.complaint.model.ComplaintStatus;
import com.sewagealert.complaint.service.ComplaintService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ComplaintAssignmentControllerTest: API-level tests for the admin assignment endpoint
 * and the field-officer endpoints. The service is mocked; role/ownership logic is
 * covered by ComplaintAssignmentServiceTest.
 */
@ExtendWith(MockitoExtension.class)
class ComplaintAssignmentControllerTest {

    @Mock private ComplaintService complaintService;

    @InjectMocks private AdminComplaintController adminController;
    @InjectMocks private FieldOfficerComplaintController officerController;

    private MockMvc adminMvc;
    private MockMvc officerMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        adminMvc = MockMvcBuilders
                .standaloneSetup(adminController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
        officerMvc = MockMvcBuilders
                .standaloneSetup(officerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    // --- Admin assignment --------------------------------------------------------

    @Test
    void adminCanAssignComplaint() throws Exception {
        ComplaintResponse response = new ComplaintResponse();
        response.setId(42L);
        response.setAssignedTo(10L);
        when(complaintService.assignComplaint(eq(42L), eq(10L), eq(1L))).thenReturn(response);

        adminMvc.perform(put("/api/v1/complaints/admin/42/assign")
                        .header("X-Auth-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fieldOfficerId\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.assignedTo").value(10));
    }

    @Test
    void assignWithoutFieldOfficerIdReturns400() throws Exception {
        adminMvc.perform(put("/api/v1/complaints/admin/42/assign")
                        .header("X-Auth-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nonAdminAssignmentReturns403() throws Exception {
        when(complaintService.assignComplaint(eq(42L), any(), eq(99L)))
                .thenThrow(new ForbiddenException("Only administrators can assign complaints"));

        adminMvc.perform(put("/api/v1/complaints/admin/42/assign")
                        .header("X-Auth-User-Id", "99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fieldOfficerId\":10}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Only administrators can assign complaints"));
    }

    // --- Field officer ------------------------------------------------------------

    @Test
    void officerCanFetchAssignedComplaints() throws Exception {
        ComplaintResponse response = new ComplaintResponse();
        response.setId(42L);
        response.setAssignedTo(10L);
        when(complaintService.getAssignedComplaints(10L)).thenReturn(List.of(response));

        officerMvc.perform(get("/api/v1/complaints/field-officer")
                        .header("X-Auth-User-Id", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(42L));
    }

    @Test
    void nonOfficerFetchingAssignedComplaintsReturns403() throws Exception {
        when(complaintService.getAssignedComplaints(99L))
                .thenThrow(new ForbiddenException("Only field officers can access their assigned complaints"));

        officerMvc.perform(get("/api/v1/complaints/field-officer")
                        .header("X-Auth-User-Id", "99"))
                .andExpect(status().isForbidden());
    }

    @Test
    void officerCanUpdateAssignedComplaintStatus() throws Exception {
        ComplaintResponse response = new ComplaintResponse();
        response.setId(42L);
        response.setStatus(ComplaintStatus.IN_PROGRESS.name());
        when(complaintService.updateAssignedComplaintStatus(eq(42L), eq(10L), any(ComplaintStatusRequest.class)))
                .thenReturn(response);

        officerMvc.perform(patch("/api/v1/complaints/field-officer/42/status")
                        .header("X-Auth-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
    }

    @Test
    void officerUpdatingAnotherOfficersComplaintReturns403() throws Exception {
        when(complaintService.updateAssignedComplaintStatus(eq(42L), eq(11L), any(ComplaintStatusRequest.class)))
                .thenThrow(new ForbiddenException("You can only update complaints assigned to you"));

        officerMvc.perform(patch("/api/v1/complaints/field-officer/42/status")
                        .header("X-Auth-User-Id", "11")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"RESOLVED\"}"))
                .andExpect(status().isForbidden());
    }
}

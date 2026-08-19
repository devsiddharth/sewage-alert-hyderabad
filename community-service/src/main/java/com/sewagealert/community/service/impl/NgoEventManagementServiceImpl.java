package com.sewagealert.community.service.impl;

import com.sewagealert.community.dto.*;
import com.sewagealert.community.exception.ForbiddenException;
import com.sewagealert.community.model.*;
import com.sewagealert.community.repository.*;
import com.sewagealert.community.service.NgoEventManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NgoEventManagementServiceImpl implements NgoEventManagementService {

    private final NgoOrganizationRepository ngoRepository;
    private final NgoEventRepository eventRepository;
    private final NgoEventRegistrationRepository registrationRepository;

    @Override
    @Transactional
    public NgoEventResponse createEvent(Long userId, NgoEventRequest request) {
        NgoOrganization org = getVerifiedOrg(userId);

        NgoEvent event = new NgoEvent();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setLocation(request.getLocation());
        event.setEventDate(request.getEventDate());
        event.setEndDate(request.getEndDate());
        event.setEventTime(request.getEventTime());
        event.setCapacity(request.getCapacity());
        event.setCategory(request.getCategory());
        event.setNgoOrganizationId(org.getId());
        event.setApprovalStatus(EventApprovalStatus.PENDING_APPROVAL);

        event = eventRepository.save(event);
        log.info("NGO event created — eventId={}, orgId={}, title={}", event.getId(), org.getId(), event.getTitle());

        return NgoEventResponse.fromEntity(event, org.getOrganizationName(), 0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NgoEventResponse> getMyEvents(Long userId) {
        NgoOrganization org = getVerifiedOrg(userId);
        return eventRepository.findByNgoOrganizationId(org.getId()).stream()
                .map(e -> NgoEventResponse.fromEntity(e, org.getOrganizationName(),
                        registrationRepository.countByNgoEventId(e.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NgoEventResponse updateEvent(Long userId, Long eventId, NgoEventRequest request) {
        NgoOrganization org = getVerifiedOrg(userId);
        NgoEvent event = getOwnedEvent(eventId, org.getId());

        if (event.getApprovalStatus() != EventApprovalStatus.PENDING_APPROVAL
                && event.getApprovalStatus() != EventApprovalStatus.REJECTED
                && event.getApprovalStatus() != EventApprovalStatus.CANCELLED) {
            throw new ForbiddenException("Can only update events that are pending approval, rejected, or cancelled.");
        }

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setLocation(request.getLocation());
        event.setEventDate(request.getEventDate());
        event.setEndDate(request.getEndDate());
        event.setEventTime(request.getEventTime());
        event.setCapacity(request.getCapacity());
        event.setCategory(request.getCategory());
        event.setApprovalStatus(EventApprovalStatus.PENDING_APPROVAL);  // Reset for re-review
        event.setRejectionReason(null);

        event = eventRepository.save(event);
        log.info("NGO event updated — eventId={}", eventId);
        return NgoEventResponse.fromEntity(event, org.getOrganizationName(),
                registrationRepository.countByNgoEventId(eventId));
    }

    @Override
    @Transactional
    public void deleteEvent(Long userId, Long eventId) {
        NgoOrganization org = getVerifiedOrg(userId);
        NgoEvent event = getOwnedEvent(eventId, org.getId());
        eventRepository.delete(event);
        log.info("NGO event deleted — eventId={}", eventId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NgoEventResponse> getPublishedEvents() {
        return eventRepository.findByApprovalStatusAndEventDateAfter(EventApprovalStatus.PUBLISHED, LocalDate.now()).stream()
                .map(e -> {
                    String ngoName = ngoRepository.findById(e.getNgoOrganizationId())
                            .map(NgoOrganization::getOrganizationName).orElse("Unknown NGO");
                    return NgoEventResponse.fromEntity(e, ngoName,
                            registrationRepository.countByNgoEventId(e.getId()));
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NgoEventResponse> getUpcomingPublishedEvents() {
        return getPublishedEvents();
    }

    @Override
    @Transactional(readOnly = true)
    public NgoEventResponse getEvent(Long eventId) {
        NgoEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));
        String ngoName = ngoRepository.findById(event.getNgoOrganizationId())
                .map(NgoOrganization::getOrganizationName).orElse("Unknown NGO");
        return NgoEventResponse.fromEntity(event, ngoName,
                registrationRepository.countByNgoEventId(eventId));
    }

    @Override
    @Transactional
    public void registerForEvent(Long userId, Long eventId) {
        NgoEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        if (event.getApprovalStatus() != EventApprovalStatus.PUBLISHED) {
            throw new RuntimeException("Can only register for published events.");
        }

        if (registrationRepository.findByNgoEventIdAndUserId(eventId, userId).isPresent()) {
            throw new RuntimeException("Already registered for this event.");
        }

        if (event.getCapacity() != null) {
            long count = registrationRepository.countByNgoEventId(eventId);
            if (count >= event.getCapacity()) {
                throw new RuntimeException("Event is at full capacity.");
            }
        }

        NgoEventRegistration reg = new NgoEventRegistration(userId, "", "");
        event.addRegistration(reg);
        eventRepository.save(event);
        log.info("User {} registered for NGO event {}", userId, eventId);
    }

    @Override
    @Transactional
    public void cancelRegistration(Long userId, Long eventId) {
        NgoEventRegistration reg = registrationRepository.findByNgoEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new RuntimeException("Registration not found."));
        reg.setStatus(NgoEventRegistration.RegistrationStatus.CANCELLED);
        registrationRepository.save(reg);
        log.info("User {} cancelled registration for NGO event {}", userId, eventId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NgoEventResponse> getMyRegistrations(Long userId) {
        return registrationRepository.findByUserId(userId).stream()
                .filter(r -> r.getStatus() == NgoEventRegistration.RegistrationStatus.REGISTERED)
                .map(r -> {
                    NgoEvent event = r.getNgoEvent();
                    String ngoName = ngoRepository.findById(event.getNgoOrganizationId())
                            .map(NgoOrganization::getOrganizationName).orElse("Unknown NGO");
                    NgoEventResponse resp = NgoEventResponse.fromEntity(event, ngoName,
                            registrationRepository.countByNgoEventId(event.getId()));
                    resp.setRegisteredByCurrentUser(true);
                    return resp;
                })
                .collect(Collectors.toList());
    }

    // ---- Admin endpoints ----

    @Override
    @Transactional(readOnly = true)
    public List<NgoEventResponse> getPendingEvents() {
        return eventRepository.findByApprovalStatus(EventApprovalStatus.PENDING_APPROVAL).stream()
                .map(e -> {
                    String ngoName = ngoRepository.findById(e.getNgoOrganizationId())
                            .map(NgoOrganization::getOrganizationName).orElse("Unknown NGO");
                    return NgoEventResponse.fromEntity(e, ngoName,
                            registrationRepository.countByNgoEventId(e.getId()));
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NgoEventResponse approveEvent(Long eventId, Long adminUserId) {
        NgoEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));
        if (event.getApprovalStatus() != EventApprovalStatus.PENDING_APPROVAL) {
            throw new ForbiddenException("Only pending events can be approved.");
        }
        event.setApprovalStatus(EventApprovalStatus.PUBLISHED);
        event.setApprovedBy(adminUserId);
        event.setApprovedAt(java.time.LocalDateTime.now());
        event = eventRepository.save(event);

        String ngoName = ngoRepository.findById(event.getNgoOrganizationId())
                .map(NgoOrganization::getOrganizationName).orElse("Unknown NGO");
        log.info("NGO event approved — eventId={}, by admin={}", eventId, adminUserId);
        return NgoEventResponse.fromEntity(event, ngoName,
                registrationRepository.countByNgoEventId(eventId));
    }

    @Override
    @Transactional
    public NgoEventResponse rejectEvent(Long eventId, Long adminUserId, String reason) {
        NgoEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));
        event.setApprovalStatus(EventApprovalStatus.REJECTED);
        event.setRejectionReason(reason);
        event.setApprovedBy(adminUserId);
        event.setApprovedAt(java.time.LocalDateTime.now());
        event = eventRepository.save(event);

        String ngoName = ngoRepository.findById(event.getNgoOrganizationId())
                .map(NgoOrganization::getOrganizationName).orElse("Unknown NGO");
        log.info("NGO event rejected — eventId={}, by admin={}", eventId, adminUserId);
        return NgoEventResponse.fromEntity(event, ngoName,
                registrationRepository.countByNgoEventId(eventId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NgoEventResponse> getAllEventsForAdmin() {
        return eventRepository.findAll().stream()
                .map(e -> {
                    String ngoName = ngoRepository.findById(e.getNgoOrganizationId())
                            .map(NgoOrganization::getOrganizationName).orElse("Unknown NGO");
                    return NgoEventResponse.fromEntity(e, ngoName,
                            registrationRepository.countByNgoEventId(e.getId()));
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NgoParticipantResponse> getEventParticipants(Long userId, Long eventId, boolean isAdmin) {
        NgoEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        if (!isAdmin) {
            NgoOrganization org = getVerifiedOrg(userId);
            if (!event.getNgoOrganizationId().equals(org.getId())) {
                throw new ForbiddenException("You can only view participants for your own events.");
            }
        }

        return registrationRepository.findByNgoEventId(eventId).stream()
                .map(r -> new NgoParticipantResponse(
                        r.getUserId(),
                        r.getUserName(),
                        r.getUserEmail(),
                        r.getStatus().name(),
                        r.getAttendance() != null ? r.getAttendance().name() : "PENDING"))
                .collect(Collectors.toList());
    }

    // ---- Helpers ----

    private NgoOrganization getVerifiedOrg(Long userId) {
        NgoOrganization org = ngoRepository.findByRepresentativeUserId(userId)
                .orElseThrow(() -> new RuntimeException("No NGO organization found for this user."));
        if (org.getStatus() != NgoApplicationStatus.APPROVED) {
            throw new ForbiddenException("Only verified NGOs can manage events.");
        }
        return org;
    }

    private NgoEvent getOwnedEvent(Long eventId, Long ngoId) {
        NgoEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));
        if (!event.getNgoOrganizationId().equals(ngoId)) {
            throw new ForbiddenException("You can only manage your own events.");
        }
        return event;
    }
}

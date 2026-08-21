package com.sewagealert.community.controller;

import com.sewagealert.community.dto.*;
import com.sewagealert.community.model.*;
import com.sewagealert.community.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * InternalAiDataController: Internal endpoints for the AI Service to retrieve
 * grounded platform data. NOT routed through the API Gateway — only reachable
 * via service-to-service Feign calls over Eureka. No JWT required.
 */
@RestController
@RequestMapping("/api/v1/internal/ai")
@Tag(name = "Internal AI Data", description = "Internal endpoints for AI data retrieval — not routed through the API Gateway")
public class InternalAiDataController {

    private final NgoOrganizationRepository ngoOrganizationRepository;
    private final NgoDriveRepository ngoDriveRepository;
    private final NgoDriveParticipationRepository ngoDriveParticipationRepository;
    private final NgoEventRepository ngoEventRepository;
    private final EventRepository eventRepository;
    private final ArticleRepository articleRepository;
    private final NgoProgressRepository ngoProgressRepository;
    private final PipelineRepository pipelineRepository;
    private final LakeRepository lakeRepository;
    private final TreatmentPlantRepository treatmentPlantRepository;

    public InternalAiDataController(
            NgoOrganizationRepository ngoOrganizationRepository,
            NgoDriveRepository ngoDriveRepository,
            NgoDriveParticipationRepository ngoDriveParticipationRepository,
            NgoEventRepository ngoEventRepository,
            EventRepository eventRepository,
            ArticleRepository articleRepository,
            NgoProgressRepository ngoProgressRepository,
            PipelineRepository pipelineRepository,
            LakeRepository lakeRepository,
            TreatmentPlantRepository treatmentPlantRepository) {
        this.ngoOrganizationRepository = ngoOrganizationRepository;
        this.ngoDriveRepository = ngoDriveRepository;
        this.ngoDriveParticipationRepository = ngoDriveParticipationRepository;
        this.ngoEventRepository = ngoEventRepository;
        this.eventRepository = eventRepository;
        this.articleRepository = articleRepository;
        this.ngoProgressRepository = ngoProgressRepository;
        this.pipelineRepository = pipelineRepository;
        this.lakeRepository = lakeRepository;
        this.treatmentPlantRepository = treatmentPlantRepository;
    }

    @GetMapping("/ngos")
    @Operation(summary = "Get all approved NGOs for AI context")
    public ResponseEntity<ApiResponse<List<NgoOrganizationResponse>>> getApprovedNgos() {
        List<NgoOrganizationResponse> ngos = ngoOrganizationRepository
                .findByStatus(NgoApplicationStatus.APPROVED)
                .stream()
                .map(NgoOrganizationResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("NGOs retrieved", ngos));
    }

    @GetMapping("/ngos/{userId}")
    @Operation(summary = "Get NGO organization by representative user ID")
    public ResponseEntity<ApiResponse<NgoOrganizationResponse>> getNgoByUserId(@PathVariable Long userId) {
        return ngoOrganizationRepository.findByRepresentativeUserId(userId)
                .map(org -> ResponseEntity.ok(ApiResponse.success("NGO retrieved", NgoOrganizationResponse.fromEntity(org))))
                .orElse(ResponseEntity.ok(ApiResponse.error("No NGO organization found for this user", null)));
    }

    @GetMapping("/drives")
    @Operation(summary = "Get all drives for AI context")
    public ResponseEntity<ApiResponse<List<NgoDriveResponse>>> getAllDrives() {
        List<NgoDriveResponse> drives = ngoDriveRepository.findAll().stream()
                .map(d -> {
                    String ngoName = ngoOrganizationRepository.findById(d.getNgoOrganizationId())
                            .map(NgoOrganization::getOrganizationName).orElse("Unknown NGO");
                    long participants = ngoDriveParticipationRepository.countByNgoDriveId(d.getId());
                    return NgoDriveResponse.fromEntity(d, ngoName, participants);
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Drives retrieved", drives));
    }

    @GetMapping("/drives/upcoming")
    @Operation(summary = "Get upcoming drives (end date >= today)")
    public ResponseEntity<ApiResponse<List<NgoDriveResponse>>> getUpcomingDrives() {
        LocalDate today = LocalDate.now();
        List<NgoDriveResponse> drives = ngoDriveRepository.findAll().stream()
                .filter(d -> d.getEndDate() == null || !d.getEndDate().isBefore(today))
                .filter(d -> d.getStatus() != NgoDrive.DriveStatus.CANCELLED
                        && d.getStatus() != NgoDrive.DriveStatus.COMPLETED)
                .map(d -> {
                    String ngoName = ngoOrganizationRepository.findById(d.getNgoOrganizationId())
                            .map(NgoOrganization::getOrganizationName).orElse("Unknown NGO");
                    long participants = ngoDriveParticipationRepository.countByNgoDriveId(d.getId());
                    return NgoDriveResponse.fromEntity(d, ngoName, participants);
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Upcoming drives retrieved", drives));
    }

    @GetMapping("/drives/ngo/{ngoOrgId}")
    @Operation(summary = "Get drives for a specific NGO organization")
    public ResponseEntity<ApiResponse<List<NgoDriveResponse>>> getDrivesByNgo(@PathVariable Long ngoOrgId) {
        String ngoName = ngoOrganizationRepository.findById(ngoOrgId)
                .map(NgoOrganization::getOrganizationName).orElse("Unknown NGO");
        List<NgoDriveResponse> drives = ngoDriveRepository.findByNgoOrganizationId(ngoOrgId).stream()
                .map(d -> NgoDriveResponse.fromEntity(d, ngoName,
                        ngoDriveParticipationRepository.countByNgoDriveId(d.getId())))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("NGO drives retrieved", drives));
    }

    @GetMapping("/events")
    @Operation(summary = "Get all platform events for AI context")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getAllEvents() {
        List<EventResponse> events = eventRepository.findAll().stream()
                .map(EventResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Events retrieved", events));
    }

    @GetMapping("/events/upcoming")
    @Operation(summary = "Get upcoming platform events")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getUpcomingEvents() {
        List<EventResponse> events = eventRepository.findByEventDateAfter(LocalDate.now()).stream()
                .map(EventResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Upcoming events retrieved", events));
    }

    @GetMapping("/ngo-events")
    @Operation(summary = "Get all published NGO events for AI context")
    public ResponseEntity<ApiResponse<List<NgoEventResponse>>> getAllNgoEvents() {
        List<NgoEventResponse> events = ngoEventRepository
                .findByApprovalStatus(EventApprovalStatus.APPROVED).stream()
                .map(e -> {
                    String ngoName = ngoOrganizationRepository.findById(e.getNgoOrganizationId())
                            .map(NgoOrganization::getOrganizationName).orElse("Unknown NGO");
                    return NgoEventResponse.fromEntity(e, ngoName, 0);
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("NGO events retrieved", events));
    }

    @GetMapping("/ngo-events/ngo/{ngoOrgId}")
    @Operation(summary = "Get NGO events for a specific organization")
    public ResponseEntity<ApiResponse<List<NgoEventResponse>>> getNgoEventsByOrg(@PathVariable Long ngoOrgId) {
        String ngoName = ngoOrganizationRepository.findById(ngoOrgId)
                .map(NgoOrganization::getOrganizationName).orElse("Unknown NGO");
        List<NgoEventResponse> events = ngoEventRepository.findByNgoOrganizationId(ngoOrgId).stream()
                .map(e -> NgoEventResponse.fromEntity(e, ngoName, 0))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("NGO events retrieved", events));
    }

    @GetMapping("/articles")
    @Operation(summary = "Get all articles for AI context")
    public ResponseEntity<ApiResponse<List<ArticleResponse>>> getAllArticles() {
        List<ArticleResponse> articles = articleRepository.findAll().stream()
                .map(ArticleResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Articles retrieved", articles));
    }

    @GetMapping("/articles/category/{category}")
    @Operation(summary = "Get articles by category")
    public ResponseEntity<ApiResponse<List<ArticleResponse>>> getArticlesByCategory(@PathVariable String category) {
        List<ArticleResponse> articles = articleRepository.findByCategory(category).stream()
                .map(ArticleResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Articles retrieved", articles));
    }

    @GetMapping("/articles/search")
    @Operation(summary = "Search articles by keyword")
    public ResponseEntity<ApiResponse<List<ArticleResponse>>> searchArticles(@RequestParam String keyword) {
        List<ArticleResponse> articles = articleRepository.findByTitleContainingIgnoreCase(keyword).stream()
                .map(ArticleResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Articles found", articles));
    }

    @GetMapping("/progress/ngo/{ngoOrgId}")
    @Operation(summary = "Get NGO progress data")
    public ResponseEntity<ApiResponse<NgoProgressResponse>> getNgoProgress(@PathVariable Long ngoOrgId) {
        return ngoProgressRepository.findByNgoOrganizationId(ngoOrgId)
                .map(p -> ResponseEntity.ok(ApiResponse.success("Progress retrieved",
                        NgoProgressResponse.fromEntity(p))))
                .orElse(ResponseEntity.ok(ApiResponse.error("No progress data found", null)));
    }

    // ---- Infrastructure endpoints ----

    @GetMapping("/pipelines")
    @Operation(summary = "Get all pipelines for AI context")
    public ResponseEntity<ApiResponse<List<PipelineResponse>>> getAllPipelines() {
        List<PipelineResponse> pipelines = pipelineRepository.findAll().stream()
                .map(PipelineResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Pipelines retrieved", pipelines));
    }

    @GetMapping("/pipelines/locality/{locality}")
    @Operation(summary = "Get pipelines by locality")
    public ResponseEntity<ApiResponse<List<PipelineResponse>>> getPipelinesByLocality(@PathVariable String locality) {
        List<PipelineResponse> pipelines = pipelineRepository.findByLocalityContainingIgnoreCase(locality).stream()
                .map(PipelineResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Pipelines retrieved", pipelines));
    }

    @GetMapping("/lakes")
    @Operation(summary = "Get all lakes for AI context")
    public ResponseEntity<ApiResponse<List<LakeResponse>>> getAllLakes() {
        List<LakeResponse> lakes = lakeRepository.findAll().stream()
                .map(LakeResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Lakes retrieved", lakes));
    }

    @GetMapping("/lakes/{lakeId}")
    @Operation(summary = "Get a specific lake")
    public ResponseEntity<ApiResponse<LakeResponse>> getLake(@PathVariable Long lakeId) {
        return lakeRepository.findById(lakeId)
                .map(lake -> ResponseEntity.ok(ApiResponse.success("Lake retrieved", LakeResponse.fromEntity(lake))))
                .orElse(ResponseEntity.ok(ApiResponse.error("Lake not found", null)));
    }

    @GetMapping("/treatment-plants")
    @Operation(summary = "Get all treatment plants for AI context")
    public ResponseEntity<ApiResponse<List<TreatmentPlantResponse>>> getAllTreatmentPlants() {
        List<TreatmentPlantResponse> plants = treatmentPlantRepository.findAll().stream()
                .map(TreatmentPlantResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Treatment plants retrieved", plants));
    }

    @GetMapping("/treatment-plants/{plantId}")
    @Operation(summary = "Get a specific treatment plant")
    public ResponseEntity<ApiResponse<TreatmentPlantResponse>> getTreatmentPlant(@PathVariable Long plantId) {
        return treatmentPlantRepository.findById(plantId)
                .map(plant -> ResponseEntity.ok(ApiResponse.success("Treatment plant retrieved", TreatmentPlantResponse.fromEntity(plant))))
                .orElse(ResponseEntity.ok(ApiResponse.error("Treatment plant not found", null)));
    }
}

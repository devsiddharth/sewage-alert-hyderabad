package com.sewagealert.ai.client;

import com.sewagealert.ai.dto.ApiResponse;
import com.sewagealert.ai.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * CommunityServiceClient: Declarative OpenFeign client for COMMUNITY-SERVICE's internal AI data endpoints.
 * Uses Eureka service discovery (lb://COMMUNITY-SERVICE).
 * Calls /api/v1/internal/ai/... which is NOT routed by the API Gateway.
 */
@FeignClient(name = "COMMUNITY-SERVICE")
public interface CommunityServiceClient {

    @GetMapping("/api/v1/internal/ai/ngos")
    ApiResponse<List<NgoData>> getApprovedNgos();

    @GetMapping("/api/v1/internal/ai/ngos/{userId}")
    ApiResponse<NgoData> getNgoByUserId(@PathVariable("userId") Long userId);

    @GetMapping("/api/v1/internal/ai/drives")
    ApiResponse<List<DriveData>> getAllDrives();

    @GetMapping("/api/v1/internal/ai/drives/upcoming")
    ApiResponse<List<DriveData>> getUpcomingDrives();

    @GetMapping("/api/v1/internal/ai/drives/ngo/{ngoOrgId}")
    ApiResponse<List<DriveData>> getDrivesByNgo(@PathVariable("ngoOrgId") Long ngoOrgId);

    @GetMapping("/api/v1/internal/ai/events")
    ApiResponse<List<EventData>> getAllEvents();

    @GetMapping("/api/v1/internal/ai/events/upcoming")
    ApiResponse<List<EventData>> getUpcomingEvents();

    @GetMapping("/api/v1/internal/ai/ngo-events")
    ApiResponse<List<NgoEventData>> getAllNgoEvents();

    @GetMapping("/api/v1/internal/ai/ngo-events/ngo/{ngoOrgId}")
    ApiResponse<List<NgoEventData>> getNgoEventsByOrg(@PathVariable("ngoOrgId") Long ngoOrgId);

    @GetMapping("/api/v1/internal/ai/articles")
    ApiResponse<List<ArticleData>> getAllArticles();

    @GetMapping("/api/v1/internal/ai/articles/category/{category}")
    ApiResponse<List<ArticleData>> getArticlesByCategory(@PathVariable("category") String category);

    @GetMapping("/api/v1/internal/ai/articles/search")
    ApiResponse<List<ArticleData>> searchArticles(@RequestParam("keyword") String keyword);

    @GetMapping("/api/v1/internal/ai/progress/ngo/{ngoOrgId}")
    ApiResponse<NgoProgressData> getNgoProgress(@PathVariable("ngoOrgId") Long ngoOrgId);

    // ---- Infrastructure endpoints ----

    @GetMapping("/api/v1/internal/ai/pipelines")
    ApiResponse<List<PipelineData>> getAllPipelines();

    @GetMapping("/api/v1/internal/ai/pipelines/locality/{locality}")
    ApiResponse<List<PipelineData>> getPipelinesByLocality(@PathVariable("locality") String locality);

    @GetMapping("/api/v1/internal/ai/lakes")
    ApiResponse<List<LakeData>> getAllLakes();

    @GetMapping("/api/v1/internal/ai/lakes/{lakeId}")
    ApiResponse<LakeData> getLake(@PathVariable("lakeId") Long lakeId);

    @GetMapping("/api/v1/internal/ai/treatment-plants")
    ApiResponse<List<TreatmentPlantData>> getAllTreatmentPlants();

    @GetMapping("/api/v1/internal/ai/treatment-plants/{plantId}")
    ApiResponse<TreatmentPlantData> getTreatmentPlant(@PathVariable("plantId") Long plantId);
}

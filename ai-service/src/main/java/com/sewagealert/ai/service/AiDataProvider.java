package com.sewagealert.ai.service;

import com.sewagealert.ai.client.AuthServiceClient;
import com.sewagealert.ai.client.CommunityServiceClient;
import com.sewagealert.ai.client.ComplaintServiceClient;
import com.sewagealert.ai.dto.*;
import com.sewagealert.ai.exception.AiProviderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AiDataProvider: Retrieves relevant structured platform data based on the detected intent
 * and builds a grounded context string for the AI provider.
 * This prevents hallucination — the AI only answers from actual platform data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiDataProvider {

    private final AuthServiceClient authServiceClient;
    private final CommunityServiceClient communityServiceClient;
    private final ComplaintServiceClient complaintServiceClient;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Look up a user's role from auth-service.
     */
    public Optional<String> getUserRole(Long userId) {
        try {
            ApiResponse<UserRoleResponse> response = authServiceClient.getUserRole(userId);
            if (response.isSuccess() && response.getData() != null) {
                return Optional.ofNullable(response.getData().getRole());
            }
        } catch (Exception e) {
            log.warn("Failed to get user role for userId={}: {}", userId, e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Look up the NGO organization for a representative user.
     */
    public Optional<NgoData> getNgoForUser(Long userId) {
        try {
            ApiResponse<NgoData> response = communityServiceClient.getNgoByUserId(userId);
            if (response.isSuccess() && response.getData() != null) {
                return Optional.of(response.getData());
            }
        } catch (Exception e) {
            log.warn("Failed to get NGO for userId={}: {}", userId, e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Build a grounded context string based on the detected intent and user's query.
     * Returns the context to be injected into the AI prompt, plus whether data was used.
     */
    public ContextResult buildContext(String query, AiIntent intent, Long userId, String role) {
        StringBuilder context = new StringBuilder();
        boolean dataUsed = false;

        try {
            switch (intent) {
                case NGO_DISCOVERY -> {
                    List<NgoData> ngos = safeCall(() -> communityServiceClient.getApprovedNgos());
                    if (ngos != null && !ngos.isEmpty()) {
                        context.append("Available NGOs on the platform:\n");
                        for (NgoData ngo : ngos) {
                            context.append(String.format("- %s (Areas: %s, Focus: %s, Mission: %s)\n",
                                    ngo.getOrganizationName(),
                                    ngo.getOperatingAreas() != null ? ngo.getOperatingAreas() : "N/A",
                                    ngo.getAreasOfFocus() != null ? ngo.getAreasOfFocus() : "N/A",
                                    ngo.getMission() != null ? truncate(ngo.getMission(), 100) : "N/A"));
                        }
                        dataUsed = true;
                    }
                }
                case DRIVE_DISCOVERY -> {
                    List<DriveData> drives = safeCall(() -> communityServiceClient.getUpcomingDrives());
                    if (drives != null && !drives.isEmpty()) {
                        context.append("Upcoming drives on the platform:\n");
                        for (DriveData d : drives) {
                            context.append(String.format("- \"%s\" (%s) at %s, dates: %s to %s, status: %s, NGO: %s, participants: %d\n",
                                    d.getTitle(), d.getDriveType() != null ? d.getDriveType() : "General",
                                    d.getLocation(), d.getStartDate(), d.getEndDate(),
                                    d.getStatus(), d.getNgoOrganizationName(), d.getCurrentParticipants()));
                        }
                        dataUsed = true;
                    } else {
                        context.append("No upcoming drives found on the platform.\n");
                        dataUsed = true;
                    }
                }
                case EVENT_DISCOVERY -> {
                    List<EventData> events = safeCall(() -> communityServiceClient.getUpcomingEvents());
                    List<NgoEventData> ngoEvents = safeCall(() -> communityServiceClient.getAllNgoEvents());
                    if ((events != null && !events.isEmpty()) || (ngoEvents != null && !ngoEvents.isEmpty())) {
                        context.append("Upcoming events:\n");
                        if (events != null) {
                            for (EventData e : events) {
                                context.append(String.format("- \"%s\" at %s on %s by %s (capacity: %s, registered: %d)\n",
                                        e.getTitle(), e.getLocation(), e.getEventDate(),
                                        e.getOrganizerName(),
                                        e.getCapacity() != null ? e.getCapacity() : "unlimited",
                                        e.getRegisteredCount()));
                            }
                        }
                        if (ngoEvents != null) {
                            for (NgoEventData e : ngoEvents) {
                                context.append(String.format("- \"%s\" at %s on %s by NGO: %s (registered: %d)\n",
                                        e.getTitle(), e.getLocation(), e.getEventDate(),
                                        e.getNgoOrganizationName(), e.getRegisteredCount()));
                            }
                        }
                        dataUsed = true;
                    } else {
                        context.append("No upcoming events found on the platform.\n");
                        dataUsed = true;
                    }
                }
                case ARTICLE_QUERY -> {
                    List<ArticleData> articles = safeCall(() -> communityServiceClient.getAllArticles());
                    if (articles != null && !articles.isEmpty()) {
                        context.append("Available articles:\n");
                        for (ArticleData a : articles) {
                            context.append(String.format("- \"%s\" (Category: %s, Author: %s, Published: %s)\n",
                                    a.getTitle(), a.getCategory(), a.getAuthorName(), a.getPublishedAt()));
                        }
                        dataUsed = true;
                    }
                }
                case ARTICLE_GENERATION -> {
                    // For article generation, provide recent articles as style reference
                    List<ArticleData> articles = safeCall(() -> communityServiceClient.getAllArticles());
                    if (articles != null && !articles.isEmpty()) {
                        context.append("Recent articles for reference style:\n");
                        articles.stream().limit(5).forEach(a ->
                                context.append(String.format("- \"%s\" (Category: %s)\n", a.getTitle(), a.getCategory())));
                        dataUsed = true;
                    }
                    context.append("Generate a new article draft based on the user's request. "
                            + "The output should be structured with title, content, and category. "
                            + "Do NOT publish it — it will be reviewed by the user first.\n");
                }
                case COMPLAINT_QUERY, COMPLAINT_INSIGHTS -> {
                    Map<String, Object> insights = safeCall(() -> complaintServiceClient.getComplaintInsights());
                    if (insights != null) {
                        context.append("Complaint platform data:\n");
                        context.append(String.format("- Total complaints: %s\n", insights.getOrDefault("total", 0)));
                        context.append(String.format("- Pending: %s\n", insights.getOrDefault("pending", 0)));
                        context.append(String.format("- In Progress: %s\n", insights.getOrDefault("inProgress", 0)));
                        context.append(String.format("- Resolved: %s\n", insights.getOrDefault("resolved", 0)));
                        context.append(String.format("- Rejected: %s\n", insights.getOrDefault("rejected", 0)));
                        context.append(String.format("- Overdue (>7 days): %s\n", insights.getOrDefault("overdueCount", 0)));
                        if (insights.get("priorityDistribution") != null) {
                            context.append("- Priority distribution: ").append(insights.get("priorityDistribution")).append("\n");
                        }
                        if (insights.get("topLocations") != null) {
                            context.append("- Top complaint areas: ").append(insights.get("topLocations")).append("\n");
                        }
                        if (insights.get("recurringLocations") != null) {
                            context.append("- Recurring problem areas: ").append(insights.get("recurringLocations")).append("\n");
                        }
                        dataUsed = true;
                    }
                }
                case COMMUNITY_QUERY -> {
                    List<NgoData> ngos = safeCall(() -> communityServiceClient.getApprovedNgos());
                    List<EventData> events = safeCall(() -> communityServiceClient.getUpcomingEvents());
                    List<DriveData> drives = safeCall(() -> communityServiceClient.getUpcomingDrives());
                    List<ArticleData> articles = safeCall(() -> communityServiceClient.getAllArticles());

                    context.append("Community platform overview:\n");
                    if (ngos != null) {
                        context.append(String.format("- Active NGOs: %d\n", ngos.size()));
                        ngos.stream().limit(5).forEach(n ->
                                context.append(String.format("  - %s (Areas: %s)\n",
                                        n.getOrganizationName(),
                                        n.getOperatingAreas() != null ? n.getOperatingAreas() : "N/A")));
                    }
                    if (events != null) {
                        context.append(String.format("- Upcoming events: %d\n", events.size()));
                    }
                    if (drives != null) {
                        context.append(String.format("- Upcoming drives: %d\n", drives.size()));
                    }
                    if (articles != null) {
                        context.append(String.format("- Published articles: %d\n", articles.size()));
                    }
                    dataUsed = true;
                }
                case NGO_ANALYTICS -> {
                    if (userId != null && "NGO_REPRESENTATIVE".equals(role)) {
                        Optional<NgoData> ngo = getNgoForUser(userId);
                        if (ngo.isPresent()) {
                            Long ngoOrgId = ngo.get().getId();
                            List<DriveData> drives = safeCall(() -> communityServiceClient.getDrivesByNgo(ngoOrgId));
                            List<NgoEventData> events = safeCall(() -> communityServiceClient.getNgoEventsByOrg(ngoOrgId));
                            NgoProgressData progress = safeCall(() -> communityServiceClient.getNgoProgress(ngoOrgId));

                            context.append(String.format("NGO Organization: %s\n", ngo.get().getOrganizationName()));
                            if (drives != null) {
                                context.append(String.format("Drives: %d total\n", drives.size()));
                                drives.forEach(d -> context.append(String.format("  - \"%s\" (%s) at %s, status: %s, participants: %d\n",
                                        d.getTitle(), d.getDriveType(), d.getLocation(), d.getStatus(), d.getCurrentParticipants())));
                            }
                            if (events != null) {
                                context.append(String.format("Events: %d total\n", events.size()));
                            }
                            if (progress != null) {
                                context.append(String.format("Progress: %d drives conducted, %d events, %d volunteers, %d people reached\n",
                                        progress.getDrivesConducted(), progress.getEventsConducted(),
                                        progress.getVolunteersInvolved(), progress.getPeopleReached()));
                            }
                            dataUsed = true;
                        }
                    }
                }
                case ADMIN_ANALYTICS -> {
                    Map<String, Object> insights = safeCall(() -> complaintServiceClient.getComplaintInsights());
                    List<NgoData> ngos = safeCall(() -> communityServiceClient.getApprovedNgos());
                    List<EventData> events = safeCall(() -> communityServiceClient.getUpcomingEvents());
                    List<DriveData> drives = safeCall(() -> communityServiceClient.getUpcomingDrives());

                    context.append("Platform-wide analytics:\n");
                    if (insights != null) {
                        context.append(String.format("Complaints: %s total, %s pending, %s in progress, %s resolved, %s rejected, %s overdue\n",
                                insights.getOrDefault("total", 0), insights.getOrDefault("pending", 0),
                                insights.getOrDefault("inProgress", 0), insights.getOrDefault("resolved", 0),
                                insights.getOrDefault("rejected", 0), insights.getOrDefault("overdueCount", 0)));
                        if (insights.get("topLocations") != null) {
                            context.append("Top complaint areas: ").append(insights.get("topLocations")).append("\n");
                        }
                        if (insights.get("recurringLocations") != null) {
                            context.append("Recurring problem areas: ").append(insights.get("recurringLocations")).append("\n");
                        }
                    }
                    if (ngos != null) {
                        context.append(String.format("Active NGOs: %d\n", ngos.size()));
                    }
                    if (events != null) {
                        context.append(String.format("Upcoming events: %d\n", events.size()));
                    }
                    if (drives != null) {
                        context.append(String.format("Upcoming drives: %d\n", drives.size()));
                    }
                    dataUsed = true;
                }
                case SUMMARY_GENERATION -> {
                    // Provide relevant data based on what the user wants summarized
                    if ("NGO_REPRESENTATIVE".equals(role) && userId != null) {
                        Optional<NgoData> ngo = getNgoForUser(userId);
                        if (ngo.isPresent()) {
                            Long ngoOrgId = ngo.get().getId();
                            List<DriveData> drives = safeCall(() -> communityServiceClient.getDrivesByNgo(ngoOrgId));
                            NgoProgressData progress = safeCall(() -> communityServiceClient.getNgoProgress(ngoOrgId));
                            context.append(String.format("NGO: %s\n", ngo.get().getOrganizationName()));
                            if (drives != null) {
                                context.append("Drives:\n");
                                drives.forEach(d -> context.append(String.format("  - \"%s\" (%s) at %s, status: %s\n",
                                        d.getTitle(), d.getDriveType(), d.getLocation(), d.getStatus())));
                            }
                            if (progress != null) {
                                context.append(String.format("Metrics: %d drives, %d events, %d volunteers, %d people reached\n",
                                        progress.getDrivesConducted(), progress.getEventsConducted(),
                                        progress.getVolunteersInvolved(), progress.getPeopleReached()));
                            }
                            dataUsed = true;
                        }
                    } else {
                        // Admin or general summary
                        Map<String, Object> insights = safeCall(() -> complaintServiceClient.getComplaintInsights());
                        if (insights != null) {
                            context.append("Platform summary:\n");
                            context.append(String.format("Total complaints: %s, Resolved: %s, Pending: %s\n",
                                    insights.getOrDefault("total", 0), insights.getOrDefault("resolved", 0),
                                    insights.getOrDefault("pending", 0)));
                            dataUsed = true;
                        }
                    }
                }
                case PIPELINE_QUERY -> {
                    List<PipelineData> pipelines = safeCall(() -> communityServiceClient.getAllPipelines());
                    if (pipelines != null && !pipelines.isEmpty()) {
                        context.append("Sewage pipeline infrastructure in Hyderabad:\n");
                        for (PipelineData p : pipelines) {
                            context.append(String.format("- Pipeline in %s: installed %s, capacity %s thousand people, status: %s, last maintenance: %s\n",
                                    p.getLocality(),
                                    p.getInstallationYear() != null ? p.getInstallationYear().toString() : "unknown",
                                    p.getDesignedCapacity() != null ? p.getDesignedCapacity().toString() : "unknown",
                                    p.getOperationalStatus(),
                                    p.getMaintenanceDate() != null ? p.getMaintenanceDate() : "no record"));
                            if (p.getNotes() != null && !p.getNotes().isBlank()) {
                                context.append(String.format("  Notes: %s\n", truncate(p.getNotes(), 150)));
                            }
                        }
                        dataUsed = true;
                    } else {
                        context.append("No pipeline data found on the platform.\n");
                        dataUsed = true;
                    }
                }
                case LAKE_QUERY -> {
                    List<LakeData> lakes = safeCall(() -> communityServiceClient.getAllLakes());
                    if (lakes != null && !lakes.isEmpty()) {
                        context.append("Lakes in Hyderabad:\n");
                        for (LakeData l : lakes) {
                            context.append(String.format("- %s at %s: restoration status: %s, water source: %s\n",
                                    l.getName(),
                                    l.getLocation() != null ? l.getLocation() : "unknown",
                                    l.getRestorationStatus() != null ? l.getRestorationStatus() : "unknown",
                                    l.getWaterSource() != null ? l.getWaterSource() : "unknown"));
                            if (l.getDescription() != null && !l.getDescription().isBlank()) {
                                context.append(String.format("  Description: %s\n", truncate(l.getDescription(), 150)));
                            }
                            if (l.getEnvironmentalUpdates() != null && !l.getEnvironmentalUpdates().isBlank()) {
                                context.append(String.format("  Environmental updates: %s\n", truncate(l.getEnvironmentalUpdates(), 150)));
                            }
                        }
                        dataUsed = true;
                    } else {
                        context.append("No lake data found on the platform.\n");
                        dataUsed = true;
                    }
                }
                case STP_QUERY -> {
                    List<TreatmentPlantData> plants = safeCall(() -> communityServiceClient.getAllTreatmentPlants());
                    if (plants != null && !plants.isEmpty()) {
                        context.append("Sewage Treatment Plants (STPs) in Hyderabad:\n");
                        for (TreatmentPlantData t : plants) {
                            context.append(String.format("- %s at %s: capacity %s MLD, method: %s\n",
                                    t.getName(),
                                    t.getLocation(),
                                    t.getCapacityMld() != null ? t.getCapacityMld().toString() : "unknown",
                                    t.getTreatmentMethod() != null ? t.getTreatmentMethod() : "unknown"));
                            if (t.getWaterReuseInfo() != null && !t.getWaterReuseInfo().isBlank()) {
                                context.append(String.format("  Water reuse: %s\n", truncate(t.getWaterReuseInfo(), 150)));
                            }
                            if (t.getDescription() != null && !t.getDescription().isBlank()) {
                                context.append(String.format("  Description: %s\n", truncate(t.getDescription(), 150)));
                            }
                        }
                        dataUsed = true;
                    } else {
                        context.append("No treatment plant data found on the platform.\n");
                        dataUsed = true;
                    }
                }
                case INFRASTRUCTURE_QUERY -> {
                    List<PipelineData> pipelines = safeCall(() -> communityServiceClient.getAllPipelines());
                    List<LakeData> lakes = safeCall(() -> communityServiceClient.getAllLakes());
                    List<TreatmentPlantData> plants = safeCall(() -> communityServiceClient.getAllTreatmentPlants());

                    context.append("Infrastructure overview:\n");
                    if (pipelines != null) {
                        context.append(String.format("- Pipelines: %d total\n", pipelines.size()));
                        long active = pipelines.stream().filter(p -> "ACTIVE".equals(p.getOperationalStatus())).count();
                        long maintenance = pipelines.stream().filter(p -> "UNDER_MAINTENANCE".equals(p.getOperationalStatus())).count();
                        long decommissioned = pipelines.stream().filter(p -> "DECOMMISSIONED".equals(p.getOperationalStatus())).count();
                        context.append(String.format("  Status: %d active, %d under maintenance, %d decommissioned\n", active, maintenance, decommissioned));
                        pipelines.stream().limit(5).forEach(p ->
                                context.append(String.format("  - %s (installed %s, status: %s)\n",
                                        p.getLocality(), p.getInstallationYear(), p.getOperationalStatus())));
                    }
                    if (lakes != null) {
                        context.append(String.format("- Lakes: %d total\n", lakes.size()));
                        lakes.stream().limit(5).forEach(l ->
                                context.append(String.format("  - %s (restoration: %s, source: %s)\n",
                                        l.getName(), l.getRestorationStatus(), l.getWaterSource())));
                    }
                    if (plants != null) {
                        context.append(String.format("- Treatment Plants: %d total\n", plants.size()));
                        double totalCapacity = plants.stream()
                                .filter(p -> p.getCapacityMld() != null)
                                .mapToDouble(TreatmentPlantData::getCapacityMld)
                                .sum();
                        context.append(String.format("  Total capacity: %.1f MLD\n", totalCapacity));
                        plants.stream().limit(5).forEach(t ->
                                context.append(String.format("  - %s at %s (%.1f MLD, %s)\n",
                                        t.getName(), t.getLocation(),
                                        t.getCapacityMld() != null ? t.getCapacityMld() : 0.0,
                                        t.getTreatmentMethod())));
                    }
                    dataUsed = true;
                }
                case INFRASTRUCTURE_CORRELATION -> {
                    // Retrieve both complaint correlation data and infrastructure data
                    CorrelationData correlation = safeCall(() -> complaintServiceClient.getComplaintCorrelation());
                    List<PipelineData> pipelines = safeCall(() -> communityServiceClient.getAllPipelines());
                    List<LakeData> lakes = safeCall(() -> communityServiceClient.getAllLakes());
                    List<TreatmentPlantData> plants = safeCall(() -> communityServiceClient.getAllTreatmentPlants());

                    context.append("Complaint-to-Infrastructure Correlation Analysis:\n\n");

                    if (correlation != null && correlation.getAreaSummaries() != null && !correlation.getAreaSummaries().isEmpty()) {
                        // Build a lookup of infrastructure by locality关键词
                        Map<String, List<String>> infraByKeyword = buildInfraLookup(pipelines, lakes, plants);

                        context.append(String.format("Total complaint areas: %d\n", correlation.getTotalAreas()));
                        context.append(String.format("Total complaints: %d\n", correlation.getTotalComplaints()));
                        if (correlation.getHighPriorityAreas() != null && !correlation.getHighPriorityAreas().isEmpty()) {
                            context.append(String.format("High-priority areas: %s\n", String.join(", ", correlation.getHighPriorityAreas())));
                        }
                        context.append("\n");

                        // Correlate each complaint area with infrastructure
                        for (CorrelationData.AreaSummary area : correlation.getAreaSummaries()) {
                            String areaName = area.getArea();
                            context.append(String.format("Area: %s (%d complaints, %d pending, %d overdue)\n",
                                    areaName, area.getTotalComplaints(), area.getPendingCount(), area.getOverdueCount()));

                            // Match this area against infrastructure
                            List<String> matchedInfra = matchAreaToInfrastructure(areaName, infraByKeyword);
                            if (!matchedInfra.isEmpty()) {
                                context.append(String.format("  Related infrastructure: %s\n", String.join("; ", matchedInfra)));
                            } else {
                                context.append("  No directly matching infrastructure found in platform data\n");
                            }

                            // Status breakdown
                            if (area.getStatusBreakdown() != null) {
                                context.append(String.format("  Status: %s\n", area.getStatusBreakdown()));
                            }
                            if (area.getPriorityBreakdown() != null && !area.getPriorityBreakdown().isEmpty()) {
                                context.append(String.format("  Priority: %s\n", area.getPriorityBreakdown()));
                            }
                            context.append("\n");
                        }

                        // Summary of infrastructure with complaints
                        context.append("Infrastructure Summary:\n");
                        if (pipelines != null) {
                            context.append(String.format("- Pipelines: %d total (%d active, %d under maintenance)\n",
                                    pipelines.size(),
                                    pipelines.stream().filter(p -> "ACTIVE".equals(p.getOperationalStatus())).count(),
                                    pipelines.stream().filter(p -> "UNDER_MAINTENANCE".equals(p.getOperationalStatus())).count()));
                        }
                        if (lakes != null) {
                            context.append(String.format("- Lakes: %d total\n", lakes.size()));
                        }
                        if (plants != null) {
                            context.append(String.format("- Treatment Plants: %d total\n", plants.size()));
                        }
                    } else {
                        context.append("No complaint correlation data available.\n");
                    }
                    dataUsed = true;
                }
                case USER_HELP -> {
                    context.append("The SewageAlert Hyderabad platform offers:\n");
                    context.append("- Report sewage issues (complaints with photos and GPS)\n");
                    context.append("- Track complaint status and resolution\n");
                    context.append("- View upcoming awareness events and register\n");
                    context.append("- Read educational articles about sewage and water management\n");
                    context.append("- Find NGOs working on sanitation in Hyderabad\n");
                    context.append("- Participate in NGO cleanup and awareness drives\n");
                    context.append("- View lakes and infrastructure (STPs, pipelines)\n");
                    context.append("For urgent sewage overflow, call GHMC at 040-2111 1111.\n");
                    dataUsed = false;
                }
                default -> {
                    context.append("General platform context: SewageAlert Hyderabad is a citizen-centric platform "
                            + "for reporting and resolving sewage issues. It includes complaint management, "
                            + "NGO collaboration, awareness events, drives, articles, and community engagement.\n");
                    dataUsed = false;
                }
            }
        } catch (Exception e) {
            log.error("Error building context for intent {}: {}", intent, e.getMessage());
            context.append("Unable to retrieve platform data at this time. ");
            context.append("Please answer based on your general knowledge about the platform.\n");
        }

        return new ContextResult(context.toString(), dataUsed);
    }

    @SuppressWarnings("unchecked")
    private <T> T safeCall(DataFetcher<ApiResponse<T>> fetcher) {
        try {
            ApiResponse<T> response = fetcher.fetch();
            return response != null ? response.getData() : null;
        } catch (Exception e) {
            log.warn("Failed to fetch data: {}", e.getMessage());
            return null;
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "N/A";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }

    @FunctionalInterface
    private interface DataFetcher<T> {
        T fetch() throws Exception;
    }

    public record ContextResult(String context, boolean dataUsed) {}

    /**
     * Build a lookup map of infrastructure keywords to infrastructure descriptions.
     * Used for matching complaint areas to nearby infrastructure.
     */
    private Map<String, List<String>> buildInfraLookup(
            List<PipelineData> pipelines, List<LakeData> lakes, List<TreatmentPlantData> plants) {
        Map<String, List<String>> lookup = new LinkedHashMap<>();

        if (pipelines != null) {
            for (PipelineData p : pipelines) {
                String locality = p.getLocality() != null ? p.getLocality().toLowerCase() : "";
                if (!locality.isBlank()) {
                    String desc = String.format("Pipeline: %s (status: %s, installed %s)",
                            p.getLocality(), p.getOperationalStatus(), p.getInstallationYear());
                    lookup.computeIfAbsent(locality, k -> new ArrayList<>()).add(desc);
                    // Also index individual words for partial matching
                    for (String word : locality.split("[\\s,]+")) {
                        if (word.length() > 3) {
                            lookup.computeIfAbsent(word, k -> new ArrayList<>()).add(desc);
                        }
                    }
                }
            }
        }

        if (lakes != null) {
            for (LakeData l : lakes) {
                String name = l.getName() != null ? l.getName().toLowerCase() : "";
                String location = l.getLocation() != null ? l.getLocation().toLowerCase() : "";
                String desc = String.format("Lake: %s (restoration: %s)",
                        l.getName(), l.getRestorationStatus());
                if (!name.isBlank()) {
                    lookup.computeIfAbsent(name, k -> new ArrayList<>()).add(desc);
                    for (String word : name.split("[\\s,]+")) {
                        if (word.length() > 3) {
                            lookup.computeIfAbsent(word, k -> new ArrayList<>()).add(desc);
                        }
                    }
                }
                if (!location.isBlank()) {
                    lookup.computeIfAbsent(location, k -> new ArrayList<>()).add(desc);
                    for (String word : location.split("[\\s,]+")) {
                        if (word.length() > 3) {
                            lookup.computeIfAbsent(word, k -> new ArrayList<>()).add(desc);
                        }
                    }
                }
            }
        }

        if (plants != null) {
            for (TreatmentPlantData t : plants) {
                String name = t.getName() != null ? t.getName().toLowerCase() : "";
                String location = t.getLocation() != null ? t.getLocation().toLowerCase() : "";
                String desc = String.format("STP: %s at %s (%s MLD, %s)",
                        t.getName(), t.getLocation(), t.getCapacityMld(), t.getTreatmentMethod());
                if (!name.isBlank()) {
                    lookup.computeIfAbsent(name, k -> new ArrayList<>()).add(desc);
                    for (String word : name.split("[\\s,]+")) {
                        if (word.length() > 3) {
                            lookup.computeIfAbsent(word, k -> new ArrayList<>()).add(desc);
                        }
                    }
                }
                if (!location.isBlank()) {
                    lookup.computeIfAbsent(location, k -> new ArrayList<>()).add(desc);
                    for (String word : location.split("[\\s,]+")) {
                        if (word.length() > 3) {
                            lookup.computeIfAbsent(word, k -> new ArrayList<>()).add(desc);
                        }
                    }
                }
            }
        }

        return lookup;
    }

    /**
     * Match a complaint area name against infrastructure keywords.
     * Returns a list of matching infrastructure descriptions.
     */
    private List<String> matchAreaToInfrastructure(String areaName, Map<String, List<String>> infraLookup) {
        if (areaName == null || areaName.isBlank()) return List.of();

        String lowerArea = areaName.toLowerCase();
        Set<String> matched = new LinkedHashSet<>();

        // Direct match on the full area name
        if (infraLookup.containsKey(lowerArea)) {
            matched.addAll(infraLookup.get(lowerArea));
        }

        // Word-level matching
        for (String word : lowerArea.split("[\\s,\\-]+")) {
            if (word.length() > 3 && infraLookup.containsKey(word)) {
                matched.addAll(infraLookup.get(word));
            }
        }

        // Substring matching against all infrastructure keys
        for (Map.Entry<String, List<String>> entry : infraLookup.entrySet()) {
            if (entry.getKey().length() > 3 && lowerArea.contains(entry.getKey())) {
                matched.addAll(entry.getValue());
            }
            if (entry.getKey().length() > 3 && entry.getKey().contains(lowerArea)) {
                matched.addAll(entry.getValue());
            }
        }

        return new ArrayList<>(matched);
    }
}

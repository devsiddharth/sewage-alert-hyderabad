package com.sewagealert.community.service.impl;

import com.sewagealert.community.client.TelanganaStpClient;
import com.sewagealert.community.config.TelanganaArcGisProperties;
import com.sewagealert.community.dto.external.telangana.ArcGisAttributes;
import com.sewagealert.community.dto.external.telangana.ArcGisFeature;
import com.sewagealert.community.dto.external.telangana.ArcGisQueryResponse;
import com.sewagealert.community.dto.external.telangana.StpLocationData;
import com.sewagealert.community.exception.ExternalApiException;
import com.sewagealert.community.service.TreatmentPlantExternalService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * TreatmentPlantExternalServiceImpl: Queries the Telangana government ArcGIS REST service
 * (STP_Locations layer) and maps features into sanitized {@link StpLocationData} objects.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TreatmentPlantExternalServiceImpl implements TreatmentPlantExternalService {

    private final TelanganaStpClient telanganaStpClient;
    private final TelanganaArcGisProperties telanganaArcGisProperties;

    @Override
    public List<StpLocationData> getStpLocations() {
        log.info("Fetching STP locations from the Telangana ArcGIS open data service");
        ArcGisQueryResponse response;
        try {
            response = telanganaStpClient.queryStpLocations("1=1", "*", "pjson", telanganaArcGisProperties.getMaxRecords());
        } catch (FeignException ex) {
            log.error("Telangana ArcGIS request failed — status={}, error={}", ex.status(), ex.getMessage());
            throw new ExternalApiException("Failed to fetch STP data from the Telangana government open data service", ex);
        }

        if (response == null) {
            throw new ExternalApiException("Telangana ArcGIS open data service returned an empty response");
        }
        if (response.getError() != null) {
            log.error("Telangana ArcGIS service returned an error — code={}, message={}",
                    response.getError().getCode(), response.getError().getMessage());
            throw new ExternalApiException("Telangana open data service error: " + response.getError().getMessage());
        }
        if (response.getFeatures() == null) {
            log.warn("Telangana ArcGIS response contained no features");
            return List.of();
        }

        List<StpLocationData> plants = response.getFeatures().stream()
                .map(ArcGisFeature::getAttributes)
                .filter(Objects::nonNull)
                .map(this::toStpLocationData)
                .collect(Collectors.toList());
        log.info("Successfully fetched {} STP locations from the Telangana ArcGIS service", plants.size());
        return plants;
    }

    private StpLocationData toStpLocationData(ArcGisAttributes attributes) {
        StpLocationData data = new StpLocationData();
        data.setName(attributes.getLocationOfStp());
        data.setLatitude(attributes.getLatitude());
        data.setLongitude(attributes.getLongitude());
        data.setYearOfCommissioning(attributes.getYearOfCommissioning());
        data.setOperationalStatus(attributes.getOperationalStatus());
        data.setInstalledCapacityMld(attributes.getInstalledCapacityMld());
        data.setUtilizationCapacityMld(attributes.getUtilizationCapacityMld());
        data.setTechnology(attributes.getTechnology());
        data.setFrequencyOfMonitoring(attributes.getFrequencyOfMonitoring());
        return data;
    }
}

package com.sewagealert.community.service.impl;

import com.sewagealert.community.client.OverpassClient;
import com.sewagealert.community.config.OverpassProperties;
import com.sewagealert.community.dto.external.overpass.LakeAddress;
import com.sewagealert.community.dto.external.overpass.LakeGeoData;
import com.sewagealert.community.dto.external.overpass.OverpassElement;
import com.sewagealert.community.dto.external.overpass.OverpassResponse;
import com.sewagealert.community.exception.LocationServiceException;
import com.sewagealert.community.service.LakeExternalService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * LakeExternalServiceImpl: Queries the OpenStreetMap Overpass API for water bodies inside the
 * configured Hyderabad bounding box and maps them into sanitized {@link LakeGeoData} objects.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LakeExternalServiceImpl implements LakeExternalService {

    private static final String OVERPASS_QUERY_TEMPLATE =
            "[out:json][timeout:%d];" +
            "(node[\"natural\"=\"water\"](%s);" +
            "way[\"natural\"=\"water\"](%s);" +
            "relation[\"natural\"=\"water\"](%s););" +
            "out body center geom;";

    private final OverpassClient overpassClient;
    private final OverpassProperties overpassProperties;

    @Override
    public List<LakeGeoData> getLakes() {
        OverpassProperties.Bounds bbox = overpassProperties.getBbox();
        String bboxString = String.format("%s,%s,%s,%s", bbox.getSouth(), bbox.getWest(), bbox.getNorth(), bbox.getEast());
        String query = String.format(OVERPASS_QUERY_TEMPLATE,
                overpassProperties.getQueryTimeoutSeconds(), bboxString, bboxString, bboxString);
        String encodedBody = "data=" + URLEncoder.encode(query, StandardCharsets.UTF_8);

        log.info("Querying Overpass API for water bodies in bbox ({})", bboxString);
        OverpassResponse response;
        try {
            response = overpassClient.query(encodedBody);
        } catch (FeignException ex) {
            log.error("Overpass API request failed — status={}, error={}", ex.status(), ex.getMessage());
            throw new LocationServiceException("Failed to fetch lake data from the OpenStreetMap Overpass API", ex);
        }

        if (response == null || response.getElements() == null) {
            log.warn("Overpass API returned no elements");
            return List.of();
        }

        List<LakeGeoData> lakes = response.getElements().stream()
                .map(this::toLakeGeoData)
                .filter(Objects::nonNull)
                .limit(overpassProperties.getMaxElements())
                .collect(Collectors.toList());
        log.info("Overpass API returned {} elements; mapped {} named lakes", response.getElements().size(), lakes.size());
        return lakes;
    }

    private LakeGeoData toLakeGeoData(OverpassElement element) {
        Map<String, String> tags = element.getTags();
        if (tags == null || isBlank(tags.get("name"))) {
            return null; // only named water bodies are useful for the map
        }

        LakeGeoData lake = new LakeGeoData();
        lake.setName(tags.get("name"));

        if (element.getLat() != null && element.getLon() != null) {
            lake.setLatitude(element.getLat());
            lake.setLongitude(element.getLon());
        } else if (element.getCenter() != null) {
            lake.setLatitude(element.getCenter().getLat());
            lake.setLongitude(element.getCenter().getLon());
        }

        lake.setGeometry(element.getGeometry());
        lake.setAddress(buildAddress(tags));
        return lake;
    }

    private LakeAddress buildAddress(Map<String, String> tags) {
        LakeAddress address = new LakeAddress();
        address.setStreet(tags.get("addr:street"));
        address.setCity(tags.get("addr:city"));
        address.setPostcode(tags.get("addr:postcode"));
        address.setState(tags.get("addr:state"));
        String formatted = Stream.of(address.getStreet(), address.getCity(), address.getPostcode(), address.getState())
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
        address.setFormatted(formatted.isEmpty() ? null : formatted);
        return address;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

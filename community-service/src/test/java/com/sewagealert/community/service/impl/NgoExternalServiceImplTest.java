package com.sewagealert.community.service.impl;

import com.sewagealert.community.client.GooglePlacesClient;
import com.sewagealert.community.client.OverpassClient;
import com.sewagealert.community.config.GooglePlacesProperties;
import com.sewagealert.community.config.OverpassProperties;
import com.sewagealert.community.dto.external.overpass.OverpassCenter;
import com.sewagealert.community.dto.external.overpass.OverpassElement;
import com.sewagealert.community.dto.external.overpass.OverpassResponse;
import com.sewagealert.community.dto.external.places.GooglePlacesResult;
import com.sewagealert.community.dto.external.places.GooglePlacesSearchResponse;
import com.sewagealert.community.dto.external.places.NgoSearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * NgoExternalServiceImplTest: Verifies the free OpenStreetMap Overpass fallback kicks in
 * when no Google Places API key is configured, and that Google Places is used when the key
 * is present.
 */
class NgoExternalServiceImplTest {

    private GooglePlacesClient googlePlacesClient;
    private GooglePlacesProperties googlePlacesProperties;
    private OverpassClient overpassClient;
    private OverpassProperties overpassProperties;
    private NgoExternalServiceImpl service;

    @BeforeEach
    void setUp() {
        googlePlacesClient = mock(GooglePlacesClient.class);
        googlePlacesProperties = new GooglePlacesProperties();
        overpassClient = mock(OverpassClient.class);
        overpassProperties = new OverpassProperties();
        service = new NgoExternalServiceImpl(googlePlacesClient, googlePlacesProperties,
                overpassClient, overpassProperties);
    }

    @Test
    void fallsBackToOverpassWhenGoogleKeyIsMissing() {
        googlePlacesProperties.setApiKey("");

        OverpassElement element = new OverpassElement();
        element.setLat(17.3850);
        element.setLon(78.4867);
        element.setTags(Map.of(
                "name", "Green Future Foundation",
                "office", "ngo",
                "addr:street", "Road No 12, Banjara Hills",
                "addr:city", "Hyderabad",
                "phone", "+91 40 1234 5678",
                "website", "https://greenfuture.example.org"));

        OverpassResponse response = new OverpassResponse();
        response.setElements(List.of(element));
        when(overpassClient.query(anyString())).thenReturn(response);

        List<NgoSearchResult> results = service.searchNgos("Hyderabad", "Water");

        assertThat(results).hasSize(1);
        NgoSearchResult ngo = results.get(0);
        assertThat(ngo.getName()).isEqualTo("Green Future Foundation");
        assertThat(ngo.getAddress()).contains("Banjara Hills");
        assertThat(ngo.getPhone()).isEqualTo("+91 40 1234 5678");
        assertThat(ngo.getWebsite()).isEqualTo("https://greenfuture.example.org");
        assertThat(ngo.getLatitude()).isEqualTo(17.3850);
        assertThat(ngo.getLongitude()).isEqualTo(78.4867);

        verify(googlePlacesClient, never()).textSearch(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void usesCenterCoordinatesForWaysAndRelations() {
        googlePlacesProperties.setApiKey("");

        OverpassElement way = new OverpassElement();
        OverpassCenter center = new OverpassCenter();
        center.setLat(17.45);
        center.setLon(78.55);
        way.setCenter(center);
        way.setTags(Map.of("name", "WaterAid Hyderabad", "office", "charity"));

        OverpassResponse response = new OverpassResponse();
        response.setElements(List.of(way));
        when(overpassClient.query(anyString())).thenReturn(response);

        List<NgoSearchResult> results = service.searchNgos(null, null);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getLatitude()).isEqualTo(17.45);
        assertThat(results.get(0).getLongitude()).isEqualTo(78.55);
    }

    @Test
    void skipsUnnamedElementsFromOverpass() {
        googlePlacesProperties.setApiKey("");

        OverpassElement unnamed = new OverpassElement();
        unnamed.setLat(17.40);
        unnamed.setLon(78.50);
        unnamed.setTags(Map.of("office", "ngo")); // no name tag

        OverpassResponse response = new OverpassResponse();
        response.setElements(List.of(unnamed));
        when(overpassClient.query(anyString())).thenReturn(response);

        List<NgoSearchResult> results = service.searchNgos(null, null);

        assertThat(results).isEmpty();
    }

    @Test
    void usesGooglePlacesWhenKeyIsConfigured() {
        googlePlacesProperties.setApiKey("test-google-key");
        googlePlacesProperties.setMaxResults(5);

        GooglePlacesResult result = new GooglePlacesResult();
        result.setName("Save Lakes Foundation");
        result.setFormattedAddress("Banjara Hills, Hyderabad");
        GooglePlacesSearchResponse searchResponse = new GooglePlacesSearchResponse();
        searchResponse.setStatus("OK");
        searchResponse.setResults(List.of(result));
        when(googlePlacesClient.textSearch(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(searchResponse);

        List<NgoSearchResult> results = service.searchNgos("Hyderabad", "Water NGO");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Save Lakes Foundation");
        verify(overpassClient, never()).query(anyString());
    }
}

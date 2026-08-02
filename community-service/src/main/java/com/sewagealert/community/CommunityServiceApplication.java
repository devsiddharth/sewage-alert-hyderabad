package com.sewagealert.community;

import com.sewagealert.community.config.GooglePlacesProperties;
import com.sewagealert.community.config.NewsApiProperties;
import com.sewagealert.community.config.OverpassProperties;
import com.sewagealert.community.config.TelanganaArcGisProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableConfigurationProperties({
        NewsApiProperties.class,
        GooglePlacesProperties.class,
        OverpassProperties.class,
        TelanganaArcGisProperties.class
})
// Community Service: Handles all community engagement features — events, articles, NGOs, infrastructure info, and environmental data.
// This is the largest service with 6 sub-modules, each with its own controller, service, model, and repository.
// It also integrates public external APIs (GNews, Google Places, OpenStreetMap Overpass, Telangana ArcGIS) via OpenFeign.
public class CommunityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommunityServiceApplication.class, args);
    }
}

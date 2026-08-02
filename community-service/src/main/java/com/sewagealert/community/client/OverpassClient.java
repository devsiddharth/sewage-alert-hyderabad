package com.sewagealert.community.client;

import com.sewagealert.community.dto.external.overpass.OverpassResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * OverpassClient: Declarative OpenFeign client for the OpenStreetMap Overpass API.
 * <p>
 * Overpass is an external public API — not registered in Eureka — so the base URL comes from
 * {@code community.external.overpass.base-url} configuration and load balancing is not applied.
 * <p>
 * Queries are sent as a form-urlencoded POST body: {@code data=<urlencoded Overpass QL>}.
 * The caller is responsible for URL-encoding the query (see LakeExternalServiceImpl).
 */
@FeignClient(name = "overpass-api", url = "${community.external.overpass.base-url}")
public interface OverpassClient {

    /**
     * query: Executes an Overpass QL query against the interpreter endpoint.
     *
     * @param data form-urlencoded body, i.e. {@code data=<urlencoded query>}
     */
    @PostMapping(value = "/api/interpreter", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    OverpassResponse query(@RequestBody String data);
}

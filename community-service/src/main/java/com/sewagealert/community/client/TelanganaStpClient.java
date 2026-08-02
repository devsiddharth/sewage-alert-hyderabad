package com.sewagealert.community.client;

import com.sewagealert.community.dto.external.telangana.ArcGisQueryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * TelanganaStpClient: Declarative OpenFeign client for the Telangana Government ArcGIS REST service
 * (TGRAC) STP_Locations layer (MapServer/7). A public, key-less open data endpoint.
 * <p>
 * The service is external — not registered in Eureka — so the base URL comes from
 * {@code community.external.telangana-arcgis.base-url} configuration and load balancing is not applied.
 */
@FeignClient(name = "telangana-arcgis", url = "${community.external.telangana-arcgis.base-url}")
public interface TelanganaStpClient {

    /**
     * queryStpLocations: Queries all sewage treatment plant records for the Telangana core urban region.
     *
     * @param where             SQL-like filter, e.g. "1=1" for all records
     * @param outFields         attribute fields to return, e.g. "*" for all
     * @param format            response format, e.g. "pjson" (pretty JSON)
     * @param resultRecordCount maximum number of records to return
     */
    @GetMapping("/arcgis/rest/services/TCUR_Folder/TCUR_Telangana_Core_Urban_Region_V2/MapServer/7/query")
    ArcGisQueryResponse queryStpLocations(@RequestParam("where") String where,
                                          @RequestParam("outFields") String outFields,
                                          @RequestParam("f") String format,
                                          @RequestParam("resultRecordCount") int resultRecordCount);
}

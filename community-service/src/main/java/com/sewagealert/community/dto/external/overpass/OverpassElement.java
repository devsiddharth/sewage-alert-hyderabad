package com.sewagealert.community.dto.external.overpass;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * OverpassElement: A single OSM element (node, way, or relation) returned by Overpass.
 * <ul>
 *   <li>Nodes carry lat/lon directly.</li>
 *   <li>Ways and relations carry a computed {@code center} and a {@code geometry} polygon
 *       (available because the query uses {@code out body center geom}).</li>
 *   <li>{@code tags} holds OSM tags such as name, natural, water, addr:city, addr:postcode.</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
public class OverpassElement {

    private String type;
    private Long id;
    private Double lat;
    private Double lon;
    private OverpassCenter center;
    private OverpassBounds bounds;
    private Map<String, String> tags;
    private List<OverpassGeometryPoint> geometry;
}

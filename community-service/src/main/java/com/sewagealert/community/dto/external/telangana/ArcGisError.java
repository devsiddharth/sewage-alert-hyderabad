package com.sewagealert.community.dto.external.telangana;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * ArcGisError: Error block returned by the ArcGIS REST service for failed queries.
 */
@Getter
@Setter
@NoArgsConstructor
public class ArcGisError {

    private Integer code;
    private String message;
    private List<String> details;
}

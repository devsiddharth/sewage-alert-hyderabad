package com.sewagealert.auth.dto;

import com.sewagealert.auth.model.Role;
import lombok.*;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private Long id;
    private String name;
    private String email;
    private Role role;

}
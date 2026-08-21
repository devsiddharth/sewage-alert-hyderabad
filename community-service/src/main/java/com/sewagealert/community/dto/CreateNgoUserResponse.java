package com.sewagealert.community.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateNgoUserResponse {
    private Long userId;
    private String email;
    private String name;
    private String temporaryPassword;
}

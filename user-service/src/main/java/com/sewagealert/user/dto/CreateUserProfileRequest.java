package com.sewagealert.user.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class CreateUserProfileRequest {

    private Long authUserId;
    private String name;
    private Long phone;
}
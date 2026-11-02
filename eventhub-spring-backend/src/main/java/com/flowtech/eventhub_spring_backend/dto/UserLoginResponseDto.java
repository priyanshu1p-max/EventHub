package com.flowtech.eventhub_spring_backend.dto;

import lombok.Data;

@Data
public class UserLoginResponseDto {

	private String email;
	private String roleName;
	private String token;
}

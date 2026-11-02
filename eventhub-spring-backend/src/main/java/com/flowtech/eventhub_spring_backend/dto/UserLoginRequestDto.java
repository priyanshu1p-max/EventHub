package com.flowtech.eventhub_spring_backend.dto;

import lombok.Data;

@Data
public class UserLoginRequestDto {

	private String email;
	private String password;
}

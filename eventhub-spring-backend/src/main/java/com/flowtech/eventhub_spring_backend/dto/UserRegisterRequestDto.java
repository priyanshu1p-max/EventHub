package com.flowtech.eventhub_spring_backend.dto;

import lombok.Data;

@Data
public class UserRegisterRequestDto {

	private Integer id;
	private String name;
	private String email;
	private String password;
	private Long phone;
	
	private String roleName;
}

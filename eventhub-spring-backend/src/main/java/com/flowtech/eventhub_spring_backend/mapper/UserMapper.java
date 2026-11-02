package com.flowtech.eventhub_spring_backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.flowtech.eventhub_spring_backend.dto.UserRegisterRequestDto;
import com.flowtech.eventhub_spring_backend.entity.User;

@Mapper(componentModel="spring")
public interface UserMapper {
	
	@Mapping(target="role", ignore=true)
	@Mapping(target="createdAt", ignore=true)
	@Mapping(target="id", ignore=true)
	public User toUser(UserRegisterRequestDto userRegisterRequestDto);

}

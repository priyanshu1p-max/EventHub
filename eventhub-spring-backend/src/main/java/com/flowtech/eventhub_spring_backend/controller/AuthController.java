package com.flowtech.eventhub_spring_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flowtech.eventhub_spring_backend.dto.UserLoginRequestDto;
import com.flowtech.eventhub_spring_backend.dto.UserLoginResponseDto;
import com.flowtech.eventhub_spring_backend.dto.UserRegisterRequestDto;
import com.flowtech.eventhub_spring_backend.entity.User;
import com.flowtech.eventhub_spring_backend.repository.UserRepository;
import com.flowtech.eventhub_spring_backend.security.EventJwtCodeGenerator;
import com.flowtech.eventhub_spring_backend.service.UserService;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping(value="/auth")
public class AuthController {
	
	private final UserService userService;
	private final AuthenticationManager authenticationManager;
	private final EventJwtCodeGenerator codeGenerator;
	private final PasswordEncoder passwordEncoder;

	@PostMapping(value="/register")
	public ResponseEntity<?> registerUser(@RequestBody UserRegisterRequestDto requestDTO){
		
		return userService.registerUser(requestDTO);
	}
	
	@PostMapping(value="/login")
	public ResponseEntity<?> loginController(@RequestBody UserLoginRequestDto dto, HttpServletRequest request,
			HttpServletResponse response){
		
		User user=userService.findUserByEmail(dto.getEmail());
		
		
		if(user.getPassword()==null || passwordEncoder.matches(dto.getPassword(),user.getPassword())==false) {
			return ResponseEntity.badRequest().body("password is not set or incorrect");
			
		}
		
		UsernamePasswordAuthenticationToken authenticationToken=new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword());
		
		Authentication authentication=authenticationManager.authenticate(authenticationToken);
		
		String code=codeGenerator.generateJwtCode(authentication);
		
		UserLoginResponseDto userLoginResponseDto =new UserLoginResponseDto();
		userLoginResponseDto.setEmail(dto.getEmail());
		userLoginResponseDto.setRoleName(user.getRole().getName());
		userLoginResponseDto.setToken(code);
				
		
		return ResponseEntity.ok(userLoginResponseDto);
		
	}
}

package com.flowtech.eventhub_spring_backend.service;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.flowtech.eventhub_spring_backend.dto.UserRegisterRequestDto;
import com.flowtech.eventhub_spring_backend.entity.Role;
import com.flowtech.eventhub_spring_backend.entity.User;
import com.flowtech.eventhub_spring_backend.mapper.UserMapper;
import com.flowtech.eventhub_spring_backend.repository.RoleRepository;
import com.flowtech.eventhub_spring_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	
	private final UserRepository userRepository;
	private final RoleRepository roleRepository; 
	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;
	
	public ResponseEntity<?> registerUser(@RequestBody UserRegisterRequestDto requestDTO){
		
		Role role= roleRepository.findByName(requestDTO.getRoleName()).
				orElseThrow(()-> new RuntimeException("Role not found"));
		
		
		User user= userMapper.toUser(requestDTO);
		
		user.setRole(role);
		
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		

if (userRepository.existsByEmail(user.getEmail())) {
    throw new RuntimeException("Email already exists");
}

if (user.getPhone() != null
        && userRepository.existsByPhone(user.getPhone())) {
    throw new RuntimeException("Phone number already exists");
}

		
		User savedUser=userRepository.save(user);
		
		return savedUser !=null?ResponseEntity.ok("User registered success")
				:ResponseEntity.badRequest().body("User registration failed");
	}
	
	public User findUserByEmail(String email) {
		return userRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("User not found with email: " + email));
	}

}

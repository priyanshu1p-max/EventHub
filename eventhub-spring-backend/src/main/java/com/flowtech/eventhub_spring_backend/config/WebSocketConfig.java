package com.flowtech.eventhub_spring_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer{

	 @Override
	    public void configureMessageBroker(MessageBrokerRegistry config) {

	        // Topics used by the backend to broadcast updates.
	        config.enableSimpleBroker("/topic");

	        // Prefix for messages sent from frontend to backend.
	        config.setApplicationDestinationPrefixes("/app");
	    }

	    @Override
	    public void registerStompEndpoints(StompEndpointRegistry registry) {

	        registry.addEndpoint("/ws")
	                .setAllowedOriginPatterns(
	                        "http://localhost:5173"
	                ).withSockJS();
	    }
}

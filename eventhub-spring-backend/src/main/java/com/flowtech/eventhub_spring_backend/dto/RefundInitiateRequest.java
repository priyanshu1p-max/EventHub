package com.flowtech.eventhub_spring_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefundInitiateRequest {

	 @NotBlank(message = "Payment reference is required")
	 private String paymentReference;
}

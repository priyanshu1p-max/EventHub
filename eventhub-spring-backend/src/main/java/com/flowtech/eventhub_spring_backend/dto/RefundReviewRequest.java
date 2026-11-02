package com.flowtech.eventhub_spring_backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RefundReviewRequest {

	@NotNull(message = "Approval decision is required")
    private Boolean approved;

    // Required by service logic when approved is false
    private String rejectionReason;
}

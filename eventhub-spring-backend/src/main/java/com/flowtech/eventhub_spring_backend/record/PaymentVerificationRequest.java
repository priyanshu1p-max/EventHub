package com.flowtech.eventhub_spring_backend.record;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentVerificationRequest(
		
		@NotNull
        Integer bookingId,

        @NotBlank
        String razorpay_order_id,

        @NotBlank
        String razorpay_payment_id,

        @NotBlank
        String razorpay_signature
		) {

}

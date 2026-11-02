package com.flowtech.eventhub_spring_backend.record;

public record PaymentOrderResponse(
		
		Integer bookingId,
        String razorpayOrderId,
        int amount,
        String currency,
        String keyId,
        String eventName
		) {

}

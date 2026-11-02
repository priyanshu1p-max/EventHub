package com.flowtech.eventhub_spring_backend.enums;

public enum RefundStatus {

	REQUEST_RECIEVED,
	ORGANISER_NOTIFIED,
	APPROVED,
    REJECTED,
    REFUND_INITIATED,
    REFUND_COMPLETED
    
    
    // These statuses control the progress of a refund.
    // A rejected refund is a terminal state unless an admin explicitly reopens
    // it through a separate process
}

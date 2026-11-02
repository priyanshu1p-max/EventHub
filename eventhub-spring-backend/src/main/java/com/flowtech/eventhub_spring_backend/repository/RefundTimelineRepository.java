package com.flowtech.eventhub_spring_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flowtech.eventhub_spring_backend.entity.RefundTimeline;

public interface RefundTimelineRepository extends JpaRepository<RefundTimeline, Integer>{

	// Fetch refund history in chronological order
    List<RefundTimeline> findByRefundRequest_RefundIdOrderByCreatedAtAsc(
            Integer refundId);
}

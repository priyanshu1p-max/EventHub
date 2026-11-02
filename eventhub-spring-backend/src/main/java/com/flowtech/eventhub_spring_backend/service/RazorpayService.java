
package com.flowtech.eventhub_spring_backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RazorpayService {

    private final RazorpayClient razorpayClient;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    // Convert rupees into paise
    private int convertToPaise(double amountInRupees) {

        if (!Double.isFinite(amountInRupees) || amountInRupees <= 0) {
            throw new IllegalArgumentException("Invalid payment amount");
        }

        try {
            return BigDecimal.valueOf(amountInRupees)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(0, RoundingMode.UNNECESSARY)
                    .intValueExact();

        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(
                    "Amount must have at most two decimal places", e);
        }
    }

    // Create Razorpay order
    public JSONObject createOrder(double amountInRupees, String receipt) {

        try {
            int amountInPaise = convertToPaise(amountInRupees);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", receipt);

            JSONObject notes = new JSONObject();
            notes.put("receipt", receipt);
            orderRequest.put("notes", notes);

            Order order = razorpayClient.orders.create(orderRequest);

            // Safely extract values from Razorpay response
            Object idValue = order.get("id");
            Object amountValue = order.get("amount");
            Object currencyValue = order.get("currency");

            if (idValue == null || amountValue == null || currencyValue == null) {
                throw new IllegalStateException(
                        "Incomplete response received from Razorpay");
            }

            String orderId = idValue.toString();
            int returnedAmount = Integer.parseInt(amountValue.toString());
            String currency = currencyValue.toString();

            JSONObject response = new JSONObject();
            response.put("orderId", orderId);
            response.put("amount", returnedAmount);
            response.put("currency", currency);
            response.put("keyId", keyId);

            return response;

        } catch (RazorpayException e) {
            throw new RuntimeException("Unable to create Razorpay order", e);
        }
    }

    // Verify Razorpay payment signature
    public boolean verifyPaymentSignature(
            String orderId,
            String paymentId,
            String receivedSignature) {

        if (orderId == null || paymentId == null
                || receivedSignature == null || keySecret == null) {
            return false;
        }

        try {
            String payload = orderId + "|" + paymentId;

            Mac mac = Mac.getInstance("HmacSHA256");

            SecretKeySpec secretKey = new SecretKeySpec(
                    keySecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256");

            mac.init(secretKey);

            byte[] generatedHash = mac.doFinal(
                    payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder generatedSignature = new StringBuilder();

            for (byte b : generatedHash) {
                generatedSignature.append(
                        String.format("%02x", b & 0xff));
            }

            return MessageDigest.isEqual(
                    generatedSignature.toString()
                            .getBytes(StandardCharsets.UTF_8),
                    receivedSignature.getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            throw new RuntimeException(
                    "Payment signature verification failed", e);
        }
    }

    // Check whether Razorpay payment is captured
    public boolean isPaymentCaptured(
            String paymentId,
            String expectedOrderId) {

        try {
            Payment payment = razorpayClient.payments.fetch(paymentId);

            Object statusValue = payment.get("status");
            Object orderIdValue = payment.get("order_id");

            String status = statusValue == null
                    ? ""
                    : statusValue.toString();

            String actualOrderId = orderIdValue == null
                    ? ""
                    : orderIdValue.toString();

            return "captured".equalsIgnoreCase(status)
                    && expectedOrderId != null
                    && expectedOrderId.equals(actualOrderId);

        } catch (RazorpayException e) {
            throw new RuntimeException(
                    "Unable to verify payment with Razorpay", e);
        }
    }
}

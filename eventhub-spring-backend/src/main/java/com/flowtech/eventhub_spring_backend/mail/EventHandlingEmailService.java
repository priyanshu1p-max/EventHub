package com.flowtech.eventhub_spring_backend.mail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.flowtech.eventhub_spring_backend.entity.Booking;
import com.flowtech.eventhub_spring_backend.entity.Event;
import com.flowtech.eventhub_spring_backend.entity.RefundRequest;
import com.flowtech.eventhub_spring_backend.service.TicketService;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventHandlingEmailService {

    private static final Logger log =
            LoggerFactory.getLogger(EventHandlingEmailService.class);

    private final JavaMailSender mailSender;

    @Value("${eventhub.mail.from:}")
    private String fromEmail;

    // Existing simple email method
    public void sendEmail(String to, String subject, String body) {

        if (to == null || to.isBlank()) {
            log.warn("Email not sent because recipient address is missing");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();

            if (fromEmail != null && !fromEmail.isBlank()) {
                message.setFrom(fromEmail);
            }

            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

            log.info("Email sent successfully to {}", to);

        } catch (MailException exception) {
            log.error("Unable to send email to {}", to, exception);
        }
    }

    // NEW: Generate QR image as PNG bytes
    private byte[] generateQrCode(String token)
            throws WriterException, IOException {

        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        BitMatrix bitMatrix = qrCodeWriter.encode(
                token,
                BarcodeFormat.QR_CODE,
                300,
                300
        );

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        MatrixToImageWriter.writeToStream(
                bitMatrix,
                "PNG",
                outputStream
        );

        return outputStream.toByteArray();
    }

    // UPDATED: Booking confirmation with individual QR attachments
    public void sendBookingConfirmation(
            Booking booking,
            List<TicketService.GeneratedTicket> generatedTickets) {

        if (booking == null || booking.getUser() == null
                || booking.getEvent() == null) {
            log.warn("Booking confirmation email skipped: booking details missing");
            return;
        }

        String customerEmail = booking.getUser().getEmail();

        if (customerEmail == null || customerEmail.isBlank()) {
            log.warn("Booking confirmation email skipped: customer email missing");
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    true,
                    "UTF-8"
            );

            if (fromEmail != null && !fromEmail.isBlank()) {
                helper.setFrom(fromEmail);
            }

            helper.setTo(customerEmail);
            helper.setSubject("EventHub - Your Tickets and Booking Confirmation");

            Event event = booking.getEvent();

            StringBuilder body = new StringBuilder();

            body.append("Hello ")
                .append(booking.getUser().getName())
                .append(",\n\n");

            body.append("Your EventHub booking has been created successfully.\n\n");

            body.append("Booking ID: ")
                .append(booking.getBookingId()).append("\n");

            body.append("Event: ")
                .append(event.getName()).append("\n");

            body.append("Event date: ")
                .append(booking.getEventDateTime()).append("\n");

            body.append("Tickets booked: ")
                .append(booking.getQuantity()).append("\n");

            body.append("Total price: ")
                .append(booking.getTotalPrice()).append("\n");

            body.append("Payment status: ")
                .append(booking.getPaymentStatus()).append("\n\n");

            body.append("YOUR INDIVIDUAL EVENT TICKETS\n");
            body.append("--------------------------------\n\n");

            if (generatedTickets != null) {

                for (TicketService.GeneratedTicket ticket : generatedTickets) {

                    body.append("Ticket ID: ")
                        .append(ticket.ticketId())
                        .append("\n");

                    body.append("Your QR code is attached as: ticket_")
                        .append(ticket.ticketId())
                        .append(".png\n\n");

                    byte[] qrImage = generateQrCode(ticket.qrToken());

                    helper.addAttachment(
                            "ticket_" + ticket.ticketId() + ".png",
                            new ByteArrayResource(qrImage),
                            "image/png"
                    );
                }
            }

            body.append("ENTRY INSTRUCTIONS\n");
            body.append("--------------------------------\n");
            body.append("QR scanning opens 1 hour before the event.\n");
            body.append("QR scanning closes 30 minutes after the event starts.\n");
            body.append("Each QR code can be used only once.\n");
            body.append("Please carry your individual QR ticket to the entrance.\n\n");

            body.append("Thank you for using EventHub!");

            helper.setText(body.toString(), false);

            mailSender.send(mimeMessage);

            log.info(
                    "Booking confirmation with {} QR attachments sent to {}",
                    generatedTickets == null ? 0 : generatedTickets.size(),
                    customerEmail
            );

        } catch (
                MailException
                | MessagingException
                | WriterException
                | IOException exception
        ) {
            // Never log the original QR tokens.
            log.error(
                    "Unable to send booking confirmation with QR tickets for booking {}",
                    booking.getBookingId(),
                    exception
            );
        }
    }

    // Existing booking cancellation email
    public void sendBookingCancellation(Booking booking) {

        String customerEmail = booking.getUser().getEmail();

        String subject = "EventHub - Booking Cancelled";

        String body =
                "Hello " + booking.getUser().getName() + ",\n\n"
                + "Your booking has been cancelled successfully.\n\n"
                + "Booking ID: " + booking.getBookingId() + "\n"
                + "Event: " + booking.getEvent().getName() + "\n"
                + "Tickets cancelled: " + booking.getQuantity() + "\n\n"
                + "Thank you for using EventHub.";

        sendEmail(customerEmail, subject, body);
    }

    // Existing event status update email
    public void sendEventStatusUpdate(Event event) {

        if (event.getUser() == null) {
            log.warn("Event status email skipped: event organiser is missing");
            return;
        }

        String organiserEmail = event.getUser().getEmail();

        String subject = "EventHub - Event Status Update";

        String body =
                "Hello " + event.getUser().getName() + ",\n\n"
                + "Your event status has been updated.\n\n"
                + "Event: " + event.getName() + "\n"
                + "Current status: " + event.getStatus() + "\n\n"
                + "Please log in to EventHub to view your event.";

        sendEmail(organiserEmail, subject, body);
    }

    // Existing refund notification email
    public void sendRefundRequestNotification(RefundRequest refund) {

        Booking booking = refund.getBooking();

        if (booking == null || booking.getEvent() == null
                || booking.getEvent().getUser() == null) {
            return;
        }

        String organiserEmail =
                booking.getEvent().getUser().getEmail();

        String subject = "New refund request - Booking #"
                + booking.getBookingId();

        String body =
                "A customer has submitted a refund request.\n\n"
                + "Booking ID: " + booking.getBookingId() + "\n"
                + "Event: " + booking.getEvent().getName() + "\n"
                + "Refund amount: " + refund.getRefundAmount() + "\n"
                + "Reason: " + refund.getReason() + "\n"
                + "Status: " + refund.getStatus();

        sendEmail(organiserEmail, subject, body);
    }
}
package io.email.userservice.service;

public interface EmailService {

    void sendSimpleMessage(String name, String to, String token);
    void sendSimpleMessageWithAttachment(String name, String to, String token);
    void sendMimeMessageWithEmbeddedFiles(String name, String to, String token);

    void sendHtmlEmail(String name, String to, String token);
    void sendHtmlEmailWithEmbeddedFiles(String name, String to, String token);

}

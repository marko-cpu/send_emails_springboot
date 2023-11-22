package io.email.userservice.service.impl;

import io.email.userservice.service.EmailService;
import io.email.userservice.utils.EmailUtils;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.servlet.ServletContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


import java.io.File;
import java.util.Map;

import static io.email.userservice.utils.EmailUtils.getEmailMessage;
import static io.email.userservice.utils.EmailUtils.getVerificationUrl;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    public static final String UTF_8 = "UTF-8";
    public static final String EMAIL_TEMPLATE = "emailtemplate";
    public static final String NEW_USER_ACCOUNT_VERIFICATION = "Account Verification";
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    @Autowired
    private ServletContext servletContext;

    @Value("${spring.mail.verify.host}")
    private String host;
    @Value("${spring.mail.username}")
    private String fromEmail;
    private final JavaMailSender emailSender;

    @Override
    @Async
    public void sendSimpleMessage(String name, String to, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setSubject("New user account verification");
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setText(getEmailMessage(name, host, token));
            emailSender.send(message);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw  new RuntimeException(e.getMessage());
        }

    }

    @Override
    @Async
    public void sendSimpleMessageWithAttachment(String name, String to, String token) {

        try {
            MimeMessage message = getMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8);
            helper.setPriority(1);
            helper.setSubject("New user account verification");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setText(getEmailMessage(name, host, token));
            // Add attachment
            FileSystemResource logo = new FileSystemResource(new File("/home/marko/Desktop/logo.jpg"));
            helper.addAttachment(logo.getFilename(), logo);
            emailSender.send(message);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw  new RuntimeException(e.getMessage());
        }
    }

    @Override
    @Async
    public void sendMimeMessageWithEmbeddedFiles(String name, String to, String token) {

        try {
            MimeMessage message = getMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8);
            helper.setPriority(1);
            helper.setSubject("New user account verification");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setText(getEmailMessage(name, host, token));
            // Add attachment
            FileSystemResource logo = new FileSystemResource(new File("/home/marko/Desktop/logo.jpg"));
            helper.addInline(getContentId(logo.getFilename()), logo);
            emailSender.send(message);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw  new RuntimeException(e.getMessage());
        }
    }


    @Override
    @Async
    public void sendHtmlEmail(String name, String to, String token) {

        try {
            Context context = new Context();
            //context.setVariable("name", name);
            //context.setVariable("url", getVerificationUrl(host, token));
            context.setVariables(Map.of("name", name, "url", getVerificationUrl(host,token)));
            String text = templateEngine.process(EMAIL_TEMPLATE, context);
            MimeMessage message = getMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8);
            helper.setPriority(1);
            helper.setSubject(NEW_USER_ACCOUNT_VERIFICATION);
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setText(text, true);
            // Add attachment
        /*    FileSystemResource logo = new FileSystemResource(new File("/home/marko/Desktop/logo.jpg"));
            helper.addAttachment(logo.getFilename(), logo); */

            emailSender.send(message);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw  new RuntimeException(e.getMessage());
        }


    }

    @Override
    @Async
    public void sendHtmlEmailWithEmbeddedFiles(String name, String to, String token) {


        try {
            MimeMessage message = getMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8);
            helper.setPriority(1);
            helper.setSubject(NEW_USER_ACCOUNT_VERIFICATION);
            helper.setFrom(fromEmail);
            helper.setTo(to);
            Context context = new Context();
            context.setVariables(Map.of("name", name, "url", getVerificationUrl(host,token)));
            String text = templateEngine.process(EMAIL_TEMPLATE, context);


            // Add HTML to the email body
            MimeMultipart mimeMultipart = new MimeMultipart("related");
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(text,"text/html");
            mimeMultipart.addBodyPart(messageBodyPart);

            // Add images to the email body

            addImageToMimeMultipart(mimeMultipart, "src/main/resources/templates/images/Email-Illustration.png", "image1");
            addImageToMimeMultipart(mimeMultipart, "src/main/resources/templates/images/facebook2x.png", "image2");
            addImageToMimeMultipart(mimeMultipart, "src/main/resources/templates/images/instagram2x.png", "image4");
            addImageToMimeMultipart(mimeMultipart, "src/main/resources/templates/images/linkedin2x.png", "image5");
            addImageToMimeMultipart(mimeMultipart, "src/main/resources/templates/images/twitter2x.png", "image6");

            message.setContent(mimeMultipart);

            emailSender.send(message);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw  new RuntimeException(e.getMessage());
        }
    }

    private MimeMessage getMimeMessage() {
        return emailSender.createMimeMessage();
    }

    private String getContentId(String fileName) {
        return "<" + fileName + ">";
    }

    private void addImageToMimeMultipart(MimeMultipart mimeMultipart, String imagePath, String contentId) throws MessagingException {
        MimeBodyPart imageBodyPart = new MimeBodyPart();
        DataSource dataSource = new FileDataSource(imagePath);
        imageBodyPart.setDataHandler(new DataHandler(dataSource));
        imageBodyPart.setHeader("Content-ID", "<" + contentId + ">");
        imageBodyPart.setDisposition(MimeBodyPart.INLINE);
        mimeMultipart.addBodyPart(imageBodyPart);
    }

}

package com.shrimali.modules.shared.services;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.client.url:https://shrimalis.com}")
    private String clientRedirectUrl;

    @Async
    public void sendEmail(String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Test");
        message.setText("Hello");

        mailSender.send(message);
    }

    @Async
    public void sendWelcomeEmail(String email) {

        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("आपका हार्दिक स्वागत है | श्रीमाली समाज");

            String html =
                    "<html><body>" +
                            "<div style='font-family: Noto Sans Devanagari, Arial, sans-serif; " +
                            "font-size:16px; line-height:1.8; color:#000'>" +

                            "<h2 style='font-size:22px; margin-bottom:10px;'>आपका हार्दिक स्वागत है!</h2>" +

                            "<p><b>आदरणीय श्रीमाली बंधु,</b></p>" +

                            "<p>श्रीमाली समाज के इस महत्वपूर्ण प्रयास में आपका हार्दिक स्वागत है। " +
                            "हमें अत्यंत प्रसन्नता है कि आपने इस पंजीकरण प्रक्रिया में सहभागिता की है।</p>" +

                            "<p>हमारा यह प्रयास अपने <b>श्रीमाली समाज</b> की एक समग्र जनगणना करने का है। " +
                            "आप सभी से विनम्र अनुरोध है कि अधिक से अधिक अपने रिश्तेदारों एवं परिचित " +
                            "श्रीमाली बंधुओं को इस पंजीकरण प्रक्रिया से जोड़ने हेतु प्रेरित करें।</p>" +

                            "<p>अगले चरण में जो सदस्य <b>आजीवन सदस्य</b> बनने के इच्छुक हैं, वे निर्धारित शुल्क जमा कर " +
                            "उचित अधिकारी के माध्यम से हमारे <b>पुष्कर समाज</b> में सम्मिलित हो सकते हैं।</p>" +

                            "<p>आपका सहयोग समाज को सशक्त, संगठित एवं प्रगतिशील बनाने में अत्यंत महत्वपूर्ण है।</p>" +

                            "<br/>" +

                            "<p>सादर,<br/>" +
                            "<strong>महेंद्र बोहरा</strong><br/>" +
                            "अध्यक्ष<br/>" +
                            "पुष्कर श्रीमाली समाज</p>" +

                            "</div></body></html>";

            helper.setText(html, true); // true = HTML
            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //    @Async
    public void sendForgotPasswordMail(String email, String token, String code) {
        try {
            String resetLink = clientRedirectUrl + "/auth/reset-password?tid=" + token + "&token=" + code;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Reset Your Password");
            message.setText(
                    "Hello,\n\n" +
                            "We received a request to reset your password.\n\n" +
                            "Click the link below to reset it:\n" +
                            resetLink + "\n\n" +
                            "This link will expire in 15 minutes.\n\n" +
                            "If you did not request a password reset, please ignore this email.\n\n" +
                            "Thanks,\n" +
                            "Your Application Team"
            );

            mailSender.send(message);

        } catch (Exception ex) {
            // NEVER throw from @Async
            log.error("Failed to send forgot password email to {}", email, ex);
        }
    }
}


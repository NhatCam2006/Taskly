package service;

import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;

public class EmailService {
    private static final String EMAIL = "nhatcam2006@gmail.com"; // Thay bằng email thật
    private static final String PASSWORD = "aomv smcl fldw iofu"; // Mật khẩu ứng dụng Gmail

    public static void sendVerificationCode(String recipientEmail, String code) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Mã xác minh đăng ký");
            message.setText("Mã xác minh của bạn là: " + code);

            Transport.send(message);
            System.out.println("📧 Email xác minh đã gửi đến: " + recipientEmail);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}

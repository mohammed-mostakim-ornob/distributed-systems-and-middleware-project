package de.uniba.dsg.helper;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class MailHelper {

    private final Session session;
    private final String username, password, mailFrom;

    public MailHelper(String mailFrom, String host, String port, String username, String password) {
        this.username = username;
        this.password = password;
        this.mailFrom = mailFrom;

        Properties properties = new Properties();

        properties.put("mail.smtp.auth", true);
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.ssl.trust", host);

        session = initialize(properties);
    }

    public void send(String mailTo, String subject, String messageText, String contentType, List<File> attachments) throws IOException, MessagingException {
        Transport.send(generateMessage(mailTo, subject, messageText, contentType, attachments));
    }

    private Session initialize(Properties properties) {
        return Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    private Message generateMessage(String mailTo, String subject, String messageText, String contentType, List<File> attachments) throws IOException, MessagingException {
        Message message = new MimeMessage(session);

        message.setFrom(new InternetAddress(mailFrom));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailTo));
        message.setSubject(subject);

        MimeBodyPart bodyMime = new MimeBodyPart();
        bodyMime.setContent(messageText, contentType);

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(bodyMime);

        addAttachments(multipart, attachments);

        message.setContent(multipart);

        return message;
    }

    private void addAttachments(Multipart multipart, List<File> attachments) throws IOException, MessagingException {
        for (File item: attachments) {
            MimeBodyPart attachmentMime = new MimeBodyPart();
            attachmentMime.attachFile(item);

            multipart.addBodyPart(attachmentMime);
        }
    }
}

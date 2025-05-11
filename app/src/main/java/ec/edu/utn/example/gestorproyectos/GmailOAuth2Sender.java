package ec.edu.utn.example.gestorproyectos;

import android.content.Context;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.sun.mail.smtp.SMTPTransport;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Properties;

public class GmailOAuth2Sender {
    // === 1. OAuth2 credentials (move to secure config!) ===
    private static final String USER_EMAIL    = "pankey.ibarra@gmail.com";
    private static final String CLIENT_ID     = "192282288096-u9r7es2bg7js56q1k3c8i7tncvi0mnq8.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "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";
    private static final String REFRESH_TOKEN = "1//04SAkNR_lk4M6CgYIARAAGAQSNwF-L9Ir86OZRosnySypsTpESNRS9ZBJnNipdz9lN9L_4DOQBxebtBvaZInJF8T5b78ZNXfRN8w";
    private static final String SCOPE         = "https://mail.google.com/";

    /**
     * Public façade: sends a mixed text+HTML email via Gmail XOAUTH2.
     */
    public static void send(
            Context ctx,
            String to,
            String subject,
            String plainBody,
            String texto1,
            String texto2,
            String texto3,
            String random
    ) throws Exception {
        // Build the HTML once here
        String htmlBody   = buildHtml(ctx, texto1, texto2, texto3, random);
        String accessToken = getAccessToken();
        // Call helper with exactly five args
        sendMail(to, subject, plainBody, htmlBody, accessToken);
    }

    // === 2. Obtain OAuth2 access token ===
    private static String getAccessToken() throws IOException {
        NetHttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory         = GsonFactory.getDefaultInstance();
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, CLIENT_ID, CLIENT_SECRET,
                Collections.singletonList(SCOPE)
        ).build();

        Credential credential = flow.createAndStoreCredential(
                new TokenResponse().setRefreshToken(REFRESH_TOKEN),
                "user"
        );
        if (credential.getAccessToken() == null) {
            credential.refreshToken();
        }
        return credential.getAccessToken();
    }

    // === 3. Build & send the message ===
    private static void sendMail(
            String to,
            String subject,
            String plainBody,
            String htmlBody,
            String accessToken
    ) throws MessagingException {
        // SMTP properties for XOAUTH2
        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.auth.mechanisms", "XOAUTH2");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust",       "smtp.gmail.com");
        props.put("mail.smtp.host",            "smtp.gmail.com");
        props.put("mail.smtp.port",            "587");
        props.put("mail.smtp.from",            USER_EMAIL);

        Session session = Session.getInstance(props);
        MimeMessage message = new MimeMessage(session);

        // From:
        try {
            InternetAddress fromAddr = new InternetAddress(
                    USER_EMAIL, "Anthos", StandardCharsets.UTF_8.name()
            );
            message.setFrom(fromAddr);
        } catch (UnsupportedEncodingException e) {
            throw new MessagingException("UTF-8 not supported", e);
        }

        // To, Subject, Headers
        message.setRecipients(MimeMessage.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject, "UTF-8");
        String mailtoUnsub = "<mailto:" + USER_EMAIL + "?subject=unsubscribe>";
        message.setHeader("List-Unsubscribe", mailtoUnsub);
        message.setHeader("List-Unsubscribe-Post", "List-Unsubscribe=One-Click");
        message.setHeader("X-Mailer", "Java/" + System.getProperty("java.version"));
        message.setHeader("X-Priority", "3");
        message.setHeader("Importance", "Normal");

        // multipart/alternative body
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(plainBody, "UTF-8");

        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(htmlBody, "text/html; charset=UTF-8");

        MimeMultipart mp = new MimeMultipart("alternative");
        mp.addBodyPart(textPart);
        mp.addBodyPart(htmlPart);
        message.setContent(mp);

        // Send via SMTPTransport
        try (SMTPTransport transport = (SMTPTransport) session.getTransport("smtp")) {
            transport.connect("smtp.gmail.com", USER_EMAIL, accessToken);
            transport.sendMessage(message, message.getAllRecipients());
            System.out.println("Sent OK – " + transport.getLastServerResponse());
        }
    }

    // === 4. Simple HTML template builder ===
    private static String buildHtml(
            Context ctx,
            String texto1,
            String texto2,
            String texto3,
            String random
    ) {
        // 1) Retrieve the raw template (with %1$s, %2$s, %3$s, %4$s) from strings.xml
        String template = ctx.getString(R.string.email_body);

        // 2) Inject your escaped values into the placeholders
        return String.format(
                template,
                escapeHtml(texto1),   // %1$s
                escapeHtml(texto2),   // %2$s
                escapeHtml(random),   // %3$s
                escapeHtml(texto3)    // %4$s
        );
    }

    // === 5. HTML-escape helper ===
    private static String escapeHtml(String text) {
        return text == null ? "" : text
                .replace("&",  "&amp;")
                .replace("<",  "&lt;")
                .replace(">",  "&gt;")
                .replace("\"", "&quot;")
                .replace("'",  "&#39;");
    }
}

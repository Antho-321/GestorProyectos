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

    private static final String USER_EMAIL    = BuildConfig.USER_EMAIL;
    private static final String CLIENT_ID     = BuildConfig.CLIENT_ID;
    private static final String CLIENT_SECRET = BuildConfig.CLIENT_SECRET;
    private static final String REFRESH_TOKEN = BuildConfig.REFRESH_TOKEN;
    private static final String SCOPE         = BuildConfig.SCOPE;

    /**
     * Public entry point — just call this.
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
        String htmlBody    = buildHtml(ctx, texto1, texto2, texto3, random);
        String accessToken = getAccessToken();
        sendMail(USER_EMAIL, to, subject, plainBody, htmlBody, accessToken);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 1. Obtain OAuth2 access token
    // ──────────────────────────────────────────────────────────────────────────
    private static String getAccessToken() throws IOException {
        NetHttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory        = GsonFactory.getDefaultInstance();

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

    // ──────────────────────────────────────────────────────────────────────────
    // 2. Build & send the message
    // ──────────────────────────────────────────────────────────────────────────
    private static void sendMail(
            String fromEmail,
            String to,
            String subject,
            String plainBody,
            String htmlBody,
            String accessToken
    ) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.auth.mechanisms", "XOAUTH2");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust",       "smtp.gmail.com");
        props.put("mail.smtp.host",            "smtp.gmail.com");
        props.put("mail.smtp.port",            "587");
        props.put("mail.smtp.from",            fromEmail);

        Session session = Session.getInstance(props);
        MimeMessage msg = new MimeMessage(session);

        // From:
        try {
            InternetAddress fromAddr = new InternetAddress(
                    fromEmail, "Anthos", StandardCharsets.UTF_8.name()
            );
            msg.setFrom(fromAddr);
        } catch (UnsupportedEncodingException e) {
            throw new MessagingException("UTF-8 not supported", e);
        }

        // To, Subject, Headers
        msg.setRecipients(MimeMessage.RecipientType.TO, InternetAddress.parse(to));
        msg.setSubject(subject, "UTF-8");
        msg.setHeader("List-Unsubscribe", "<mailto:" + fromEmail + "?subject=unsubscribe>");
        msg.setHeader("List-Unsubscribe-Post", "List-Unsubscribe=One-Click");
        msg.setHeader("X-Mailer", "Java/" + System.getProperty("java.version"));
        msg.setHeader("X-Priority", "3");
        msg.setHeader("Importance", "Normal");

        // multipart/alternative body
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(plainBody, "UTF-8");
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(htmlBody, "text/html; charset=UTF-8");
        MimeMultipart mp = new MimeMultipart("alternative");
        mp.addBodyPart(textPart);
        mp.addBodyPart(htmlPart);
        msg.setContent(mp);

        // Send via SMTPTransport
        try (SMTPTransport transport = (SMTPTransport) session.getTransport("smtp")) {
            transport.connect("smtp.gmail.com", fromEmail, accessToken);
            transport.sendMessage(msg, msg.getAllRecipients());
            System.out.println("Sent OK – " + transport.getLastServerResponse());
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 3. Simple HTML template builder
    // ──────────────────────────────────────────────────────────────────────────
    private static String buildHtml(
            Context ctx,
            String texto1,
            String texto2,
            String texto3,
            String random
    ) {
        String template = ctx.getString(R.string.email_body);
        return String.format(
                template,
                escapeHtml(texto1),   // %1$s
                escapeHtml(texto2),   // %2$s
                escapeHtml(random),   // %3$s
                escapeHtml(texto3)    // %4$s
        );
    }

    /** HTML-escape helper */
    private static String escapeHtml(String text) {
        return text == null ? "" : text
                .replace("&",  "&amp;")
                .replace("<",  "&lt;")
                .replace(">",  "&gt;")
                .replace("\"", "&quot;")
                .replace("'",  "&#39;");
    }
}

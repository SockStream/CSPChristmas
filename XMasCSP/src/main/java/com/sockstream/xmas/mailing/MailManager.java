package com.sockstream.xmas.mailing;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.lang3.StringUtils;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.sockstream.xmas.model.Participant;



public class MailManager {
	private static Logger mLOGGER = LogManager.getLogger(MailManager.class);
	
	private static Gmail _mailService;
	
	public static Gmail getGMailService() throws IOException, MessagingException {
		if (_mailService != null)
		{
			return _mailService;
		}
		
		Credential credential = GoogleManager.getCredentials();
		
		_mailService = new Gmail.Builder(GoogleManager.HTTP_TRANSPORT, GoogleManager.JSON_FACTORY, credential).setApplicationName(GoogleManager.APPLICATION_NAME).build();
		//MimeMessage message = createEmail("clement.beze@gmail.com", "clement.beze@gmail.com", "Titre", "Sujet");
		//_mailService.users().messages().send("clement.beze@gmail.com", createMessageWithEmail(message)).execute();
		return _mailService;
	}
	
	public static MimeMessage createEmail(String to,
            String from,
            String subject,
            String bodyText)
		throws MessagingException {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		
		MimeMessage email = new MimeMessage(session);
		
		email.setFrom(new InternetAddress(from));
		email.addRecipient(javax.mail.Message.RecipientType.TO,
		new InternetAddress(to));
		email.setSubject(subject);
		//email.setText(bodyText);
		email.setContent(bodyText,"text/html; charset=utf-8");
		return email;
	}


	public static Message createMessageWithEmail(MimeMessage emailContent)
            throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }
	
	public void sendTestMailTo(Participant personne) throws MessagingException, IOException {		
		String subject = "mail Test";

		List<String> lines;
		try {
			lines = Files.readAllLines(Paths.get(MailConstantes.BODY_FILENAME));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		@SuppressWarnings("unchecked")
		String body = "";
		for(String line : lines)
		{
			body += line;
		}
		//publishMail(personne.getMail(),subject,body);
		MimeMessage message = createEmail(personne.getMail(), personne.getMail(), subject, body);
		_mailService.users().messages().send(personne.getMail(), createMessageWithEmail(message)).execute();
	}

	public void sendXMasMails(Participant personne, Participant designee) throws MessagingException, IOException {
		String subject = MailConstantes.OBJET;
		
		List<String> lines;
		try {
			lines = Files.readAllLines(Paths.get(MailConstantes.BODY_FILENAME));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		@SuppressWarnings("unchecked")
		String body = "";
		for(String line : lines)
		{
			body += line;
		}
		
		body = body.replaceAll(MailConstantes.USER_NAME, personne.getPrenom());
		
		body = body.replaceAll(MailConstantes.PERSONNE_DESIGNEE, designee.getPrenom() + " " + designee.getNom());

		MimeMessage message = createEmail(personne.getMail(), "secretsantadu62@gmail.com", subject, body);
		_mailService.users().messages().send("me", createMessageWithEmail(message)).execute();
	}
}

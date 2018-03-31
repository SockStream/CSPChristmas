package com.sockstream.xmas.mailing;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.sockstream.xmas.model.Participant;

public class MailManager {
	private static String PASSWORD = "GmailPassword";
	private static String USER_NAME = "GmailLogin"; //without@gmail.com
	
	
	public static void sendTestMailTo(Participant personne) {
		// TODO Auto-generated method stub
		
		String subject = "mail Test";
		String body = "this is a test";
		
		publishMail(personne.getMail(),subject,body);
	}

	public static void sendXMasMails(Participant personne) {
		// TODO Auto-generated method stub
		Participant match = personne.getPreviousMates().get(personne.getPreviousMates().size()-1);
		String subject = "mail subject";
		String body = "mailBody";
		publishMail(personne.getMail(), subject, body);
	}

	private static void publishMail(String destinataire, String subject, String body) {
		Properties properties = System.getProperties();
		String host = "smtp.gmail.com";
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.user", USER_NAME);
		properties.put("mail.smtp.password", PASSWORD);
		properties.put("mail.smtp.port", "587");
		properties.put("mail.smtp.auth", "true");
		
		Session session = Session.getDefaultInstance(properties);
		MimeMessage message = new MimeMessage(session);
		
		try {
			message.setFrom(new InternetAddress(USER_NAME));
			InternetAddress[] toAddress = new InternetAddress[1];

            // To get the array of addresses
            for( int i = 0; i < toAddress.length; i++ ) {
                toAddress[i] = new InternetAddress(destinataire);
            }

            for( int i = 0; i < toAddress.length; i++) {
                message.addRecipient(Message.RecipientType.TO, toAddress[i]);
            }

            message.setSubject(subject);
            message.setText(body);
            Transport transport = session.getTransport("smtp");
            transport.connect(host, USER_NAME, PASSWORD);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        }
        catch (AddressException ae) {
            ae.printStackTrace();
        }
        catch (MessagingException me) {
            me.printStackTrace();
        }
	}
}

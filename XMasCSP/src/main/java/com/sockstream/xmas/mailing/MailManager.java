package com.sockstream.xmas.mailing;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sockstream.xmas.model.Participant;

public class MailManager {
	
	private static String PASSWORD = "lesjoiesducode";
	private static String USER_NAME = "clement.beze";
	private static Logger mLogger = LogManager.getLogger(MailManager.class);

	private MailManager() {
	}
	
	public static void sendTestMailTo(Participant personne) {
		String subject = "mail Test";
		String body = "this is a test";
		publishMail(personne.getMail(),subject,body);
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
            mLogger.error(ae);
        }
        catch (MessagingException me) {
        	mLogger.error(me);
        }
	}

	public static void sendXMasMails(Participant personne) {
		Participant match = personne.getPreviousMates().get(personne.getPreviousMates().size()-1);
		String subject = "Loterie de Noel";
		String body = "    HoHoHo bel enfant !\nAprès une nouvelle année à supporter cette pute de " + match.getPrenom() + " " + match.getNom() + ", et puisque tu as été trop con(conne) popur ne pas t'en débarasser, t'es bon pour lui offrir un cadeau pas trop merdique, disons suffisamment acceptable pour ne pas en avoir honte en lui offrant.\nPour éviter de claquer tout son salaire pour une personne aussi débile, il est de bon ton de ne pas mettre plus de '10-15€' dans le cadeau.\n\nGros Bisous\nPapa Noël <3";
		publishMail(personne.getMail(), subject, body);
	}
}
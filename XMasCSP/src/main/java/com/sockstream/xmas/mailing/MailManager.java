package com.sockstream.xmas.mailing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
	private String mPASSWD;
	private String mUSERNAME; //without@gmail.com
	private static Logger mLOGGER = LogManager.getLogger(MailManager.class);
	
	public MailManager() {
		Properties properties = new Properties();
		InputStream inputStream = null;
		
		try {

			//File file = new File("config.properties");
			//mLOGGER.debug(file.getAbsolutePath());
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
	        inputStream = classLoader.getResourceAsStream("config.properties");

	        // load a properties file
	        properties.load(inputStream);

	        // get the property value and print it out
	        mPASSWD = properties.getProperty("mailPassword");
	        mUSERNAME = properties.getProperty("username");

	    } catch (IOException ex) {
	        mLOGGER.error(ex);
	    } finally {
	        if (inputStream != null) {
	            try {
	            	inputStream.close();
	            } catch (IOException e) {
	            	mLOGGER.error(e);
	            }
	        }
	    }
	}
	
	public void sendTestMailTo(Participant personne) {		
		String subject = "mail Test";
		String body = MailConstantes.BODY;
		
		publishMail(personne.getMail(),subject,body);
	}

	public void sendXMasMails(Participant personne, Participant designee) {
		String subject = MailConstantes.OBJET;
		String body = MailConstantes.BODY;
		body = body.replaceAll(MailConstantes.USER_NAME, personne.getPrenom());
		
		body = body.replaceAll(MailConstantes.PERSONNE_DESIGNEE, designee.getPrenom() + " " + designee.getNom());
		publishMail(personne.getMail(), subject, body);
	}

	private void publishMail(String destinataire, String subject, String body) {
		Properties properties = System.getProperties();
		String host = "smtp.gmail.com";
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.user", mUSERNAME);
		properties.put("mail.smtp.password", mPASSWD);
		properties.put("mail.smtp.port", "587");
		properties.put("mail.smtp.auth", "true");
		
		Session session = Session.getDefaultInstance(properties);
		MimeMessage message = new MimeMessage(session);
		
		try {
			message.setFrom(new InternetAddress(mUSERNAME));
			InternetAddress[] toAddress = new InternetAddress[1];

            // To get the array of addresses
            for( int i = 0; i < toAddress.length; i++ ) {
                toAddress[i] = new InternetAddress(destinataire);
            }

            for( int i = 0; i < toAddress.length; i++) {
                message.addRecipient(Message.RecipientType.TO, toAddress[i]);
            }

            message.setSubject(subject);
            message.setContent(body, "text/html; charset=utf-8");
            //message.setText(body);
            Transport transport = session.getTransport("smtp");
            transport.connect(host, mUSERNAME, mPASSWD);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        }
        catch (AddressException ae) {
            mLOGGER.error(ae);
        }
        catch (MessagingException me) {
            mLOGGER.error(me);
        }
	}
}

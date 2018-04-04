package com.sockstream.xmas.mailing;

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

	        inputStream = new FileInputStream("config.properties");

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
		String body = "this is a test";
		
		publishMail(personne.getMail(),subject,body);
	}

	public void sendXMasMails(Participant personne) {
		String subject = "mail subject";
		String body = "mailBody";
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
            message.setText(body);
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

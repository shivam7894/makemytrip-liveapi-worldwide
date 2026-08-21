package com.mmt.service;
import jakarta.mail.*; import jakarta.mail.internet.*; import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Service; import java.util.Properties;
@Service public class EmailService {
 @Value("${mail.smtp.host:}") String host; @Value("${mail.smtp.port:587}") int port; @Value("${mail.smtp.username:}") String username; @Value("${mail.smtp.password:}") String password; @Value("${mail.smtp.auth:true}") boolean auth; @Value("${mail.smtp.starttls:true}") boolean starttls;
 @Value("${mail.smtp.connectiontimeout:10000}") int connectionTimeout;
 @Value("${mail.smtp.timeout:10000}") int timeout;
 @Value("${mail.smtp.writetimeout:10000}") int writeTimeout;
 public boolean configured(){return host!=null&&!host.isBlank()&&username!=null&&!username.isBlank()&&password!=null&&!password.isBlank();}
 public void sendOtp(String to,String purpose,String otp){
   if(!configured()) return;
   try { Properties p=new Properties(); p.put("mail.smtp.host",host); p.put("mail.smtp.port",String.valueOf(port)); p.put("mail.smtp.auth",String.valueOf(auth)); p.put("mail.smtp.starttls.enable",String.valueOf(starttls));
     p.put("mail.smtp.connectiontimeout",String.valueOf(connectionTimeout));
     p.put("mail.smtp.timeout",String.valueOf(timeout));
     p.put("mail.smtp.writetimeout",String.valueOf(writeTimeout));
     Session s=Session.getInstance(p,new Authenticator(){protected PasswordAuthentication getPasswordAuthentication(){return new PasswordAuthentication(username,password);}});
     Message m=new MimeMessage(s); m.setFrom(new InternetAddress(username,"MMT Travel Security")); m.setRecipients(Message.RecipientType.TO,InternetAddress.parse(to)); m.setSubject("MMT " + purpose + " verification OTP");
     m.setText("Your MakeMyTrip verification code is " + otp + ".\n\nIt expires in 10 minutes. Never share this OTP with anyone.\n\n— MMT Travel Security"); Transport.send(m);
   } catch(Exception e){ throw new RuntimeException("Unable to send OTP email: "+e.getMessage(),e); }
 }
}

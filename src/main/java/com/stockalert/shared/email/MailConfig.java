package com.stockalert.shared.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender(
            @Value("${spring.mail.host:smtp.gmail.com}") String host,
            @Value("${spring.mail.port:587}") int port,
            @Value("${spring.mail.username:}") String username,
            @Value("${spring.mail.password:}") String password,
            @Value("${spring.mail.properties.mail.smtp.auth:true}") boolean auth,
            @Value("${spring.mail.properties.mail.smtp.starttls.enable:true}") boolean starttls
    ) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties properties = mailSender.getJavaMailProperties();
        properties.put("mail.smtp.auth", String.valueOf(auth));
        properties.put("mail.smtp.starttls.enable", String.valueOf(starttls));

        return mailSender;
    }
}

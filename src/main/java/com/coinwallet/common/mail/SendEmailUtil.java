package com.coinwallet.common.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class SendEmailUtil {
    @Autowired
    private JavaMailSender mailSender; //自动注入的Bean

    @Value("${spring.mail.username}")
    private String Sender; //读取配置文件中的参数


    public void sendSimpleMail(String msg)   {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(Sender);
        message.setTo("925936461@qq.com"); //自己给自己发送邮件
        message.setSubject("主题：OCN价格预警");
        message.setText(msg);
        try {
            mailSender.send(message);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

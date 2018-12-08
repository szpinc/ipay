package org.szpinc.pay.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author GhostDog
 */
@Component
public class EmailUtil {

    private static final Logger LOG = LoggerFactory.getLogger(EmailUtil.class);
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private TemplateEngine templateEngine;

    public void sendTemplateMail(String sender, String sendTo, String title, String templateName, Object obj) {
        if (LOG.isInfoEnabled()) {
            LOG.info("开始给[{}]发送邮件" , sendTo);
        }
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(sender);
            helper.setTo(sendTo);
            helper.setSubject(title);

            Context context = new Context();
            context.setVariable("title" , title);
            context.setVariables(StringUtils.beanToMap(obj));
            //获取模板html
            String content = templateEngine.process(templateName, context);
            helper.setText(content, true);
            mailSender.send(message);
            if (LOG.isInfoEnabled()) {
                LOG.info("邮件发送成功");
            }
        } catch (MessagingException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("邮件发送失败");
                LOG.error("程序异常" , e);
            }
        }
    }

    /**
     * 验证邮箱
     *
     * @param email
     * @return
     */
    public static boolean checkEmail(String email) {
        boolean flag = false;
        try {
            String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
            Pattern regex = Pattern.compile(check);
            Matcher matcher = regex.matcher(email);
            flag = matcher.matches();
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

}

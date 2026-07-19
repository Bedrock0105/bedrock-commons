package org.bedrock.common.resource.service.email.impl;

import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bedrock.common.code.util.CollectionUtil;
import org.bedrock.common.code.util.StringUtil;
import org.bedrock.common.resource.model.email.EmailProperties;
import org.bedrock.common.resource.model.email.AttachmentDataSource;
import org.bedrock.common.resource.model.email.EmailResponse;
import org.bedrock.common.resource.service.email.EmailTemplate;
import org.eclipse.angus.mail.util.MailConnectException;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.annotation.Nullable;
import java.util.List;


@Slf4j
@AllArgsConstructor
public class SpringEmailTemplate implements EmailTemplate {

    private final EmailProperties emailProperties;
    private final JavaMailSender javaMailSender;

    @Override
    public EmailResponse send(List<String> to, String subject, String content) {
        return send(to, subject, content, false);
    }

    @Override
    public EmailResponse send(List<String> to, String subject, String content, boolean isHtml) {
        return send(getEmailForm(), to, subject, content, isHtml);
    }

    @Override
    public EmailResponse send(@Nullable String form, List<String> to, String subject, String content) {
        return send(form, to, subject, content, false);
    }

    @Override
    public EmailResponse send(@Nullable String form, List<String> to, String subject, String content, boolean isHtml) {
        return send(form, to, subject, content, isHtml, null, null);
    }

    @Override
    public EmailResponse send(List<String> to, String subject, String content, @Nullable List<String> cc, @Nullable List<String> bcc) {
        return send(getEmailForm(), to, subject, content, cc, bcc);
    }

    @Override
    public EmailResponse send(@Nullable String form, List<String> to, String subject, String content, @Nullable List<String> cc, @Nullable List<String> bcc) {
        return send(form, to, subject, content, false, cc, bcc);
    }

    @Override
    public EmailResponse send(List<String> to, String subject, String content, boolean isHtml, @Nullable List<String> cc, @Nullable List<String> bcc) {
        return send(getEmailForm(), to, subject, content, isHtml, cc, bcc);
    }

    @Override
    public EmailResponse send(@Nullable String form, List<String> to, String subject, String content, boolean isHtml, @Nullable List<String> cc, @Nullable List<String> bcc) {
        return send(form, to, subject, content, isHtml, cc, bcc, null, null);
    }

    @Override
    public EmailResponse send(@Nullable String form, List<String> to, String subject, String content, boolean isHtml, @Nullable List<String> cc, @Nullable List<String> bcc, @Nullable List<AttachmentDataSource> attachments, @Nullable List<AttachmentDataSource> inlineElements) {
        return sendEmailMessage(form, to, subject, content, isHtml, cc, bcc, attachments, inlineElements);
    }

    public EmailResponse sendEmailMessage(@Nullable String form, List<String> to, String subject,
                                          String content, boolean isHtml,
                                          @Nullable List<String> cc, @Nullable List<String> bcc,
                                          @Nullable List<AttachmentDataSource> attachments,
                                          @Nullable List<AttachmentDataSource> inlineElements) {
        if (StringUtil.isBlank(form)) {
            form = getEmailForm();
        }
        try {
            /**
             * 判断是否是普通邮件
             */
            if (CollectionUtil.isEmpty(attachments) && !isHtml) {
                return sendSimpleMail(form, to, subject, content, cc, bcc);
            }
            //创建一个邮件消息
            MimeMessage message = javaMailSender.createMimeMessage();
            // 创建 MimeMessageHelper，指定 boolean multipart 参数为 true
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(form);
            helper.setTo(to.toArray(new String[0]));
            helper.setSubject(subject);
            //邮件内容  true 表示带有附件或html
            helper.setText(content, isHtml);
            //设置抄送
            if (CollectionUtil.isNotEmpty(cc)) {
                helper.setCc(cc.toArray(new String[0]));
            }
            //设置密送
            if (CollectionUtil.isNotEmpty(bcc)) {
                helper.setBcc(bcc.toArray(new String[0]));
            }

            //设置附件
            if (CollectionUtil.isNotEmpty(attachments)) {
                for (AttachmentDataSource attachment : attachments) {
                    attachment.setFileTypeMap(attachment.getFileTypeMap());
                    helper.addAttachment(attachment.getName(), attachment);
                }
            }
            //内联元素
            if (CollectionUtil.isNotEmpty(inlineElements)) {
                for (AttachmentDataSource inlineElement : inlineElements) {
                    inlineElement.setFileTypeMap(helper.getFileTypeMap());
                    helper.addInline(inlineElement.getContentId(), inlineElement);
                }
            }
            javaMailSender.send(message);
        } catch (MailAuthenticationException e) {
            log.error("邮箱配置错误 邮件发送失败", e);
            return new EmailResponse(false, 401, "邮箱账号密码配置错误");
        } catch (MailSendException e) {
            log.error("邮箱配置错误 邮件发送失败", e);
            if (e.getCause() != null && e.getCause() instanceof MailConnectException) {
                return new EmailResponse(false, 400, "邮件服务器连接失败");
            }
            return new EmailResponse(false, 400, e.getMessage());
        } catch (Exception e) {
            log.error("邮件发送失败", e);
            return new EmailResponse(false, 500, e.getMessage());
        }
        return new EmailResponse(true, 200, "发送成功");
    }

    private EmailResponse sendSimpleMail(String form, List<String> to,
                                         String subject, String content,
                                         @Nullable List<String> cc, @Nullable List<String> bcc) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(form);
        message.setTo(to.toArray(new String[0]));
        message.setSubject(subject);
        message.setText(content);
        //设置抄送
        if (CollectionUtil.isNotEmpty(cc)) {
            message.setCc(cc.toArray(new String[0]));
        }
        //设置密送
        if (CollectionUtil.isNotEmpty(bcc)) {
            message.setBcc(bcc.toArray(new String[0]));
        }
        javaMailSender.send(message);
        return new EmailResponse(true, 200, "发送成功");
    }


    private String getEmailForm() {
        return emailProperties.getUsername();
    }
}

package org.bedrock.common.resource.service.email;

import org.bedrock.common.resource.model.email.*;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 邮件发送接口
 */
public interface EmailTemplate {


	/**
	 * @param to      接收人
	 * @param subject 主题
	 * @param content 内容
	 * @return boolean
	 * @Discription 发送邮件
	 */
	EmailResponse send(List<String> to, String subject, String content);


	/**
	 * @param to      接收人
	 * @param subject 主题
	 * @param content 内容
	 * @param isHtml  是否为HTML
	 * @return boolean
	 * @Discription 发送邮件
	 */
	EmailResponse send(List<String> to, String subject, String content, boolean isHtml);


	/**
	 * @param form    发送人
	 * @param to      接收人
	 * @param subject 主题
	 * @param content 内容模板
	 * @return boolean
	 * @Discription 发送邮件
	 */
	EmailResponse send(@Nullable String form, List<String> to, String subject, String content);

	/**
	 * @param form    发送人
	 * @param to      接收人
	 * @param subject 主题
	 * @param content 内容模板
	 * @param isHtml  是否为HTML
	 * @return boolean
	 * @Discription 发送邮件
	 */
	EmailResponse send(@Nullable String form, List<String> to, String subject, String content,
					   boolean isHtml);


	/**
	 * @param emailSend 邮件发送对象
	 * @return boolean
	 * @Discription 发送邮件
	 */
	default EmailResponse send(EmailSend emailSend) {
		return send(emailSend.getForm(), emailSend.getTo(), emailSend.getSubject(), emailSend.getContent(), emailSend.getIsHtml());
	}


	/**
	 * @param to      接收人
	 * @param subject 主题
	 * @param content 内容
	 * @param cc      抄送人
	 * @param bcc     密送人
	 * @return boolean
	 * @Discription 发送邮件
	 */
	EmailResponse send(List<String> to, String subject, String content,
					   @Nullable List<String> cc, @Nullable List<String> bcc);


	/**
	 * @param form    发送人
	 * @param to      接收人
	 * @param subject 主题
	 * @param content 内容模板
	 * @param cc      抄送人
	 * @param bcc     密送人
	 * @return boolean
	 * @Discription 发送邮件
	 */
	EmailResponse send(@Nullable String form, List<String> to, String subject, String content,
					   @Nullable List<String> cc, @Nullable List<String> bcc);


	/**
	 * @param to      接收人
	 * @param subject 主题
	 * @param content 内容
	 * @param isHtml  是否为HTML 内容
	 * @param cc      抄送人
	 * @param bcc     密送人
	 * @return boolean
	 * @Discription 发送邮件
	 */
	EmailResponse send(List<String> to, String subject, String content,
					   boolean isHtml, @Nullable List<String> cc,
					   @Nullable List<String> bcc);


	/**
	 * @param form    发送人
	 * @param to      接收人
	 * @param subject 主题
	 * @param content 内容模板
	 * @param isHtml  是否为HTML 内容
	 * @param cc      抄送人
	 * @param bcc     密送人
	 * @return boolean
	 * @Discription 发送邮件
	 */
	EmailResponse send(@Nullable String form, List<String> to, String subject,
					   String content, boolean isHtml, @Nullable List<String> cc,
					   @Nullable List<String> bcc);


	/**
	 * @param emailSend 邮件发送对象
	 * @return boolean
	 * @Discription 发送邮件
	 */
	default EmailResponse send(EmailSendCCAndBCC emailSend) {
		return send(emailSend.getForm(), emailSend.getTo(), emailSend.getSubject(), emailSend.getContent(), emailSend.getIsHtml(), emailSend.getCc(), emailSend.getBcc());
	}


	/**
	 * @param form        发送人
	 * @param to          接收人
	 * @param subject     主题
	 * @param content     内容模板
	 * @param isHtml      是否为HTML 内容
	 * @param cc          抄送人
	 * @param bcc         密送人
	 * @param attachments 附件
	 * @param inlineElement 内嵌元素
	 * @return boolean
	 * @Discription 发送邮件
	 */
	EmailResponse send(@Nullable String form, List<String> to, String subject,
					   String content, boolean isHtml, @Nullable List<String> cc,
					   @Nullable List<String> bcc, @Nullable List<AttachmentDataSource> attachments,
					   @Nullable List<AttachmentDataSource> inlineElement);

	/**
	 * @param emailSend 邮件发送对象
	 * @return boolean
	 * @Discription 发送邮件
	 */
	default EmailResponse send(EmailSendAttachment emailSend) {
		return send(emailSend.getForm(), emailSend.getTo(), emailSend.getSubject(),
			emailSend.getContent(), emailSend.getIsHtml(),
			emailSend.getCc(), emailSend.getBcc(), emailSend.getAttachments(),
			emailSend.getInlineElement());
	}
}

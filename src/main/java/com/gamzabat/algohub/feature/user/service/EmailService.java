package com.gamzabat.algohub.feature.user.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.gamzabat.algohub.constants.EmailTemplateStrings;
import com.gamzabat.algohub.enums.EmailType;
import com.gamzabat.algohub.exception.MessagingRuntimeException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
	private static final String FROM_ADDRESS = "noreply@algohub.kr";
	private static final String EMAIL_VERIFICATION_CLIENT_ENDPOINT = "https://algohub.kr/sign-up";
	private static final String RESET_PASSWORD_CLIENT_ENDPOINT = "https://algohub.kr/reset-password";
	private final JavaMailSender mailSender;
	private final TemplateEngine templateEngine;

	@Async
	@Retryable(
		retryFor = {MessagingException.class},
		backoff = @org.springframework.retry.annotation.Backoff(delay = 3000)
	)
	public CompletableFuture<Void> sendVerificationMail(String to, String token, EmailType type) {

		sendMail(to, type, getEmailContent(type, token));
		return CompletableFuture.completedFuture(null);
	}

	@Recover
	public CompletableFuture<Void> failedToSendEmail(MessagingRuntimeException e, String to, String token,
		EmailType type) {
		return handleSendingEmailFailed(e, type.getValue(), to);
	}

	private void sendMail(String recipient, EmailType type, String content) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setTo(recipient);
			helper.setFrom(FROM_ADDRESS);
			helper.setSubject(getEmailSubject(type));
			helper.setText(content, true);
			mailSender.send(message);

		} catch (MessagingException e) {
			log.warn("Failed to send email, retry. : {}", e.toString());
			throw new MessagingRuntimeException(e);
		}
	}

	private CompletableFuture<Void> handleSendingEmailFailed(MessagingRuntimeException e, String purpose,
		String email) {
		log.error("Failed to send {} email to {} after retries. Exception: {}", purpose, email, e.getMessage(), e);
		CompletableFuture<Void> failedFuture = new CompletableFuture<>();
		failedFuture.completeExceptionally(e);
		return failedFuture;
	}

	private String getEmailContent(EmailType type, String token) {
		switch (type) {
			case RESET_PASSWORD: {
				Context context = new Context();
				context.setVariable("title", EmailTemplateStrings.RESET_PASSWORD_TITLE);
				context.setVariable("content", EmailTemplateStrings.RESET_PASSWORD_CONTENT);
				context.setVariable("button", EmailTemplateStrings.RESET_PASSWORD_BUTTON);
				context.setVariable("url", RESET_PASSWORD_CLIENT_ENDPOINT + "?token=" + token);
				return templateEngine.process("email-template", context);
			}

			case EMAIL_VALIDATION:
				Context context = new Context();
				context.setVariable("title", EmailTemplateStrings.EMAIL_VALIDATION_TITLE);
				context.setVariable("content", EmailTemplateStrings.EMAIL_VALIDATION_CONTENT);
				context.setVariable("button", EmailTemplateStrings.EMAIL_VALIDATION_BUTTON);
				context.setVariable("url", EMAIL_VERIFICATION_CLIENT_ENDPOINT + "?token=" + token);
				return templateEngine.process("email-template", context);
			default:
				throw new IllegalArgumentException("LOGIC ERROR : Unreachable code block");
		}
	}

	private String getEmailSubject(EmailType type) {
		return switch (type) {
			case RESET_PASSWORD -> EmailTemplateStrings.RESET_PASSWORD_SUBJECT;
			case EMAIL_VALIDATION -> EmailTemplateStrings.EMAIL_VALIDATION_SUBJECT;
			default -> throw new IllegalArgumentException("LOGIC ERROR : Unreachable code block");
		};
	}
}

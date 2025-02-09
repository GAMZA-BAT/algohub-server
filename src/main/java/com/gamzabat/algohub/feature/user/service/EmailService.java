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
	private static final String RESET_PASSWORD_SUBJECT = "[AlgoHub] 비밀번호 찾기";
	//TODO: When the work is completed on the client side, it needs to be replaced with the appropriate URL.
	private static final String RESET_PASSWORD_CLIENT_ENDPOINT = "https://algohub.kr";
	private final JavaMailSender mailSender;
	private final TemplateEngine templateEngine;

	@Async
	@Retryable(
		retryFor = {MessagingException.class},
		backoff = @org.springframework.retry.annotation.Backoff(delay = 3000)
	)
	public CompletableFuture<Void> sendResetPasswordMail(String to, String token) {
		Context context = new Context();
		context.setVariable("verificationUrl", RESET_PASSWORD_CLIENT_ENDPOINT + "?token=" + token);
		String emailContent = templateEngine.process("reset-password", context);
		MimeMessage message = mailSender.createMimeMessage();

		try {

			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			helper.setTo(to);
			helper.setFrom(FROM_ADDRESS);
			helper.setSubject(RESET_PASSWORD_SUBJECT);
			helper.setText(emailContent, true);
			mailSender.send(message);
			return CompletableFuture.completedFuture(null);
		} catch (MessagingException e) {
			log.warn("Failed to send email, retry. : {}", e.toString());
			throw new MessagingRuntimeException(e);
		}
	}

	@Recover
	public CompletableFuture<Void> failedToSendResetPasswordMail(MessagingRuntimeException e, String to, String token) {
		log.error("Failed to send reset password email to {} after retries. Exception: {}", to, e.getMessage(), e);
		CompletableFuture<Void> failedFuture = new CompletableFuture<>();
		failedFuture.completeExceptionally(e);
		return failedFuture;
	}
}

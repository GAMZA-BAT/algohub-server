package com.gamzabat.algohub.feature.user.service;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.gamzabat.algohub.common.redis.RedisService;
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
	private static final String EMAIL_VERIFICATION_SUBJECT = "[AlgoHub] 이메일 인증번호";
	private static final String EMAIL_VERIFICATION_CLIENT_ENDPOINT = "https://algohub.kr/sign-up";
	private static final String RESET_PASSWORD_SUBJECT = "[AlgoHub] 비밀번호 찾기";
	private static final String RESET_PASSWORD_CLIENT_ENDPOINT = "https://algohub.kr/reset-password";
	private final JavaMailSender mailSender;
	private final TemplateEngine templateEngine;
	private final RedisService redisService;

	@Async
	@Retryable(
		retryFor = {MessagingException.class},
		backoff = @org.springframework.retry.annotation.Backoff(delay = 3000)
	)
	public CompletableFuture<Void> sendResetPasswordMail(String to, String token) {
		Context context = new Context();
		context.setVariable("resetUrl", RESET_PASSWORD_CLIENT_ENDPOINT + "?token=" + token);
		String emailContent = templateEngine.process("reset-password", context);

		sendEmail(to, RESET_PASSWORD_SUBJECT, emailContent);
		return CompletableFuture.completedFuture(null);
	}

	@Recover
	public CompletableFuture<Void> failedToSendResetPasswordMail(MessagingRuntimeException e, String to, String token) {
		return handleSendingEmailFailed(e, "reset password", to);
	}

	@Async
	@Retryable(
		retryFor = {MessagingException.class},
		backoff = @org.springframework.retry.annotation.Backoff(delay = 3000)
	)
	public CompletableFuture<Void> sendVerificationCode(String email) {
		String token = UserService.generateSecureToken();

		redisService.setValues(token, email, Duration.ofMinutes(3));
		log.info(redisService.getValues(token));
		Context context = new Context();
		context.setVariable("verificationUrl", EMAIL_VERIFICATION_CLIENT_ENDPOINT + "?token=" + token);
		String emailContent = templateEngine.process("verification-code", context);

		sendEmail(email, EMAIL_VERIFICATION_SUBJECT, emailContent);
		return CompletableFuture.completedFuture(null);

	}

	@Recover
	public CompletableFuture<Void> failedToSendVerificationEmail(MessagingRuntimeException e, String email) {
		return handleSendingEmailFailed(e, "verification", email);
	}

	private void sendEmail(String recipient, String subject, String content) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setTo(recipient);
			helper.setFrom(FROM_ADDRESS);
			helper.setSubject(subject);
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
}

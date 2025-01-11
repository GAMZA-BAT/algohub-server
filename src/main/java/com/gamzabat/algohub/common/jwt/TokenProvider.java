package com.gamzabat.algohub.common.jwt;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.amazonaws.util.StringUtils;
import com.gamzabat.algohub.common.jwt.domain.RefreshToken;
import com.gamzabat.algohub.common.jwt.dto.JwtDTO;
import com.gamzabat.algohub.common.jwt.exception.ExpiredTokenException;
import com.gamzabat.algohub.common.jwt.exception.TokenException;
import com.gamzabat.algohub.common.jwt.repository.RefreshTokenRepository;
import com.gamzabat.algohub.common.redis.RedisService;
import com.gamzabat.algohub.exception.JwtRequestException;
import com.gamzabat.algohub.feature.group.studygroup.exception.CannotFoundUserException;
import com.gamzabat.algohub.feature.user.dto.TokenResponse;
import com.gamzabat.algohub.feature.user.repository.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Getter
public class TokenProvider {
	private final Key accessTokenKey;
	private final Key refreshTokenKey;
	private final RedisService redisService;
	private final RefreshTokenRepository refreshTokenRepository;
	private final UserRepository userRepository;
	@Value("${jwt_expiration_time}")
	private long accessTokenExpirationTime;
	@Value("${refresh_token_expiration_time}")
	private long refreshTokenExpirationTime;

	public TokenProvider(@Value("${jwt_secret_key}") String accessTokenKey,
		@Value("${jwt_refresh_secret_key}") String refreshTokenKey,
		RedisService redisService, RefreshTokenRepository refreshTokenRepository,
		UserRepository userRepository) {
		byte[] accessKeyBytes = Decoders.BASE64URL.decode(accessTokenKey);
		byte[] refreshKeyBytes = Decoders.BASE64URL.decode(refreshTokenKey);
		this.accessTokenKey = Keys.hmacShaKeyFor(accessKeyBytes);
		this.refreshTokenKey = Keys.hmacShaKeyFor(refreshKeyBytes);
		this.redisService = redisService;
		this.refreshTokenRepository = refreshTokenRepository;
		this.userRepository = userRepository;
	}

	public JwtDTO generateTokens(Authentication authentication) {
		String loginId = UUID.randomUUID().toString();
		return JwtDTO.builder()
			.grantType("Bearer")
			.accessToken(generateAccessToken(loginId, authentication))
			.refreshToken(generateRefreshToken(loginId, authentication))
			.build();
	}

	public String generateAccessToken(String loginId, Authentication authentication) {
		String authorities = authentication.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.joining(","));

		long now = (new Date().getTime());
		Date tokenExpireDate = new Date(now + this.accessTokenExpirationTime);
		return createNewAccessToken(authentication.getName(), authorities, loginId, tokenExpireDate);
	}

	public String generateRefreshToken(String loginId, Authentication authentication) {
		long now = (new Date().getTime());
		Date expirationTime = new Date(now + this.refreshTokenExpirationTime);
		String refreshToken = createNewRefreshToken(authentication.getName());

		com.gamzabat.algohub.feature.user.domain.User user = userRepository.findByEmail(authentication.getName())
			.orElseThrow(() -> new CannotFoundUserException(HttpStatus.NOT_FOUND.value(), "존재하지 않는 유저입니다."));

		refreshTokenRepository.save(
			RefreshToken.builder()
				.refreshToken(refreshToken)
				.user(user)
				.loginId(loginId)
				.expirationDateTime(expirationTime)
				.build()
		);
		return refreshToken;
	}

	public Authentication getAuthentication(String token) {
		Claims claims = parseClaims(token);
		if (claims.get("auth") == null)
			throw new JwtRequestException(HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST", "권한 정보가 비어있습니다.");

		Collection<? extends GrantedAuthority> authorities = Arrays.stream(claims.get("auth").toString().split(","))
			.map(SimpleGrantedAuthority::new)
			.toList();

		UserDetails principal = new User(claims.getSubject(), "", authorities);
		return new UsernamePasswordAuthenticationToken(principal, "", authorities);
	}

	public boolean validateToken(String token) {
		try {
			if (logout(token))
				throw new JwtRequestException(HttpStatus.FORBIDDEN.value(), "FORBIDDEN", "로그아웃 된 토큰입니다.");
			Jwts.parserBuilder()
				.setSigningKey(accessTokenKey)
				.build().parseClaimsJws(token);
			return true;
		} catch (SecurityException | MalformedJwtException e) {
			throw new JwtRequestException(HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST", "검증되지 않은 토큰입니다.");
		} catch (ExpiredJwtException e) {
			throw new JwtRequestException(HttpStatus.UNAUTHORIZED.value(), "UNAUTHORIZED", "만료된 토큰 입니다.");
		} catch (UnsupportedJwtException e) {
			throw new JwtRequestException(HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST", "지원하지 않는 형태의 토큰입니다.");
		} catch (IllegalArgumentException e) {
			throw new JwtRequestException(HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST", "토큰이 비어있습니다.");
		}
	}

	private Claims parseClaims(String token) {
		try {
			return Jwts.parserBuilder()
				.setSigningKey(accessTokenKey)
				.build()
				.parseClaimsJws(token)
				.getBody();
		} catch (ExpiredJwtException e) {
			return e.getClaims();
		}
	}

	public String getUserEmail(String authToken) {
		return getClaims(authToken).getSubject();
	}

	public String resolveToken(HttpServletRequest request) {
		String token = request.getHeader("Authorization");
		if (StringUtils.hasValue(token) && token.startsWith("Bearer"))
			return token.substring(7);
		throw new JwtRequestException(HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST", "유효한 형태의 토큰이 존재하지 않습니다.");
	}

	private Claims getClaims(String expiredToken) {
		String token = expiredToken.replace("Bearer", "").trim();
		return parseClaims(token);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = ExpiredTokenException.class)
	public TokenResponse reissueTokens(String expiredToken, String inputRefreshToken) {
		Claims claims = getClaims(expiredToken);
		String subject = claims.getSubject();
		com.gamzabat.algohub.feature.user.domain.User user = userRepository.findByEmail(subject)
			.orElseThrow(() -> new CannotFoundUserException(HttpStatus.NOT_FOUND.value(), "존재하지 않는 유저입니다."));

		String loginId = (String)claims.get("loginId");
		RefreshToken refreshToken = refreshTokenRepository.findByLoginIdAndUser(loginId, user)
			.orElseThrow(() -> new TokenException(HttpStatus.UNAUTHORIZED.value(), "유효하지 않은 리프레시 토큰입니다. 재로그인이 필요합니다."));

		validateTokenPair(inputRefreshToken, loginId, refreshToken);

		long now = (new Date().getTime());
		Date accessTokenExpireDate = new Date(now + this.accessTokenExpirationTime);
		String newAccessToken = createNewAccessToken(
			subject, claims.get("auth").toString(), loginId, accessTokenExpireDate
		);

		Date refreshTokenExpireDate = new Date(now + this.refreshTokenExpirationTime);
		String newRefreshToken = createNewRefreshToken(subject);
		refreshToken.updateRefreshToken(newRefreshToken, refreshTokenExpireDate);

		return new TokenResponse(newAccessToken, newRefreshToken);
	}

	private void validateTokenPair(String inputRefreshToken, String loginId, RefreshToken refreshToken) {
		if (!loginId.equals(refreshToken.getLoginId()) || !inputRefreshToken.equals(refreshToken.getRefreshToken())) {
			throw new TokenException(HttpStatus.FORBIDDEN.value(), "토큰의 로그인 정보가 일치하지 않습니다.");
		}

		if (refreshToken.getExpirationDateTime().before(new Date())) {
			refreshTokenRepository.delete(refreshToken);
			log.info("success to delete refresh token");
			throw new ExpiredTokenException(HttpStatus.UNAUTHORIZED.value(), "리프레시 토큰의 유효기간이 만료되었습니다. 재로그인이 필요합니다.");
		}
	}

	private String createNewAccessToken(String subject, String authorities, String loginId, Date expirationDateTime) {
		return Jwts.builder()
			.setSubject(subject)
			.setIssuedAt(new Date())
			.claim("auth", authorities)
			.claim("loginId", loginId)
			.setExpiration(expirationDateTime)
			.signWith(accessTokenKey, SignatureAlgorithm.HS256)
			.compact();
	}

	private String createNewRefreshToken(String subject) {
		return Jwts.builder()
			.setSubject(subject)
			.setIssuedAt(new Date())
			.signWith(refreshTokenKey, SignatureAlgorithm.HS256)
			.compact();
	}

	private boolean logout(String token) {
		return redisService.getValues(token).equals("logout");
	}

	@Transactional
	@Scheduled(cron = "0 0 0 * * *")
	public void clearExpiredRefreshTokens() {
		refreshTokenRepository.deleteExpiredRefreshTokens();
	}
}
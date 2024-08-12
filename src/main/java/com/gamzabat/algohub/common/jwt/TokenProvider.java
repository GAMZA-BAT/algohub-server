package com.gamzabat.algohub.common.jwt;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import com.amazonaws.util.StringUtils;
import com.gamzabat.algohub.common.redis.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.gamzabat.algohub.common.jwt.dto.JwtDTO;
import com.gamzabat.algohub.exception.JwtRequestException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Getter
public class TokenProvider {
	private final Key key;
	@Value("${jwt_expiration_time}")
	private long tokenExpiration;
	private final RedisService redisService;

	public TokenProvider(@Value("${jwt_secret_key}") String secretKey, RedisService redisService) {
		byte[] keyBytes = Decoders.BASE64URL.decode(secretKey);
		this.key = Keys.hmacShaKeyFor(keyBytes);
		this.redisService = redisService;
	}

	public JwtDTO generateToken(Authentication authentication){
		String authorities = authentication.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.joining(","));

		long now = (new Date().getTime());

		Date tokenExpireDate = new Date(now + this.tokenExpiration);
		String token = Jwts.builder()
			.setSubject(authentication.getName())
			.claim("auth", authorities)
			.setExpiration(tokenExpireDate)
			.signWith(key, SignatureAlgorithm.HS256)
			.compact();

		return JwtDTO.builder()
			.grantType("Bearer")
			.token(token)
			.build();
	}

	public Authentication getAuthentication(String token){
		Claims claims = parseClaims(token);
		if (claims.get("auth") == null)
			throw new JwtRequestException(HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST", "권한 정보가 비어있습니다.");

		Collection<? extends GrantedAuthority> authorities = Arrays.stream(claims.get("auth").toString().split(","))
			.map(SimpleGrantedAuthority::new)
			.toList();

		UserDetails principal = new User(claims.getSubject(), "", authorities);
		return new UsernamePasswordAuthenticationToken(principal, "",authorities);
	}

	public boolean validateToken(String token) {
		try{
			if (logout(token))
				throw new JwtRequestException(HttpStatus.UNAUTHORIZED.value(),"UNAUTHORIZED","로그아웃 되었습니다.");
			Jwts.parserBuilder()
				.setSigningKey(key)
				.build().parseClaimsJws(token);
			return true;
		} catch (SecurityException | MalformedJwtException e){
			throw new JwtRequestException(HttpStatus.UNAUTHORIZED.value(),"UNAUTHORIZED","검증되지 않은 토큰입니다.");
		} catch (ExpiredJwtException e){
			throw new JwtRequestException(HttpStatus.UNAUTHORIZED.value(),"UNAUTHORIZED","만료된 토큰 입니다.");
		} catch (UnsupportedJwtException e){
			throw new JwtRequestException(HttpStatus.BAD_REQUEST.value(),"BAD_REQUEST","지원하지 않는 형태의 토큰입니다.");
		} catch (IllegalArgumentException e){
			throw new JwtRequestException(HttpStatus.BAD_REQUEST.value(),"BAD_REQUEST","토큰이 비어있습니다.");
		}
	}

	private Claims parseClaims(String token){
		return Jwts.parserBuilder()
			.setSigningKey(key)
			.build()
			.parseClaimsJws(token)
			.getBody();
	}

	public String getUserEmail(String authToken){
		String token = authToken.replace("Bearer","").trim();
		Jws<Claims> claimsJws = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
		return claimsJws.getBody().getSubject();
	}

	public String resolveToken(HttpServletRequest request){
		String token = request.getHeader("Authorization");
		if (StringUtils.hasValue(token) && token.startsWith("Bearer"))
			return token.substring(7);
		return null;
	}

	private boolean logout(String token) {
		return redisService.getValues(token).equals("logout");
	}
}
package com.gamzabat.algohub.common.annotation;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.gamzabat.algohub.common.jwt.TokenProvider;
import com.gamzabat.algohub.exception.UserValidationException;
import com.gamzabat.algohub.feature.user.domain.User;
import com.gamzabat.algohub.feature.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthedUserResolver implements HandlerMethodArgumentResolver {
	private final UserRepository userRepository;
	private final TokenProvider tokenProvider;

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(AuthedUser.class)
			&& User.class.isAssignableFrom(parameter.getParameterType());
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		String jwt = webRequest.getHeader("Authorization");
		if (jwt != null)
			return userRepository.findByEmail(tokenProvider.getUserEmail(jwt))
				.orElseThrow(() -> new UserValidationException("없는 사용자 입니다."));
		else
			throw new UserValidationException("로그인 되지 않았습니다.");
	}
}

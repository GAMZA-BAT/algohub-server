package com.gamzabat.algohub.common.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnumValidator implements ConstraintValidator<ValidEnum, String> {
	private Class<? extends Enum<?>> enumClass;

	@Override
	public void initialize(ValidEnum constraintAnnotation) {
		this.enumClass = constraintAnnotation.enumClass();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
		Object[] enumValues = this.enumClass.getEnumConstants();
		if (enumValues != null) {
			for (Object enumValue : enumValues) {
				if (value.equals(enumValue.toString())) {
					return true;
				}
			}
		}
		return false;
	}
}

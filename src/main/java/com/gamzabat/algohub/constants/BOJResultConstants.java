package com.gamzabat.algohub.constants;

public class BOJResultConstants {
	public static final String CORRECT = "맞았습니다!!";
	public static final String COMPILE_ERROR = "컴파일 에러";
	public static final String RUNTIME_ERROR = "런타임 에러";
	public static final String NOT_CORRECT = "틀렸습니다";
	public static final String TIME_OVER = "시간 초과";
	public static final String MEMORY_OVER = "메모리 초과";
	public static final String OVER_OUTPUT_LIMIT = "출력 초과";
	public static final String WRONG_OUTPUT_FORMAT = "출력 형식이 잘못되었습니다";

	private BOJResultConstants() {
		throw new RuntimeException("Can not instantiate : BOJResultConstants");
	}
}

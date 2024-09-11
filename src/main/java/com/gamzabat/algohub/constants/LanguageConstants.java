package com.gamzabat.algohub.constants;

public class LanguageConstants {
	public static final String[] C_BOUNDARY = {
		"C99", "C11", "C90", "C2x",
		"C99 (Clang)", "C11 (Clang)", "C90 (Clang)", "C2x (Clang)"
	};
	public static final String[] CPP_BOUNDARY = {
		"C++11", "C++14", "C++17", "C++20", "C++98",
		"C++11 (Clang)", "C++14 (Clang)", "C++17 (Clang)", "C++20 (Clang)", "C++98 (Clang)"
	};
	public static final String[] JAVA_BOUNDARY = {
		"Java 8", "Java 11", "Java 8 (OpenJDK)"
	};
	public static final String[] PYTHON_BOUNDARY = {
		"Python 3", "PyPy3"
	};
	public static final String[] RUST_BOUNDARY = {
		"Rust 2015", "Rust 2018", "Rust 2021"
	};

	private LanguageConstants() {
		throw new IllegalStateException("Utility class");
	}
}

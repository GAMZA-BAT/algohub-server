package com.gamzabat.algohub.constants;

public class LanguageConstants {
	public static final String[] C_BOUNDARY = {
		"C99", "C11", "C90", "Objective-C",
		"C99 (Clang)", "C11 (Clang)", "C90 (Clang)"
	};
	public static final String[] CPP_BOUNDARY = {
		"C++11", "C++14", "C++17", "C++20", "C++26", "C++98", "Objective-C++",
		"C++11 (Clang)", "C++14 (Clang)", "C++17 (Clang)", "C++20 (Clang)", "C++98 (Clang)"
	};
	public static final String[] JAVA_BOUNDARY = {
		"Java 8", "Java 11", "Java 15", "Java 8 (OpenJDK)"
	};
	public static final String[] PYTHON_BOUNDARY = {
		"Python 3", "PyPy3"
	};
	public static final String[] RUST_BOUNDARY = {
		"Rust 2015", "Rust 2018", "Rust 2021"
	};
	public static final String[] SCRIPT_BOUNDARY = {
		"Ruby", "Python 3", "PyPy3", "Bash", "Lua", "Perl", "sed", "awk",
		"Tcl", "Rhino", "Pike", "PHP", "Text"
	};
	public static final String[] SYSTEM_BOUNDARY = {
		"C99", "C99 (Clang)", "C11", "C11 (Clang)", "C90", "C90 (Clang)",
		"C2x", "C2x (Clang)", "C#", "Rust 2018", "Rust 2015", "Rust 2021",
		"Go", "Go(gccgo)", "D", "D(LDC)", "Pascal"
	};
	public static final String[] WEBAPP_BOUNDARY = {
		"Java 11", "Java 8 (OpenJDK)", "Java 8", "Java 15", "Kotlin (JVM)",
		"Swift", "node.js", "TypeScript"
	};
	public static final String[] FUNCTION_BOUNDARY = {
		"Scheme", "OCaml", "F#", "Haxe", "Ada"
	};
	public static final String[] LOW_BOUNDARY = {
		"Assembly (32bit)", "Assembly (64bit)", "Fortran",
		"FreeBASIC", "Visual Basic"
	};
	public static final String[] OTHER_BOUNDARY = {
		"Brainf**k", "Whitespace", "Golfscript", "INTERCAL", "Algol 68",
		"Befunge", "아희", "SystemVerilog", "bc"
	};

	public static final String C = "C";
	public static final String CPP = "C++";
	public static final String PYTHON = "Python";
	public static final String JAVA = "Java";
	public static final String RUST = "Rust";
	public static final String SCRIPT = "Script Language";
	public static final String SYSTEM = "System Language";
	public static final String WEBAPP = "Web/App Language";
	public static final String FUNCTION = "Functional Language";
	public static final String LOW = "Low-Level Language";
	public static final String OTHER = "Others";

	private LanguageConstants() {
		throw new RuntimeException("Can not instantiate : LanguageConstants");
	}
}

package com.gamzabat.algohub.common.logging;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.apache.coyote.BadRequestException;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.Getter;

@Getter
public class RequestBodyWrapper extends HttpServletRequestWrapper {
	private final String body;

	public RequestBodyWrapper(HttpServletRequest request) throws BadRequestException {
		super(request);

		if (isMultipartRequest(request)) {
			body = null; //  cannot read multipart request body
			return;
		}

		StringBuilder builder = new StringBuilder();

		try (BufferedReader reader = request.getReader()) {
			char[] buffer = new char[1024];
			int read;
			while ((read = reader.read(buffer)) != -1) {
				builder.append(buffer, 0, read);
			}
		} catch (IOException e) {
			throw new BadRequestException("Failed to read request body", e);
		}

		body = builder.toString();
	}

	private boolean isMultipartRequest(HttpServletRequest request) {
		String contentType = request.getContentType();
		return contentType != null && contentType.startsWith("multipart/form-data");
	}

	@Override
	public ServletInputStream getInputStream() {
		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
			body != null ? body.getBytes(StandardCharsets.UTF_8) : new byte[0]);
		return new ServletInputStream() {
			private boolean finished = false;

			@Override
			public boolean isFinished() {
				return finished;
			}

			@Override
			public boolean isReady() {
				return true;
			}

			@Override
			public void setReadListener(ReadListener readListener) {
				throw new UnsupportedOperationException("ReadListener is not supported");
			}

			@Override
			public int read() {
				int data = byteArrayInputStream.read();
				if (data == -1) {
					finished = true;
				}
				return data;
			}
		};
	}

	@Override
	public BufferedReader getReader() {
		return new BufferedReader(new InputStreamReader(this.getInputStream(), StandardCharsets.UTF_8));
	}
}

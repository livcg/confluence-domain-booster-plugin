package com.bugsio.confluence.plugins;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class PageCapturingResponseWrapper extends HttpServletResponseWrapper {
	
	private CharArrayWriter charWriter;
	private PrintWriter writer;
	private boolean getOutputStreamCalled;
	private boolean getWriterCalled;

	public PageCapturingResponseWrapper(HttpServletResponse response) {
		super(response);

		charWriter = new CharArrayWriter();
	}

	protected boolean isContentCaptured() {
		return writer != null;
	}
	
	public ServletOutputStream getOutputStream() throws IOException {
		if (getWriterCalled) {
			throw new IllegalStateException("getWriter already called");
		}

		getOutputStreamCalled = true;
		return super.getOutputStream();
	}

	public PrintWriter getWriter() throws IOException {
		if (writer != null) {
			return writer;
		}
		if (getOutputStreamCalled) {
			throw new IllegalStateException("getOutputStream already called");
		}
		getWriterCalled = true;
		writer = new PrintWriter(charWriter);
		return writer;
	}

	public String toString() {
		String s = null;

		if (writer != null) {
			s = charWriter.toString();
		}
		return s;
	}
}

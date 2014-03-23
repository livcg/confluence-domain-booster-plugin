package com.bugsio.confluence.plugins;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DomainBoosterResponseWrapper extends HttpServletResponseWrapper {
	private static final Logger log = LoggerFactory.getLogger(DomainBoosterResponseWrapper.class);

	public DomainBoosterResponseWrapper(HttpServletResponse response) {
		super(response);
	}

	@Override
	public void sendRedirect(String location) throws IOException {
		log.debug("Send redirect: {}", location);
		super.sendRedirect(location);
	}
					
	@Override
	public void addHeader(String name, String value) {
		if("Location".equalsIgnoreCase(name)) {
			log.debug("Added Location header: {}", value);
		}
		super.addHeader(name, value);
	}
	
	@Override
	public void setHeader(String name, String value) {
		if("Location".equalsIgnoreCase(name)) {
			log.debug("Set Location header: {}", value);
		}
		super.setHeader(name, value);
	}
	
	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return super.getOutputStream();
	}
	
	@Override
	public PrintWriter getWriter() throws IOException {		
		return super.getWriter();
	}
	
	@Override
	public void flushBuffer() throws IOException {
		super.flushBuffer();
	}
	
	@Override
	public void setCharacterEncoding(String charset) {
		super.setCharacterEncoding(charset);
	}
	
	@Override
	public void setContentType(String type) {
		super.setContentType(type);
	}
}

package com.bugsio.confluence.plugins;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;


public class DomainBoosterSettings implements Serializable {
	private static final long serialVersionUID = 1L;

	protected static String[] splitMultiValueParam(String str) {
		if(StringUtils.isBlank(str))
			return new String[0];
		return StringUtils.split(str, " ,\n\r");
	}
	
	private String unknownHostnameRedirect = "";
	
	private String neverRedirectUsers = "admin";
	
	private String globalAnonForbiddenPaths = "/pages/diffpages.action\r\n/pages/viewpreviousversions.action";
	
	private List<SiteSettings> serverSettings = new ArrayList<SiteSettings>(10);

	private String insertAtEndOfHead, insertAtBeginningOfBody, insertAtEndOfBody;
	
	public DomainBoosterSettings() {
		this(null);
	}
	
	public DomainBoosterSettings(DomainBoosterSettings settings) {
		if(settings != null) {
			setUnknownHostnameRedirect(settings.getUnknownHostnameRedirect());
			setNeverRedirectUsers(settings.getNeverRedirectUsers());
			setGlobalAnonForbiddenPaths(settings.getGlobalAnonForbiddenPaths());
			setInsertAtBeginningOfBody(settings.getInsertAtBeginningOfBody());
			setInsertAtEndOfBody(settings.getInsertAtEndOfBody());
			setInsertAtEndOfHead(settings.getInsertAtEndOfHead());
			setServerSettings(settings.getServerSettings()); // TODO clone these?
		}
	}
	
	public SiteSettings findForHostName(String hostName) {
		for(SiteSettings ss: serverSettings) {
			if(ss.getHostName().equals(hostName))
				return ss;
		}
		return null;
	}
	
	public boolean delete(SiteSettings ss) {
		if(ss != null)
			return serverSettings.remove(ss);
		return false;
	}
	
	public String getUnknownHostnameRedirect() {
		return unknownHostnameRedirect;
	}

	public void setUnknownHostnameRedirect(String unknownHostnameRedirect) {
		if(unknownHostnameRedirect != null)
			unknownHostnameRedirect = unknownHostnameRedirect.trim();
		this.unknownHostnameRedirect = unknownHostnameRedirect;
	}

	public String getNeverRedirectUsers() {
		return neverRedirectUsers;
	}

	public void setNeverRedirectUsers(String neverRedirectUsers) {
		this.neverRedirectUsers = neverRedirectUsers;
	}

	public String getGlobalAnonForbiddenPaths() {
		return globalAnonForbiddenPaths;
	}

	public void setGlobalAnonForbiddenPaths(String globalAnonForbiddenPaths) {
		this.globalAnonForbiddenPaths = globalAnonForbiddenPaths;
	}

	public List<SiteSettings> getServerSettings() {
		return serverSettings;
	}

	public void setServerSettings(List<SiteSettings> serverSettings) {
		if(serverSettings != null)
			this.serverSettings = serverSettings;
		else
			this.serverSettings.clear();
	}

	public String getInsertAtEndOfHead() {
		return insertAtEndOfHead;
	}

	public void setInsertAtEndOfHead(String insertAtEndOfHead) {
		this.insertAtEndOfHead = insertAtEndOfHead;
	}

	public String getInsertAtBeginningOfBody() {
		return insertAtBeginningOfBody;
	}

	public void setInsertAtBeginningOfBody(String insertAtBeginningOfBody) {
		this.insertAtBeginningOfBody = insertAtBeginningOfBody;
	}

	public String getInsertAtEndOfBody() {
		return insertAtEndOfBody;
	}

	public void setInsertAtEndOfBody(String insertAtEndOfBody) {
		this.insertAtEndOfBody = insertAtEndOfBody;
	}
	
}

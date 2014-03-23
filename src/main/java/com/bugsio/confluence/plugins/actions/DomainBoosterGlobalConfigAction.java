package com.bugsio.confluence.plugins.actions;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.bugsio.confluence.plugins.DomainBoosterSettings;
import com.bugsio.confluence.plugins.DomainBoosterSettingsManager;


public class DomainBoosterGlobalConfigAction extends ConfluenceActionSupport {
	private static final long serialVersionUID = 1L;

	private DomainBoosterSettingsManager domainBoosterSettingsManager;

	private String unknownHostnameRedirect;
	private String neverRedirectUsers;
	private String globalAnonForbiddenPaths;
	private String insertAtEndOfHead, insertAtBeginningOfBody, insertAtEndOfBody;
	
	public void setDomainBoosterSettingsManager(DomainBoosterSettingsManager settingsManager) {
		this.domainBoosterSettingsManager = settingsManager;
	}

	public DomainBoosterSettings getDomainBoosterSettings() {
		return domainBoosterSettingsManager.getDomainBoosterSettings();
	}
	
	public boolean isAllowedNewDomain()
	{
		return true;
	}
	
    public String execute()
    {
    	DomainBoosterSettings settings = domainBoosterSettingsManager.getDomainBoosterSettings();
    	
    	settings.setGlobalAnonForbiddenPaths(getGlobalAnonForbiddenPaths());
    	settings.setNeverRedirectUsers(getNeverRedirectUsers());
    	settings.setUnknownHostnameRedirect(getUnknownHostnameRedirect());
    	
    	settings.setInsertAtBeginningOfBody(getInsertAtBeginningOfBody());
    	settings.setInsertAtEndOfBody(getInsertAtEndOfBody());
    	settings.setInsertAtEndOfHead(getInsertAtEndOfHead());
    	
    	domainBoosterSettingsManager.save(settings);
    	
        addActionMessage(getText("domain-booster.config.global.saved"));
    	
        return SUCCESS;
    }

    public String doDefault()
    {
    	// passing messages we may add in another action
    	String msg = (String) getSession().get("domain-booster-message");
    	if(msg != null) {
    		addActionMessage(msg);
    		getSession().remove("domain-booster-message");
    	}
    	msg = (String) getSession().get("domain-booster-error");
    	if(msg != null) {
    		addActionError(msg);
    		getSession().remove("domain-booster-error");
    	}
    	
    	
    	DomainBoosterSettings settings = domainBoosterSettingsManager.getDomainBoosterSettings();
    	
    	setGlobalAnonForbiddenPaths(settings.getGlobalAnonForbiddenPaths());
    	setNeverRedirectUsers(settings.getNeverRedirectUsers());
    	setUnknownHostnameRedirect(settings.getUnknownHostnameRedirect());
    	
    	setInsertAtBeginningOfBody(settings.getInsertAtBeginningOfBody());
    	setInsertAtEndOfBody(settings.getInsertAtEndOfBody());
    	setInsertAtEndOfHead(settings.getInsertAtEndOfHead());
    	
        return SUCCESS;
    }
    
	public String getUnknownHostnameRedirect() {
		return unknownHostnameRedirect;
	}

	public void setUnknownHostnameRedirect(String unknownHostnameRedirect) {
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

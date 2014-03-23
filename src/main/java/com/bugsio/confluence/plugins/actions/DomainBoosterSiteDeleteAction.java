package com.bugsio.confluence.plugins.actions;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.bugsio.confluence.plugins.DomainBoosterSettings;
import com.bugsio.confluence.plugins.DomainBoosterSettingsManager;


public class DomainBoosterSiteDeleteAction extends ConfluenceActionSupport {
	private static final long serialVersionUID = 1L;
	
	private DomainBoosterSettingsManager domainBoosterSettingsManager;
	
	List<String> host = new ArrayList<String>();

	public List<String> getHost() {
		return host;
	}

	public void setHost(List<String> host) {
		this.host = host;
	}
	
	public String confirm() {
		return "confirm";
	}
	
	public void setDomainBoosterSettingsManager(DomainBoosterSettingsManager settingsManager) {
		this.domainBoosterSettingsManager = settingsManager;
	}
	
	public String execute() {
		
    	DomainBoosterSettings settings = domainBoosterSettingsManager.getDomainBoosterSettings();
    	
    	for(String h: host) {
    		settings.delete(settings.findForHostName(h));
    	}
    	
    	domainBoosterSettingsManager.save(settings);
    	
        return SUCCESS;		
	}
	
}

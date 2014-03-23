package com.bugsio.confluence.plugins.actions;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.bugsio.confluence.plugins.DomainBoosterSettings;
import com.bugsio.confluence.plugins.DomainBoosterSettingsManager;
import com.bugsio.confluence.plugins.FindReplaceRule;
import com.bugsio.confluence.plugins.SiteSettings;


public class DomainBoosterSiteTextRuleDeleteAction extends ConfluenceActionSupport {
	private static final long serialVersionUID = 1L;

	private DomainBoosterSettingsManager domainBoosterSettingsManager;
	
	String hostName;
	List<String> index = new ArrayList<String>();

	public void setDomainBoosterSettingsManager(DomainBoosterSettingsManager settingsManager) {
		this.domainBoosterSettingsManager = settingsManager;
	}
	
	public String confirm() {
		return "confirm";
	}
	
	public String execute() {
		
    	DomainBoosterSettings settings = domainBoosterSettingsManager.getDomainBoosterSettings();
    	
    	SiteSettings ss = domainBoosterSettingsManager.getServerSettingsForHostName(hostName);
		if(ss == null) {
			//log.error("Not found settings for host: {}", hostName);
			return SUCCESS;			
		}
		
		List<FindReplaceRule> newRules = new ArrayList<FindReplaceRule>();
		int i = 0;
    	for(FindReplaceRule r: ss.getTextReplacementRules()) {
    		String istr = String.valueOf(i);
    		if(!index.contains(istr))
    			newRules.add(r);
    		++i;
    	}
    	ss.setTextReplacementRules(newRules);
    	
    	domainBoosterSettingsManager.save(settings);
    	
        return SUCCESS;		
	}

	public String getFindText(String i) {
    	SiteSettings ss = domainBoosterSettingsManager.getServerSettingsForHostName(hostName);
		return ss.getTextReplacementRules().get(Integer.parseInt(i)).getF();
	}
	
	public List<String> getIndex() {
		return index;
	}

	public void setIndex(List<String> index) {
		this.index = index;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	
}

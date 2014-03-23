package com.bugsio.confluence.plugins.actions;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.bugsio.confluence.plugins.DomainBoosterSettings;
import com.bugsio.confluence.plugins.DomainBoosterSettingsManager;
import com.bugsio.confluence.plugins.FindReplaceRule;
import com.bugsio.confluence.plugins.SiteSettings;


public class DomainBoosterSiteConfigAction extends ConfluenceActionSupport {
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(DomainBoosterSiteConfigAction.class);
	
	private DomainBoosterSettingsManager domainBoosterSettingsManager;

	private String editHost;
	private boolean editHostForm;
	
	// all props of ServerSettings (and getters/setters are also lower bellow)
	private String hostName;
	private String defaultSpace;
	private int proxyPort;
	private String proxyScheme;
	private String aliases;
	private String siteFooter;
	private String spaces;
	private List<FindReplaceRule> textReplacementRules;
	private boolean anonAllowedUserProfileAccess = false;
	private boolean sendAliasedToMain; // not UI for it!
	private boolean useGlobalAnonForbiddenPaths;
	private boolean knownSpaceForwardAllowed;
	private boolean unknownSpaceHandleAllowed;
	private boolean limitSearchToSite;
	private String dashboardLinkText;
	private String dashboardLinkUrl;
	private boolean hideDashboard;
	private boolean hideSpaceInBreadcrumb;
	private boolean expandBreadcrumb;
	
	private String newFindRule, newReplaceRule;
	private boolean addTextRule;

	public void setDomainBoosterSettingsManager(DomainBoosterSettingsManager settingsManager) {
		this.domainBoosterSettingsManager = settingsManager;
	}

	public boolean isAllowedNewSite()
	{
		return true; // FIXME
	}

	public boolean isNewSiteMode()
	{
		return StringUtils.isBlank(editHost);
	}
	
	public String addSite()
	{
		return execute();
	}
	
	public String execute()
	{
    	DomainBoosterSettings settings = domainBoosterSettingsManager.getDomainBoosterSettings();
    	
    	SiteSettings serverSettings = null;
    	boolean newSite = isNewSiteMode(); 
    	if(newSite) {
    		serverSettings = new SiteSettings();
    		
    	}
    	else {
    		serverSettings = domainBoosterSettingsManager.getServerSettingsForHostName(getEditHost());
    		if(serverSettings == null) {
    			log.error("Not found settings for host {} to update", getEditHost());
    			return INPUT;
    		}
    	}
    	
    	serverSettings.setHostName(getHostName());
    	serverSettings.setDefaultSpace(getDefaultSpace());
    	serverSettings.setAliases(getAliases());
    	serverSettings.setFooter(getSiteFooter());
    	serverSettings.setSpaces(getSpaces());
    	serverSettings.setDashboardLinkText(getDashboardLinkText());
    	serverSettings.setDashboardLinkUrl(getDashboardLinkUrl());
    	if(isEditHostForm()) { // test if submitted via form to keep default site settings when new site is created
	    	serverSettings.setKnownSpaceForwardAllowed(isKnownSpaceForwardAllowed());
	    	serverSettings.setUnknownSpaceHandleAllowed(isUnknownSpaceHandleAllowed());
	    	serverSettings.setUseGlobalAnonForbiddenPaths(isUseGlobalAnonForbiddenPaths());
	    	serverSettings.setAnonAllowedUserProfileAccess(isAnonAllowedUserProfileAccess());
	    	serverSettings.setLimitSearchToSite(isLimitSearchToSite());
	    	serverSettings.setHideDashboard(isHideDashboard());
	    	serverSettings.setHideSpaceInBreadcrumb(isHideSpaceInBreadcrumb());
	    	serverSettings.setExpandBreadcrumb(isExpandBreadcrumb());
    	}
    	
    	if(getProxyPort() >= 0)
    		serverSettings.setProxyPort(getProxyPort());
    	// TODO scheme?
    	
    	if(newSite)
    		settings.getServerSettings().add(serverSettings);
    	
    	domainBoosterSettingsManager.save(settings);
    	
    	if(!newSite)
    		domainBoosterSettingsManager.clearCachesForHostName(getEditHost());
    	
        //addActionMessage(getText("domain-booster.config.global.saved")); // message does not survive redirect :-(
    	
        return SUCCESS;		
	}
	
	public String editSite()
	{
		if(StringUtils.isBlank(editHost)) {
			log.debug("editHost param is missing");
			return "not-found";
		}
		
    	SiteSettings ss = domainBoosterSettingsManager.getServerSettingsForHostName(editHost);
		if(ss == null) {
			log.debug("Not found settings for host: {}", editHost);
			return "not-found";			
		}

		setHostName(ss.getHostName());
		setDefaultSpace(ss.getDefaultSpace());
		setProxyPort(ss.getProxyPort());
		setProxyScheme(ss.getProxyScheme());
		setAliases(ss.getAliases());
		setSiteFooter(ss.getFooter());
		setSpaces(ss.getSpaces());
		setAnonAllowedUserProfileAccess(ss.isAnonAllowedUserProfileAccess());
		//setSendAliasedToMain(ss.isSendAliasedToMain());
		setUseGlobalAnonForbiddenPaths(ss.isUseGlobalAnonForbiddenPaths());
		setKnownSpaceForwardAllowed(ss.isKnownSpaceForwardAllowed());
		setUnknownSpaceHandleAllowed(ss.isUnknownSpaceHandleAllowed());
		setLimitSearchToSite(ss.isLimitSearchToSite());
		setDashboardLinkText(ss.getDashboardLinkText());
		setDashboardLinkUrl(ss.getDashboardLinkUrl());
		setHideDashboard(ss.isHideDashboard());
		setHideSpaceInBreadcrumb(ss.isHideSpaceInBreadcrumb());
		setExpandBreadcrumb(ss.isExpandBreadcrumb());
		
		setTextReplacementRules(ss.getTextReplacementRules());
		
		return INPUT;
	}
	
	public String newFindReplaceRule()
	{
    	DomainBoosterSettings settings = domainBoosterSettingsManager.getDomainBoosterSettings();
    	
    	SiteSettings ss = domainBoosterSettingsManager.getServerSettingsForHostName(editHost);
		if(ss == null) {
			log.error("Not found settings for host: {}", editHost);
			return INPUT;			
		}
		
		log.debug("Adding new text rule: {} -> {}", newFindRule, newReplaceRule);
		
    	if(!StringUtils.isBlank(newFindRule)) {
    		ss.addOrUpdateTextReplacementRule(newFindRule, newReplaceRule);
    		domainBoosterSettingsManager.save(settings);
    	}
    	newFindRule = "";
    	newReplaceRule = "";
    	
    	return editSite();
	}
	
    public void validate()
    {
        super.validate();

        if(isAddTextRule()) {
            if(StringUtils.isBlank(getNewFindRule()))
            	addFieldError("newFindRule", getText("domain-booster.config.error.newFindRule.required"));
        	return;
        }
        
        if(StringUtils.isBlank(getHostName()))
        	addFieldError("hostName", getText("domain-booster.config.error.hostName.required"));
        
        if(StringUtils.isBlank(getDefaultSpace()))
        	addFieldError("defaultSpace", getText("domain-booster.config.error.defaultSpace.required"));

        // TODO later check if key format is valid (no whitespace)
        
        // in case of new site is being created and sending user to full form, init checkboxes
        if(hasFieldErrors() && isNewSiteMode() && !isEditHostForm()) {
        	SiteSettings ss = new SiteSettings();
    		setAnonAllowedUserProfileAccess(ss.isAnonAllowedUserProfileAccess());
    		setUseGlobalAnonForbiddenPaths(ss.isUseGlobalAnonForbiddenPaths());
    		setKnownSpaceForwardAllowed(ss.isKnownSpaceForwardAllowed());
    		setUnknownSpaceHandleAllowed(ss.isUnknownSpaceHandleAllowed());     
    		setLimitSearchToSite(ss.isLimitSearchToSite());
    		setHideDashboard(ss.isHideDashboard());
    		setHideSpaceInBreadcrumb(ss.isHideSpaceInBreadcrumb());
    		setExpandBreadcrumb(ss.isExpandBreadcrumb());
        }
        
        if(!hasFieldErrors()) {
        	
        	SiteSettings editedSite = isNewSiteMode() ? null : domainBoosterSettingsManager.getServerSettingsForHostName(getEditHost());
        	
        	SiteSettings ss = domainBoosterSettingsManager.getServerSettingsForHostName(getHostName());
    		if(ss != null && ss != editedSite)
    			addFieldError("hostName", getText("domain-booster.config.error.hostName.exists"));
        	
        	ss = domainBoosterSettingsManager.getServerSettingsForSpace(getDefaultSpace());
    		if(ss != null && ss != editedSite)
    			addFieldError("defaultSpace", getText("domain-booster.config.error.defaultSpace.exists", 
    					new String[] { getDefaultSpace(), ss.getHostName() }));
        }
    }
    
	public String getEditHost() {
		return editHost;
	}

	public void setEditHost(String editHost) {
		this.editHost = editHost;
	}

	public boolean isEditHostForm() {
		return editHostForm;
	}

	public void setEditHostForm(boolean editHostForm) {
		this.editHostForm = editHostForm;
	}

	public String getProxyPortStr() {
		if(getProxyPort() > 0)
			return String.valueOf(getProxyPort());
		return "";
	}
	
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public String getDefaultSpace() {
		return defaultSpace;
	}
	public void setDefaultSpace(String defaultSpace) {
		this.defaultSpace = defaultSpace;
	}
	public int getProxyPort() {
		return proxyPort;
	}
	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}
	public String getProxyScheme() {
		return proxyScheme;
	}
	public void setProxyScheme(String proxyScheme) {
		this.proxyScheme = proxyScheme;
	}
	public String getAliases() {
		return aliases;
	}
	public void setAliases(String aliases) {
		this.aliases = aliases;
	}
	public String getSiteFooter() {
		return siteFooter;
	}
	public void setSiteFooter(String footer) {
		this.siteFooter = footer;
	}
	public String getSpaces() {
		return spaces;
	}
	public void setSpaces(String spaces) {
		this.spaces = spaces;
	}
	public List<FindReplaceRule> getTextReplacementRules() {
		return textReplacementRules;
	}
	public void setTextReplacementRules(List<FindReplaceRule> textReplacementRules) {
		this.textReplacementRules = textReplacementRules;
	}
	public boolean isAnonAllowedUserProfileAccess() {
		return anonAllowedUserProfileAccess;
	}
	public void setAnonAllowedUserProfileAccess(boolean anonAllowedUserProfileAccess) {
		this.anonAllowedUserProfileAccess = anonAllowedUserProfileAccess;
	}
	public boolean isSendAliasedToMain() {
		return sendAliasedToMain;
	}
	public void setSendAliasedToMain(boolean sendAliasedToMain) {
		this.sendAliasedToMain = sendAliasedToMain;
	}
	public boolean isUseGlobalAnonForbiddenPaths() {
		return useGlobalAnonForbiddenPaths;
	}
	public void setUseGlobalAnonForbiddenPaths(boolean useGlobalAnonForbiddenPaths) {
		this.useGlobalAnonForbiddenPaths = useGlobalAnonForbiddenPaths;
	}
	public boolean isKnownSpaceForwardAllowed() {
		return knownSpaceForwardAllowed;
	}
	public void setKnownSpaceForwardAllowed(boolean knownSpaceForwardAllowed) {
		this.knownSpaceForwardAllowed = knownSpaceForwardAllowed;
	}
	public boolean isUnknownSpaceHandleAllowed() {
		return unknownSpaceHandleAllowed;
	}
	public void setUnknownSpaceHandleAllowed(boolean unknownSpaceHandleAllowed) {
		this.unknownSpaceHandleAllowed = unknownSpaceHandleAllowed;
	}
	public boolean isLimitSearchToSite() {
		return limitSearchToSite;
	}
	public void setLimitSearchToSite(boolean limitSearchToSite) {
		this.limitSearchToSite = limitSearchToSite;
	}

	public String getDashboardLinkText() {
		return dashboardLinkText;
	}

	public void setDashboardLinkText(String dashboardLinkText) {
		this.dashboardLinkText = dashboardLinkText;
	}

	public String getDashboardLinkUrl() {
		return dashboardLinkUrl;
	}

	public void setDashboardLinkUrl(String dashboardLinkUrl) {
		this.dashboardLinkUrl = dashboardLinkUrl;
	}

	public boolean isHideDashboard() {
		return hideDashboard;
	}

	public void setHideDashboard(boolean hideDashboard) {
		this.hideDashboard = hideDashboard;
	}

	public boolean isHideSpaceInBreadcrumb() {
		return hideSpaceInBreadcrumb;
	}

	public void setHideSpaceInBreadcrumb(boolean hideSpaceInBreadcrumb) {
		this.hideSpaceInBreadcrumb = hideSpaceInBreadcrumb;
	}

	public boolean isExpandBreadcrumb() {
		return expandBreadcrumb;
	}

	public void setExpandBreadcrumb(boolean expandBreadcrumb) {
		this.expandBreadcrumb = expandBreadcrumb;
	}

	public String getNewFindRule() {
		return newFindRule;
	}

	public void setNewFindRule(String newFindRule) {
		this.newFindRule = newFindRule;
	}

	public String getNewReplaceRule() {
		return newReplaceRule;
	}

	public void setNewReplaceRule(String newReplaceRule) {
		this.newReplaceRule = newReplaceRule;
	}

	public boolean isAddTextRule() {
		return addTextRule;
	}

	public void setAddTextRule(boolean addTextRule) {
		this.addTextRule = addTextRule;
	}
	
}

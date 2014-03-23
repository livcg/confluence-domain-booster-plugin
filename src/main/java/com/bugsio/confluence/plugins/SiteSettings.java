package com.bugsio.confluence.plugins;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;


public class SiteSettings {
	
	private String hostName;
	
	private String defaultSpace;
	
	/** Value 0 means default port */
	private int proxyPort;
	
	private String proxyScheme;
	
	/** Request for aliased site should result with redirect to the main server name */ 
	private String aliases;
	
	private String spaces;

	private String footer;
	
	private boolean anonAllowedUserProfileAccess = false;
	
	private boolean sendAliasedToMain = true; // no UI option for it!
	
	private boolean useGlobalAnonForbiddenPaths = true;
	
	private boolean knownSpaceForwardAllowed = true;
	
	private boolean unknownSpaceHandleAllowed = true;
	
	private boolean limitSearchToSite = true;
	
	private String dashboardLinkText;
	private String dashboardLinkUrl;
	private boolean hideDashboard;
	private boolean hideSpaceInBreadcrumb;
	private boolean expandBreadcrumb;
	
	
	private List<FindReplaceRule> textReplacementRules = new ArrayList<FindReplaceRule>();
			
	// NOTE: after adding new property re-generate equals and hashCode !
	
	public void addOrUpdateTextReplacementRule(String find, String replace) {
		if(StringUtils.isBlank(find))
			return;
		for(FindReplaceRule fr: textReplacementRules) {
			if(find.equals(fr.getF())) {
				fr.setR(replace);
				return;
			}
		}
		FindReplaceRule fr = new FindReplaceRule();
		fr.setF(find);
		fr.setR(replace);
		textReplacementRules.add(fr);
	}
	
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		if(hostName == null)
			throw new IllegalArgumentException();
		this.hostName = hostName.toLowerCase();
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
	public String getFooter() {
		return footer;
	}
	public void setFooter(String footer) {
		this.footer = footer;
	}
	public String getSpaces() {
		return spaces;
	}
	public void setSpaces(String spaces) {
		this.spaces = spaces;
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
	public List<FindReplaceRule> getTextReplacementRules() {
		if(textReplacementRules == null)
			textReplacementRules = new ArrayList<FindReplaceRule>();
		return textReplacementRules;
	}
	public void setTextReplacementRules(List<FindReplaceRule> textReplacementRules) {
		this.textReplacementRules = textReplacementRules;
	}
	
	public boolean isHideDashboard() {
		return hideDashboard;
	}

	public void setHideDashboard(boolean hideDashboard) {
		this.hideDashboard = hideDashboard;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((aliases == null) ? 0 : aliases.hashCode());
		result = prime * result + (anonAllowedUserProfileAccess ? 1231 : 1237);
		result = prime * result + ((dashboardLinkText == null) ? 0 : dashboardLinkText.hashCode());
		result = prime * result + ((dashboardLinkUrl == null) ? 0 : dashboardLinkUrl.hashCode());
		result = prime * result + ((defaultSpace == null) ? 0 : defaultSpace.hashCode());
		result = prime * result + (expandBreadcrumb ? 1231 : 1237);
		result = prime * result + ((footer == null) ? 0 : footer.hashCode());
		result = prime * result + (hideDashboard ? 1231 : 1237);
		result = prime * result + (hideSpaceInBreadcrumb ? 1231 : 1237);
		result = prime * result + ((hostName == null) ? 0 : hostName.hashCode());
		result = prime * result + (knownSpaceForwardAllowed ? 1231 : 1237);
		result = prime * result + (limitSearchToSite ? 1231 : 1237);
		result = prime * result + proxyPort;
		result = prime * result + ((proxyScheme == null) ? 0 : proxyScheme.hashCode());
		result = prime * result + (sendAliasedToMain ? 1231 : 1237);
		result = prime * result + ((spaces == null) ? 0 : spaces.hashCode());
		result = prime * result + ((textReplacementRules == null) ? 0 : textReplacementRules.hashCode());
		result = prime * result + (unknownSpaceHandleAllowed ? 1231 : 1237);
		result = prime * result + (useGlobalAnonForbiddenPaths ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SiteSettings other = (SiteSettings) obj;
		if (aliases == null) {
			if (other.aliases != null)
				return false;
		} else if (!aliases.equals(other.aliases))
			return false;
		if (anonAllowedUserProfileAccess != other.anonAllowedUserProfileAccess)
			return false;
		if (dashboardLinkText == null) {
			if (other.dashboardLinkText != null)
				return false;
		} else if (!dashboardLinkText.equals(other.dashboardLinkText))
			return false;
		if (dashboardLinkUrl == null) {
			if (other.dashboardLinkUrl != null)
				return false;
		} else if (!dashboardLinkUrl.equals(other.dashboardLinkUrl))
			return false;
		if (defaultSpace == null) {
			if (other.defaultSpace != null)
				return false;
		} else if (!defaultSpace.equals(other.defaultSpace))
			return false;
		if (expandBreadcrumb != other.expandBreadcrumb)
			return false;
		if (footer == null) {
			if (other.footer != null)
				return false;
		} else if (!footer.equals(other.footer))
			return false;
		if (hideDashboard != other.hideDashboard)
			return false;
		if (hideSpaceInBreadcrumb != other.hideSpaceInBreadcrumb)
			return false;
		if (hostName == null) {
			if (other.hostName != null)
				return false;
		} else if (!hostName.equals(other.hostName))
			return false;
		if (knownSpaceForwardAllowed != other.knownSpaceForwardAllowed)
			return false;
		if (limitSearchToSite != other.limitSearchToSite)
			return false;
		if (proxyPort != other.proxyPort)
			return false;
		if (proxyScheme == null) {
			if (other.proxyScheme != null)
				return false;
		} else if (!proxyScheme.equals(other.proxyScheme))
			return false;
		if (sendAliasedToMain != other.sendAliasedToMain)
			return false;
		if (spaces == null) {
			if (other.spaces != null)
				return false;
		} else if (!spaces.equals(other.spaces))
			return false;
		if (textReplacementRules == null) {
			if (other.textReplacementRules != null)
				return false;
		} else if (!textReplacementRules.equals(other.textReplacementRules))
			return false;
		if (unknownSpaceHandleAllowed != other.unknownSpaceHandleAllowed)
			return false;
		if (useGlobalAnonForbiddenPaths != other.useGlobalAnonForbiddenPaths)
			return false;
		return true;
	}

}
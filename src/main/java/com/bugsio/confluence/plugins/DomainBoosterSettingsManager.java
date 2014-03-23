package com.bugsio.confluence.plugins;

import java.io.File;


public interface DomainBoosterSettingsManager {
	
    DomainBoosterSettings getDomainBoosterSettings();

    void save(DomainBoosterSettings domainBoosterSettings);
    
    SiteSettings getServerSettingsForHostName(String hostName);
    
    SiteSettings getServerSettingsForSpace(String space);
    
    void clearCachesForHostName(String hostName);
    
    String getSettingsJS(SiteSettings siteSettings, boolean anonUser);
    
    boolean isPathForbiddenForAnon(String path);
    
    boolean isRedirectDisabledForUser(String username);
    
    String exportDomainBoosterSettings();
    boolean restoreDomainBoosterSettings(File file) throws Exception;
}

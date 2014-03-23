package com.bugsio.confluence.plugins;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.bandana.BandanaManager;
import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheManager;
import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
import com.thoughtworks.xstream.XStream;


public class DefaultDomainBoosterSettingsManager implements DomainBoosterSettingsManager {
	
	private static final Logger log = LoggerFactory.getLogger(DefaultDomainBoosterSettingsManager.class);

    private static final String CACHE_KEY = "com.bugsio.confluence.plugins.domain-booster.settings";

	private static final String SETTINGS_KEY = "com.bugsio.confluence.plugins.domain-booster.settings.v1";

    private final BandanaManager bandanaManager;

    private final CacheManager cacheManager;

    private final XStream xStream;
    
    private boolean initialized = false;
	private Map<String, SiteSettings> serverNameSettings = new HashMap<String, SiteSettings>();
	private Map<String, SiteSettings> spaceSettings = new HashMap<String, SiteSettings>();
	private List<String> globalAnonForbiddenPaths = new ArrayList<String>();
	private List<String> neverRedirectUsers = new ArrayList<String>();
    
	private Map<String, String> jsEncodedSiteSettings = Collections.synchronizedMap(new HashMap<String, String>());
	
    public DefaultDomainBoosterSettingsManager(BandanaManager bandanaManager, CacheManager cacheManager)
    {
        this.bandanaManager = bandanaManager;
        this.cacheManager = cacheManager;
        this.xStream = new XStream();
        xStream.setClassLoader(getClass().getClassLoader());
    }

    private String getCacheEntryKey()
    {
        return CACHE_KEY;
    }

    private DomainBoosterSettings getCachedSetings()
    {
        Cache webdavSettingsCache = cacheManager.getCache(CACHE_KEY);
        String cacheEntryKey = getCacheEntryKey();
        try
        {
            return (DomainBoosterSettings) webdavSettingsCache.get(cacheEntryKey);
        }
        catch (ClassCastException cce)
        {
            // If this happens, either someone stored something that is not a WebdavSettings into the cache with the same key
            // or the plugin was upgraded and the WebdavSettings loaded by the previous class loader can't be cast to
            // a WebdavSettings.
            log.debug("Unable to cast the cached DomainBoosterSettings retrieved with key " + cacheEntryKey + " to a DomainBoosterSettings. It will be purged from the cache.");
            webdavSettingsCache.remove(cacheEntryKey);
        }
        return null;
    }

    private void cacheSettings(DomainBoosterSettings settings)
    {
        Cache webdavSettingsCache = cacheManager.getCache(CACHE_KEY);
        webdavSettingsCache.put(getCacheEntryKey(), settings);
    }

    @Override
    public String exportDomainBoosterSettings() {
    	return xStream.toXML(getDomainBoosterSettings());
    }
    
    @Override
    public boolean restoreDomainBoosterSettings(File file) throws Exception {
    	try {
    		DomainBoosterSettings settings = (DomainBoosterSettings) xStream.fromXML(new FileReader(file));
    		save(settings);
    	}
    	catch(Throwable e) {
    		log.error("Error importing settings XML", e);
    		return false;
    	}
    	return true;
    }
    
	@Override
	public DomainBoosterSettings getDomainBoosterSettings() {
		DomainBoosterSettings settings = getCachedSetings();

        if (null != settings)
            return new DomainBoosterSettings(settings); /* Because WebdavSettings is not immutable */

        String settingsXml = (String) bandanaManager.getValue(ConfluenceBandanaContext.GLOBAL_CONTEXT, SETTINGS_KEY);

        log.debug("Settings XML for key {}: {}", SETTINGS_KEY, settingsXml);
        
        try {
	        settings = StringUtils.isNotBlank(settingsXml)
	                ? (DomainBoosterSettings) xStream.fromXML(settingsXml)
	                : new DomainBoosterSettings();
        }
        catch(Throwable e) {
        	log.error("Error extracting settings from XML: " + settingsXml, e);
        	settings = new DomainBoosterSettings();
        }
        cacheSettings(new DomainBoosterSettings(settings));
        
        return settings;
	}
	
	@Override
	public void save(DomainBoosterSettings domainBoosterSettings) {
        bandanaManager.setValue(
                ConfluenceBandanaContext.GLOBAL_CONTEXT,
                SETTINGS_KEY,
                xStream.toXML(domainBoosterSettings)
        );
        cacheSettings(new DomainBoosterSettings(domainBoosterSettings));
        
        synchronized (this) {
			initialized = false;
			init(domainBoosterSettings);
		}
	}
	
	private synchronized void init(DomainBoosterSettings domainBoosterSettings) {
		
		Map<String, SiteSettings> serverNameSettings = new HashMap<String, SiteSettings>();
		Map<String, SiteSettings> spaceSettings = new HashMap<String, SiteSettings>();
		for(SiteSettings ss: domainBoosterSettings.getServerSettings()) {
			
			addServerNameMappingSafe(ss.getHostName(), ss, serverNameSettings);
			String[] aliases = DomainBoosterSettings.splitMultiValueParam(ss.getAliases());
			for(String alias: aliases) {
				addServerNameMappingSafe(alias, ss, serverNameSettings);
			}
			
			addSpaceMappingSafe(ss.getDefaultSpace(), ss, spaceSettings);
			String[] spaces = DomainBoosterSettings.splitMultiValueParam(ss.getSpaces());
			for(String space: spaces) {
				addSpaceMappingSafe(space, ss, spaceSettings);
			}
		}

		// use fast members update as we don't use synchronize on service methods
		this.serverNameSettings = serverNameSettings;
		this.spaceSettings = spaceSettings;
		this.globalAnonForbiddenPaths = Arrays.asList(
				DomainBoosterSettings.splitMultiValueParam(
						domainBoosterSettings.getGlobalAnonForbiddenPaths()));
		this.neverRedirectUsers = Arrays.asList(
				DomainBoosterSettings.splitMultiValueParam(
						domainBoosterSettings.getNeverRedirectUsers()));
		
		log.debug("*** NEW servers: {}", this.serverNameSettings);
		log.debug("*** NEW spaceSettings: {}", this.spaceSettings);
		log.debug("*** NEW globalAnonForbiddenPaths: {}", this.globalAnonForbiddenPaths);
		log.debug("*** NEW neverRedirectUsers: {}", this.neverRedirectUsers);
		
		initialized = true;
	}
	
	private boolean addServerNameMappingSafe(String serverName, SiteSettings ss, Map<String, SiteSettings> serverNameSettings) {
		SiteSettings existingSettings = serverNameSettings.get(serverName);
		if(existingSettings != null && existingSettings != ss) {
			// FIXME log issue
			return false;
		}
		serverNameSettings.put(serverName, ss);
		return true;
	}
	
	private boolean addSpaceMappingSafe(String space, SiteSettings ss, Map<String, SiteSettings> spaceSettings) {
		SiteSettings existingSettings = spaceSettings.get(space);
		if(existingSettings != null && existingSettings != ss) {
			// FIXME log issue
			return false;
		}
		spaceSettings.put(space, ss);
		return true;
	}
	
	private void initIfNotAlready() {
		if(!initialized) {
			synchronized (this) {
				if(!initialized) {
					init(getDomainBoosterSettings());
				}
			}
		}
	}
	
	public SiteSettings getServerSettingsForHostName(String hostName) {
		initIfNotAlready();
		SiteSettings ss = serverNameSettings.get(hostName);
		return ss;
	}
	
	public SiteSettings getServerSettingsForSpace(String space) {
		initIfNotAlready();
		SiteSettings ss = spaceSettings.get(space);
		return ss;
	}

	public void clearCachesForHostName(String hostName) {
		synchronized (jsEncodedSiteSettings) {
			jsEncodedSiteSettings.remove(hostName + "-" + "guest");
			jsEncodedSiteSettings.remove(hostName + "-" + "user");
		}
	}
	
	public String getSettingsJS(SiteSettings siteSettings, boolean anonUser) {
		String cacheKey = siteSettings.getHostName() + "-" + (anonUser ? "guest" : "user");
		String js = jsEncodedSiteSettings.get(cacheKey);
		if(js == null) {
			synchronized (jsEncodedSiteSettings) {
				js = jsEncodedSiteSettings.get(cacheKey);
				if(js == null) {
					
//					SiteStyle style = anonUser ? siteSettings.getAnonStyle() : siteSettings.getUserStyle();
					
					// pass data needed in JS
					JSONObject obj=new JSONObject();
					obj.put("footer", siteSettings.getFooter());
					
					String pageClass = "dbp-site dbp-site-" + siteSettings.getDefaultSpace();
					if(anonUser)
						pageClass += " dbp-guest ";
					if(pageClass.length() > 0)
						obj.put("pageClass", pageClass.toLowerCase());
					
					
					if(StringUtils.isNotBlank(siteSettings.getDashboardLinkText()))
						obj.put("dashboardLinkText", siteSettings.getDashboardLinkText());
					if(StringUtils.isNotBlank(siteSettings.getDashboardLinkUrl()))
						obj.put("dashboardLinkUrl", siteSettings.getDashboardLinkUrl());
					
					obj.put("hideDashboard", siteSettings.isHideDashboard());
					obj.put("expandBreadcrumb", siteSettings.isExpandBreadcrumb());
					obj.put("hideSpaceInBreadcrumb", siteSettings.isHideSpaceInBreadcrumb());
					
//					if(style != null) {
//						obj.put("hideChildPages", style.isHideChildPages());
//						obj.put("hideComments", style.isHideComments());
//						obj.put("hideLike", style.isHideLike());
//						obj.put("hideHeaderMenu", style.isHideHeaderMenu());
//						obj.put("hidePageMeta", style.isHidePageMeta());
//						obj.put("hideTools", style.isHideTools());
//					}
					//obj.put("css", style.getCss());
					//obj.put("script", style.getScript());
					
					
					String json = obj.toString();					
					js = "<script>var dbSiteSettingsStr = '" + StringEscapeUtils.escapeJavaScript(json) + "';</script>";
					
					jsEncodedSiteSettings.put(cacheKey, js);
				}
			}
		}
		return js;
	}
	
	public boolean isPathForbiddenForAnon(String path) {
		if(path == null || path.length() == 0)
			return false;
		for(String s: globalAnonForbiddenPaths)
			if(path.startsWith(s))
				return true;
		return false;
	}
	
	public boolean isRedirectDisabledForUser(String username) {
		return neverRedirectUsers.contains(username);
	}
}

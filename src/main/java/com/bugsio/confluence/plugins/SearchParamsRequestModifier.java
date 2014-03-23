package com.bugsio.confluence.plugins;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SearchParamsRequestModifier extends HttpServletRequestWrapper {

	private static final Logger log = LoggerFactory.getLogger(SearchParamsRequestModifier.class);
			
	private SiteSettings siteSettings;
	
	private String spacesSearchRequest;
	
	//private DomainBoosterSettings domainBoosterSettings;
	
	private Map modifiedParameterMap;
	
	public SearchParamsRequestModifier(HttpServletRequest request, 
			SiteSettings siteSettings, DomainBoosterSettings domainBoosterSettings) {
		super(request);
		this.siteSettings = siteSettings;
		//this.domainBoosterSettings = domainBoosterSettings; 
	}

	private List<String> _siteSpaces;
	private List<String> getSiteSpaces() {
		if(_siteSpaces == null) {
			List<String> spaces = new ArrayList<String>();
			spaces.add(siteSettings.getDefaultSpace());
			String[] otherSpaces = DomainBoosterSettings.splitMultiValueParam(siteSettings.getSpaces());
			for(String s: otherSpaces) {
				if(!StringUtils.isBlank(s) && !spaces.contains(s))
					spaces.add(s);
			}
			_siteSpaces = spaces;
		}
		return _siteSpaces;
	}
	
	private String getSpacesSearchRequest() {
		if(spacesSearchRequest == null) {
			List<String> spaces = getSiteSpaces();			
			String siteSearchquery = null;
			if(spaces.size() == 1)
				siteSearchquery = spaces.get(0);
			else {
				StringBuilder buf = new StringBuilder();
				for(String space: spaces) {
					if(buf.length() > 0)
						buf.append(" OR ");
					buf.append(space);
				}
				siteSearchquery = String.format("(%s)", buf.toString());
			}
			spacesSearchRequest = String.format("spacekey:%s", siteSearchquery);			
		}
		return spacesSearchRequest;
	}
	
	private Map getModifiedParameterMap() {
		if(modifiedParameterMap == null) {
			modifiedParameterMap = new HashMap<String, Object>(super.getParameterMap());
			modifySearchParam(modifiedParameterMap, "query", "spaceKey", 1);
			modifySearchParam(modifiedParameterMap, "queryString", "where", 2);
			modifiedParameterMap = Collections.unmodifiableMap(modifiedParameterMap);
		}
		return modifiedParameterMap;
	}
	
	private void modifySearchParam(Map map, String queryParam, String spaceScopeParam, int modifyMethod) {
		String q = getParamValueFromMap(queryParam, map);
		if(!StringUtils.isBlank(q)) {
			String s = getParamValueFromMap(spaceScopeParam, map);
			
			// is search already limited to site's space?
			if(!StringUtils.isBlank(s) && getSiteSpaces().contains(s)) {
				log.debug("Search request '{}' is already scoped to space '{}' belonging to the used site.", q, s);
				return;
			}
			
			String spacesFiler = getSpacesSearchRequest();
			
			// before changing the query, make sure we're not already limiting it
			int ind = q.indexOf(spacesFiler);
			if(ind != -1 && (ind + spacesFiler.length() == q.length())) {
				log.debug("Search request '{}' already contains the filter.", q);
				return;
			}
			
			if(1 == modifyMethod) { // can't update query, can specify one space param only
				
				// not sure if this will happen as instant search request doesn't belong to a space, but...
				String reqSpace = DomainBoosterContextHolder.getRequestedSpace();
				if(reqSpace != null && getSiteSpaces().contains(reqSpace)) { 
					log.debug("Narrow search to the current space {}", reqSpace);
					map.put(spaceScopeParam, reqSpace);					
				}
				// TODO one more alternative would be to rely on referrer space! - extract in filter and put to DBCH
				
				else if(DomainBoosterContextHolder.getSiteSettings() != null) {
					log.debug("Narrow search to site's default space: {}", DomainBoosterContextHolder.getSiteSettings().getDefaultSpace());
					map.put(spaceScopeParam, DomainBoosterContextHolder.getSiteSettings().getDefaultSpace());					
				}
				
				else {
					//log.debug("Can't limit search request on unknown site");
				}
				
			}
			else if(2 == modifyMethod) { // can update query
				
				// create final query
				String modifiedQuery = String.format("((%s) AND %s)", q, spacesFiler);
				map.put(queryParam, modifiedQuery);
				
				log.debug("*** MAPPED QUERY: {} -> {}", q, modifiedQuery);
				
				// simple text mapping
				DomainBoosterContextHolder.addSearchReplaceRule(modifiedQuery, q);
				
				// encoded mapping
				try {
					String qe = URLEncoder.encode(q, "UTF-8");
					String me = URLEncoder.encode(modifiedQuery, "UTF-8");
					
					log.debug("*** MAPPED QUERY ENCODED: {} -> {}", qe, me);
					
					DomainBoosterContextHolder.addSearchReplaceRule(me, qe);
					
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				
			}
		}
	}
	
	private String getParamValueFromMap(String param, Map map) {
		String[] p = (String[]) map.get(param);
		if(p == null || p.length == 0)
			return null;
		return p[0];
	}
	
	@Override
	public String getParameter(String name) {
		String[] values = (String[]) getParameterMap().get(name);
		if(values != null && values.length > 0)
			return values[0];
		return null;
	}
	
	@Override
	public String[] getParameterValues(String name) {		
		return (String[]) getParameterMap().get(name);
	}
	
	@Override
	public Map getParameterMap() {
		return getModifiedParameterMap();
	}
}

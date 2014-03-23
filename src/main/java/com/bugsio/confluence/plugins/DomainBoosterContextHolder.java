package com.bugsio.confluence.plugins;

import java.util.ArrayList;
import java.util.List;


class DomainBoosterContextHolder {

	private static ThreadLocal<String> requestSpaceHolder = new ThreadLocal<String>();
	private static ThreadLocal<SiteSettings> siteSettingsHolder = new ThreadLocal<SiteSettings>();
	private static ThreadLocal<List<String>> searchStringsHolder = new ThreadLocal<List<String>>();
	private static ThreadLocal<List<String>> replaceStringsHolder = new ThreadLocal<List<String>>();
	
	static void clearThreadLocals() {
		requestSpaceHolder.set(null);
		siteSettingsHolder.set(null);
		
		// init or clear search and replace lists
		if(searchStringsHolder.get() == null)
			searchStringsHolder.set(new ArrayList<String>(30));
		else
			searchStringsHolder.get().clear();
		if(replaceStringsHolder.get() == null)
			replaceStringsHolder.set(new ArrayList<String>(30));
		else
			replaceStringsHolder.get().clear();
	}


	static void setRequestedSpace(String space) {
		requestSpaceHolder.set(space);
	}

	static String getRequestedSpace() {
		return requestSpaceHolder.get();
	}

	static void setSiteSettings(SiteSettings ss) {
		siteSettingsHolder.set(ss);
	}

	static SiteSettings getSiteSettings() {
		return siteSettingsHolder.get();
	}

	static boolean addSearchReplaceRule(String search, String replace) {
		List<String> searches = searchStringsHolder.get();
		List<String> replaces = replaceStringsHolder.get();
		if(!searches.contains(search)) {
			searches.add(search);
			replaces.add(replace);
		}
		else {
			return false;
		}
		return true;
	}
	
	static String[] getSearchRules() {
		List<String> searches = searchStringsHolder.get();
		return searches.toArray(new String[searches.size()]);
	}
	
	static String[] getReplaceRules() {
		List<String> replaces = replaceStringsHolder.get();
		return replaces.toArray(new String[replaces.size()]);
	}
}

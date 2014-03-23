package com.bugsio.confluence.plugins;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.user.UserManager;
import com.bugsio.atlassian.plugin.PluginUtilsLite;


public class DomainBoosterFilter implements Filter {

	private static final Logger log = LoggerFactory.getLogger(DomainBoosterFilter.class);
	
	private static final String DISPLAY_PREFIX = "/display/";
	
	private static final String[] IGNORED_PATHS = { "/download", "/rest", "/images" };
	
	private static final DateFormat exportDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	private final UserManager userManager;
	private final WebResourceManager webResourceManager;
	private final DomainBoosterSettingsManager settingsManager;
	
	private static final Class[] putMetadataParamTypes = new Class[] { String.class, String.class };
	
	public DomainBoosterFilter(UserManager userManager, WebResourceManager webResourceManager, DomainBoosterSettingsManager settingsManager) {
		this.userManager = userManager;
		this.webResourceManager = webResourceManager;
		this.settingsManager = settingsManager;
	}
	
	public void init(FilterConfig arg0) throws ServletException {
	}
	
	public void destroy() {
	}
	
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) 
			throws IOException, ServletException {

		final HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		
		// Protection from filter re-entry... just in case...
		if(request.getAttribute(this.getClass().getName()) != null) {
			log.info("Detected filter re-entry");
			chain.doFilter(request, response);
			return;
		}
		request.setAttribute(this.getClass().getName(), Boolean.TRUE);

		DomainBoosterContextHolder.clearThreadLocals();
				
		String requestedServer = request.getServerName().toLowerCase();
		int requestedPort = request.getServerPort();
		String servletPath = request.getServletPath();
		String pathInfo = request.getPathInfo();
		
		log.debug("Requested site {}:{}, servletPath={}, pathInfo={}", new String[] { requestedServer, String.valueOf(requestedPort), servletPath, pathInfo });
		
		String username = userManager.getRemoteUsername(request);
		boolean anonUser = username == null;
		
		if("/admin/downloadDomainBoosterSettings".equals(servletPath)) {
			if(!userManager.isSystemAdmin(username)) {
				response.sendError(403);
				return;
			}
			response.setContentType("application/xml");
			response.addHeader("Content-Disposition", "attachment; filename=\"domain-booster-config-" + 
					exportDateFormat.format(new Date()) + ".xml\"");
			PrintWriter out = response.getWriter(); 
			out.print(settingsManager.exportDomainBoosterSettings());
			out.flush();
			out.close();			
			return;
		}
		
		// check option to never redirect certain users (e.g. admin)
		if(!anonUser && settingsManager.isRedirectDisabledForUser(username)) {
			processRequest(request, response, chain, settingsManager, null, false, anonUser);
			return;			
		}

        webResourceManager.requireResource("com.bugsio.confluence.plugins.domain-booster:domain-booster-site-resources");			

		// get settings matching requested server name
		SiteSettings serverSettings = settingsManager.getServerSettingsForHostName(requestedServer);
		
		// handling if server name is unknown
		if(serverSettings == null) {
			
			// first check if there's site handing requested space
			String requestedSpace = getSpaceNameFromRequest(servletPath, pathInfo);
			if(requestedSpace != null) {
				serverSettings = settingsManager.getServerSettingsForSpace(requestedSpace);
				if(serverSettings != null) {
					log.debug("Access for space {} document redirecting to {}", requestedSpace, serverSettings.getHostName());
					response.sendRedirect(getRedirectToServer(request, serverSettings, true));
					return;					
				}
			}
			
			String unknownHostRedirect = settingsManager.getDomainBoosterSettings().getUnknownHostnameRedirect();
			
			// prevent redirection to the same host
			try {
				URI uri = new URI(unknownHostRedirect);
				if(uri.getHost().equals(requestedServer)) {
					unknownHostRedirect = null;
					//log.debug("Preventing redirection for unknown host");
				}
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			
			// if no redirection rule specified, just handle the request
			if(StringUtils.isBlank(unknownHostRedirect)) {
				processRequest(request, response, chain, settingsManager, null, false, anonUser);
				return;			
			}
			else {
				log.debug("Request for unknown server {} will be redirected to {}", requestedServer, unknownHostRedirect);
			}
				
			// if redirect rule matches known hostname, redirect to it
			serverSettings = settingsManager.getServerSettingsForHostName(unknownHostRedirect);
			if(serverSettings != null) {
				response.sendRedirect(getRedirectToServer(request, serverSettings, true));
				return;
			}
			// allow special value "404"
			if("404".equals(unknownHostRedirect)) {
				response.sendError(404);
				return;
			}
			// finally, just redirect to 
			response.sendRedirect(unknownHostRedirect);
			return;			
		}
		DomainBoosterContextHolder.setSiteSettings(serverSettings);

		// check if found aliased that needs redirection
		if(serverSettings.isSendAliasedToMain() && !requestedServer.equals(serverSettings.getHostName())) {
			log.debug("Access to aliased hostname {} should be forwarded to {}", requestedServer, serverSettings.getHostName());
			response.sendRedirect(getRedirectToServer(request, serverSettings, true));
			return;
		}
		
		// access to bare domain name?
		if(servletPath == null || servletPath.length() == 0) {
			String targetUrl = getRedirectToServer(request, serverSettings, false);
			log.debug("Access to server root. Redirecting user to: {}", targetUrl);
			response.sendRedirect(targetUrl);
			return;			
		}

		// add meta with site settings
		addMetadata(serverSettings);
		
		if("GET".equals(request.getMethod())) {
			
			// don't filter "special" paths 
			// e.g. for downloads we have no clue about belonging space (without further querying) so always pass through
			if(isIgnoredContentPath(servletPath)) {
				// download paths look like: /confluence/download/attachments/1179650/filename.zip
				processRequest(request, response, chain, settingsManager, serverSettings, false, anonUser); // process downloads without filtering
				return;
			}
			
			String requestedSpace = getSpaceNameFromRequest(servletPath, pathInfo);
			if(requestedSpace != null) {
				
				// requested user space?
				if(requestedSpace.startsWith("~")) {
					if(anonUser && !serverSettings.isAnonAllowedUserProfileAccess()) {
						sendUnauthorized(request, response, serverSettings);
						return;
					}
				}
				// requested content space
				else {
					
					DomainBoosterContextHolder.setRequestedSpace(requestedSpace);
					
					// get server handling the Space
					SiteSettings spaceServerSettings = settingsManager.getServerSettingsForSpace(requestedSpace);
					if(spaceServerSettings != null) {
						if(!requestedServer.equals(spaceServerSettings.getHostName())) {
							log.debug("Requested server {} doesn't match space {}.", requestedServer, requestedSpace);
							
							// redirect to server handling requested space?
							if(serverSettings.isKnownSpaceForwardAllowed()) {
								String targetUrl = getRedirectToServer(request, spaceServerSettings, false);
								log.debug("Send permanent redirect to: {}", targetUrl);
								response.setHeader("Location", targetUrl);
								response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY); // using 301 redirect to avoid losing SEO
								return;
							}
							
							// if handling of known space is not allowed, follow rule for unknown space
							if(!serverSettings.isUnknownSpaceHandleAllowed()) {
								response.sendError(HttpServletResponse.SC_NOT_FOUND);
								return;
							}
						}					
					}
					else {
						if(!serverSettings.isUnknownSpaceHandleAllowed()) {
							response.sendError(HttpServletResponse.SC_NOT_FOUND);
							return;
						}						
						// TODO what's Confl behavior on non-existing space? If it shows list of spaces maybe we could just return 404 or redirect somewhere else? 
						//log.debug("No server handling space {}. Just pass through.");
					}
				}
			}
			else {

				// check forbidden paths
				if(anonUser) {
					if(serverSettings.isUseGlobalAnonForbiddenPaths() && settingsManager.isPathForbiddenForAnon(servletPath)) {
						//String targetUrl = getRedirectToServer(request, serverSettings, false);
						log.debug("Path not allowed for anon user: {}", servletPath);
						
						// Homepage action and Dashboard are special cases - send anon user to the default space 
						if(servletPath.equals("/homepage.action") || servletPath.equals("/dashboard.action"))
							response.sendRedirect(getRedirectToServer(request, serverSettings, false));
						else // in all other cases send user to login page
							sendUnauthorized(request, response, serverSettings);
						
						return;			
					}
				}
				else {
					// TODO limit for signed in users?
					
				}
				
			}
		}

		//log.debug("Not filtering {} request to {}", request.getMethod(), servletPath);
		processRequest(request, response, chain, settingsManager, serverSettings, true, anonUser);
	}
	
	private String getSpaceNameFromRequest(String servletPath, String pathInfo) {
		if(servletPath.equals("/display") && pathInfo != null && pathInfo.length() > 1) {
			// get requested Space from path
			String requestedSpace = pathInfo.substring(1); // skip '/' at start
			int spaceEndInd = requestedSpace.indexOf('/');
			if(spaceEndInd != -1)
				requestedSpace = requestedSpace.substring(0, spaceEndInd); 
			return requestedSpace;
		}
		return null;
	}
	
	private boolean isIgnoredContentPath(String servletPath) {
		for(String path: IGNORED_PATHS)
			if(servletPath.startsWith(path))
				return true;
		return false;
	}
	
	private boolean isSearchRequest(String servletPath) {
		return (servletPath != null && servletPath.contains("search"));
	}
	
	private void sendUnauthorized(HttpServletRequest request, HttpServletResponse response, SiteSettings ss) throws IOException {
		
		StringBuilder buf = new StringBuilder();
		buf.append(request.getServletPath());
		if(request.getPathInfo() != null)
			buf.append(request.getPathInfo());
		if(request.getQueryString() != null)
			buf.append("?").append(request.getQueryString());
		String redirectUri = buf.toString();
		
		String url = getRedirectionBase(request, ss);
		url += "/login.action?os_destination=" + URLEncoder.encode(redirectUri, "UTF8");

		log.debug("Send user to: {}", url);
		
		// using simple redirect as setting SC_UNAUTHORIZED show Tomcat's ugly page instead to show the login page
		response.sendRedirect(url);
	}
	
	private void processRequest(HttpServletRequest request, HttpServletResponse response, FilterChain chain, 
			DomainBoosterSettingsManager settingsManager, final SiteSettings serverSettings, 
			boolean filterResponseContent, boolean anonUser) 
			throws IOException, ServletException {
		
		HttpServletRequest newRequest = request;
		if(serverSettings != null) {
			
			newRequest = new HttpServletRequestWrapper(newRequest) {
				
				@Override
				public String getServerName() {
					String serverName = serverSettings.getHostName();
					log.debug("Requested server name. Current: {}, Returning: {}", super.getServerName(), serverName);
					return serverName;
				}
				
				@Override
				public int getServerPort() {
					int port = super.getServerPort(); 
					if(serverSettings.getProxyPort() > 0) {
						log.debug("Return server port {} instead of default {}", serverSettings.getProxyPort(), super.getServerPort());
						port = serverSettings.getProxyPort();
					}
					else {
						log.debug("Return default port {}", port);
					}
					return port;
				}
			};
			
			if("POST".equals(newRequest.getMethod()) || !StringUtils.isBlank(newRequest.getQueryString())) {
				if(serverSettings.isLimitSearchToSite() && isSearchRequest(newRequest.getServletPath())) {
					log.debug("Wrap request with search params modifier");					
					newRequest = new SearchParamsRequestModifier(newRequest, serverSettings, settingsManager.getDomainBoosterSettings());
				}
			}
		}
		
		HttpServletResponse newResponse = response;
		PageCapturingResponseWrapper pageCapturingResponseWrapper = null;
		if(filterResponseContent) {
			newResponse = pageCapturingResponseWrapper = new PageCapturingResponseWrapper(newResponse); 
		}
		
		chain.doFilter(newRequest, newResponse);

		log.debug("Has page captured: {}", pageCapturingResponseWrapper != null && pageCapturingResponseWrapper.isContentCaptured());
		
		if(pageCapturingResponseWrapper != null && pageCapturingResponseWrapper.isContentCaptured()) {
			String contentType = pageCapturingResponseWrapper.getContentType();
			String text = newResponse.toString();
			if (text != null) {
				
				if(isContentTypeAllowedForFiltering(contentType)) {
				
					DomainBoosterSettings settings = settingsManager.getDomainBoosterSettings();
					
					String ssScript = null;
					String insertAtEndOfBody = settings.getInsertAtEndOfBody();
					
					if(serverSettings != null) {
						ssScript = settingsManager.getSettingsJS(serverSettings, anonUser);
					}
					
					log.debug("************* serverSettings={}", serverSettings);
					log.debug("************* ssScript={}", ssScript);
					
					if(StringUtils.isNotBlank(ssScript) || StringUtils.isNotBlank(insertAtEndOfBody)) 
						text = StringUtils.replace(text, "</html>", // this is actually end of page!
								String.format("%s%s</html>", ssScript != null ? ssScript : "", insertAtEndOfBody));
					
					if(serverSettings != null && serverSettings.getTextReplacementRules() != null) {
						for(FindReplaceRule fr: serverSettings.getTextReplacementRules()) {
							DomainBoosterContextHolder.addSearchReplaceRule(fr.getF(), fr.getR());
						}
					}
					
					text = StringUtils.replaceEach(text,
							DomainBoosterContextHolder.getSearchRules(),
							DomainBoosterContextHolder.getReplaceRules()
						);
				}
				response.getWriter().write(text);
			}
		}
	}
	
	private boolean addMetadata(SiteSettings siteSettings) {
		try {
			PluginUtilsLite.invokeMethodWithKnownParamTypes(webResourceManager, 
					"putMetadata", putMetadataParamTypes, 
					"domain-booster-host", siteSettings.getHostName());
			List<FindReplaceRule> rules = siteSettings.getTextReplacementRules();
			String rulePrefix = "meta-ajs:";
			if(rules != null) {
				for(FindReplaceRule rule: rules) {
					if(rule.getF().startsWith(rulePrefix) && rule.getF().length() > rulePrefix.length()) {
						PluginUtilsLite.invokeMethodWithKnownParamTypes(webResourceManager, 
								"putMetadata", putMetadataParamTypes, 
								"domain-booster-" + rule.getF().substring(rulePrefix.length()), rule.getR());						
					}
				}
			}
		}
		catch(Throwable e) {
			log.warn("Error adding metadata", e);
			return false;
		}
		return true;
	}
	
	private boolean isContentTypeAllowedForFiltering(String type) {
		if(type == null)
			return false;
		return type.startsWith("text/html") || type.startsWith("application/json");
	}
	
	private static String getRedirectionBase(HttpServletRequest request, SiteSettings ss) {
		StringBuilder buf = new StringBuilder();
		buf.append(ss.getProxyScheme() != null ? ss.getProxyScheme() : request.getScheme()).append("://");
		buf.append(ss.getHostName());
		if(ss.getProxyPort() > 0 && ss.getProxyPort() != 80 && ss.getProxyPort() != 443)
			buf.append(":").append(ss.getProxyPort());
		else if(request.getServerPort() != 80 && request.getServerPort() != 443)
			buf.append(":").append(request.getServerPort());
		buf.append(request.getContextPath());
		return buf.toString();
	}
	
	private static String getRedirectToServer(HttpServletRequest request, SiteSettings ss, boolean keepPath) {
		
		StringBuilder buf = new StringBuilder();
		buf.append(getRedirectionBase(request, ss));
		
		if(keepPath) {
			buf.append(request.getServletPath());
			if(request.getPathInfo() != null)
				buf.append(request.getPathInfo());
			if(request.getQueryString() != null)
				buf.append("?").append(request.getQueryString());
		}
		else {
			buf.append(DISPLAY_PREFIX);
			buf.append(ss.getDefaultSpace());
		}
		
		return buf.toString();
	}
}

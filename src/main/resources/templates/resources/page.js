AJS.toInit(function () {
	//alert('in page.js: ' + window.dbSiteSettingsStr);
	
	if(window.dbSiteSettingsStr) {
		var ss = (new Function("return " + window.dbSiteSettingsStr))();
		var space = AJS.Meta.get("space-key");
		var extraClass = '';
		
		if(ss.pageClass) extraClass += ss.pageClass;
		if(space)
			extraClass += ' dbp-space-' + space;		
		if(extraClass.length > 0)
			AJS.$('body').addClass(extraClass);
		
		if(ss.footer) AJS.$('#footer').html(ss.footer);

		if(ss.dashboardLinkText)
			AJS.$('#breadcrumbs li:first a').html(ss.dashboardLinkText);
		if(ss.dashboardLinkUrl)
			AJS.$('#breadcrumbs li:first a').attr('href', ss.dashboardLinkUrl).removeAttr('title');
		if(ss.hideDashboard) {		
			AJS.$('#breadcrumbs li:first').remove();
			//AJS.$('#breadcrumbs li:first').addClass('first');
		}
		
		if(ss.hideSpaceInBreadcrumb && AJS.params.spaceKey) {
			AJS.$('#breadcrumbs a[href="' + AJS.params.contextPath + "/display/" + AJS.params.spaceKey + '"]').parents('li').remove();
		}
		
		if(ss.expandBreadcrumb) {
			AJS.$('#breadcrumbs #ellipsis').remove();
			AJS.$('#breadcrumbs li').removeClass('hidden-crumb');			
		}
		
		//if(ss.hideDashboard || ss.hideSpaceInBreadcrumb) 
		{
			AJS.$('#breadcrumbs li').removeClass('first');
			AJS.$('#breadcrumbs li:first').addClass('first');
		}		
	}
});

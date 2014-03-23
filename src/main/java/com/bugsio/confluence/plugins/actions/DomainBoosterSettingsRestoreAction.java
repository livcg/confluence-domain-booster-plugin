package com.bugsio.confluence.plugins.actions;

import java.io.File;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.xwork.FileUploadUtils;
import com.bugsio.confluence.plugins.DomainBoosterSettingsManager;


public class DomainBoosterSettingsRestoreAction extends ConfluenceActionSupport {
	private static final long serialVersionUID = 1L;
	
	private DomainBoosterSettingsManager domainBoosterSettingsManager;
    
	
    public String doDefault()
    {
    	File file = null;
		try {
			file = getRestoreFileFromUpload();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	if(file != null) {
    		try {
	    		domainBoosterSettingsManager.restoreDomainBoosterSettings(file);
	    		
	        	getSession().put("domain-booster-message", "Successfully restored Domain Booster configuration.");
    		}
    		catch(Exception e) {
    	    	addActionError("Error restoring settings. " + e.getMessage());
    	    	getSession().put("domain-booster-error", "Error restoring Domain Booster configuration. " + e.getMessage());
    	        return ERROR;
    		}
    	}
        return SUCCESS;
    }
    
    protected File getRestoreFileFromUpload() throws Exception
    {
        try
        {
            File file = FileUploadUtils.getSingleFile();

            if (file == null) {
            	getSession().put("domain-booster-error", "No files uploaded.");
            	addActionError("No files uploaded.");
                throw new Exception("No files uploaded.");
            }

            return file;
        }
        catch (FileUploadUtils.FileUploadException e)
        {
            String[] errors = e.getErrors();

            for (int i = 0; i < errors.length; i++)
            {
            	getSession().put("domain-booster-error", e.getErrors()[i]);
                addActionError(e.getErrors()[i]);
            }

            throw new Exception("Error uploading file.");
        }
    }

	public void setDomainBoosterSettingsManager(DomainBoosterSettingsManager domainBoosterSettingsManager) {
		this.domainBoosterSettingsManager = domainBoosterSettingsManager;
	}     
    
}

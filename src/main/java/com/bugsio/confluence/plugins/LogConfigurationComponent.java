package com.bugsio.confluence.plugins;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class LogConfigurationComponent {
	private static final Logger LOG = Logger.getLogger(LogConfigurationComponent.class);
	 
	public LogConfigurationComponent() {
		Level level = Level.toLevel("DEBUG");
		changeLevel(level, Logger.getLogger("com.bugsio"));
	}
	
	private void changeLevel(Level level, Logger logger) {
		Level oldLevel = logger.getLevel();
		logger.setLevel(level);
 
		String message = "Changing loglevel for logger " + logger.getName() + 
			" from " + oldLevel + " to " + level.toString();
 
		LOG.info(message);			
	}
}
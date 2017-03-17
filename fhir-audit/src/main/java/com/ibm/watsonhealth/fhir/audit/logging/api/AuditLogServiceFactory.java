/**
 * (C) Copyright IBM Corp. 2016,2017,2018,2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.watsonhealth.fhir.audit.logging.api;

import static com.ibm.watsonhealth.fhir.config.FHIRConfiguration.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.watsonhealth.fhir.audit.logging.impl.DisabledAuditLogService;
import com.ibm.watsonhealth.fhir.audit.logging.impl.LoggerAuditLogService;
import com.ibm.watsonhealth.fhir.config.FHIRConfiguration;

/**
 * Instantiates and returns an implementation of the FHIR server audit log service.
 * @author markd
 *
 */
public class AuditLogServiceFactory {
	
	private static final Logger log = java.util.logging.Logger.getLogger(AuditLogServiceFactory.class.getName());
	private static final String CLASSNAME = AuditLogServiceFactory.class.getName();
	
	private static final String AUDIT_LOG_FILENAME_PREFIX = "fhiraudit.";
	private static final String AUDIT_LOG_FILENAME_SUFFIX = ".log";
	private static final long AUDIT_LOG_MAX_SIZE_DEFAULT = 20;
	private static AuditLogService serviceInstance = null;

	
	/**
	 * Returns the AuditLogService to be used by all FHIR server components.
	 * @return AuditLogService
	 */
	public static AuditLogService getService() {
		
		if (serviceInstance == null) {
			buildLoggerAuditLogService();
		}
		return serviceInstance;
	}
	
	/**
	 * Nulls out the singleton instance of the audit logger service object that is cached by this factory class, 
	 * then creates, caches, and returns a new service object instance.
	 * @return AuditLogService - The newly cached audit log service object.
	 */
	public static AuditLogService resetService() {
		final String METHODNAME = "resetService";
		log.entering(CLASSNAME, METHODNAME);
		
		serviceInstance = null;
		AuditLogService newService = getService();
		log.exiting(CLASSNAME, METHODNAME);
		return newService;
	}
	
	/**
	 * Creates an audit log service that uses the java.util.logging package to record audit log entries.
	 * @return AuditLogService - An audit log service that delegates to java.util.logging.
	 */
	private static AuditLogService getLoggerAuditLogService(String auditLogFilePath, long maxAuditLogSize) {
		final String METHODNAME = "getLoggerAuditLogService";
		log.entering(CLASSNAME, METHODNAME);
				
		Logger javaLogger = null;
		FileHandler auditLogFileHandler = null;
					 
		try {
			// Create a unique Logger instance for audit logging, using a custom FileHandler and Formatter.
			javaLogger = Logger.getLogger(auditLogFilePath);
			javaLogger.setLevel(Level.INFO);
			javaLogger.setUseParentHandlers(false);
			auditLogFileHandler = new FileHandler(auditLogFilePath,true);
			auditLogFileHandler.setFormatter(new LoggerAuditLogService.LoggerFormatter());
			javaLogger.addHandler(auditLogFileHandler);
			 
			serviceInstance = new LoggerAuditLogService(javaLogger, maxAuditLogSize);
			log.info("Initialized Audit logging to file: " + auditLogFilePath);
		}
		catch (Throwable e) {
			log.severe("Failure creating LoggerAuditLog: " + e.getMessage());
			serviceInstance = new LoggerAuditLogService();
		}  
		 		    
		log.exiting(CLASSNAME, METHODNAME);
		return serviceInstance;
	}
	
	/**
	 * Builds and returns a singleton LoggerAuditLogService implementation object that will be cached by this factory class.
	 * @return AuditLogService - A LoggerAuditLogService implementation.
	 */
	private static synchronized AuditLogService buildLoggerAuditLogService() {
		final String METHODNAME = "buildLoggerAuditLogService";
		log.entering(CLASSNAME, METHODNAME);
		
		String auditLogPath;
		long maxAuditLogSize;
		if (serviceInstance == null) {
			auditLogPath = buildAuditLogPath();
			maxAuditLogSize = acquireMaxAuditLogSize();
			if (!auditLogPath.isEmpty()) {
				serviceInstance = getLoggerAuditLogService(auditLogPath, maxAuditLogSize);
			}
			else {
				serviceInstance = new DisabledAuditLogService();
			}
		}
		log.exiting(CLASSNAME, METHODNAME);
		return serviceInstance;
	}
	
	/**
	 * Builds and returns the complete path name of the audit log file to be used by a file-based audit log service.
	 * @return String the audit log file path.
	 */
	private static String buildAuditLogPath() {
		final String METHODNAME = "buildAuditLogPath";
		log.entering(CLASSNAME, METHODNAME);
		
		String currentDateTime;
		String auditLogDir = null;
		StringBuilder auditLogPath = new StringBuilder();
		SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("YYYYMMddHHmmssSSS");
		currentDateTime = dateTimeFormatter.format(new Date(System.currentTimeMillis()));
		String fileSeparator = System.getProperty("file.separator");
		
		try {
		    auditLogDir = FHIRConfiguration.getInstance().loadConfiguration().getStringProperty(PROPERTY_AUDIT_LOGPATH);
		}
		catch(Throwable t) {
			// Use the JVM default temp directory. This should only end up being used when running in junit or testNg mode.
			log.fine("Could not obtain audit log path via configuration.");
		}
		
		if (auditLogDir != null && !auditLogDir.isEmpty()) {
			if (!auditLogDir.endsWith(fileSeparator)) {
				auditLogDir = auditLogDir + fileSeparator;
			}
			// Construct the full path name.
			auditLogPath.append(auditLogDir)
						.append(AUDIT_LOG_FILENAME_PREFIX)
						.append(currentDateTime)
						.append(AUDIT_LOG_FILENAME_SUFFIX);
		}
		 
		log.exiting(CLASSNAME, METHODNAME);
		return auditLogPath.toString();
	}
	
	/**
	 * Acquire the maximum audit log size via a JNDI lookup. If the max log size is not specified or is not a valid
	 * integer, return a default value.
	 * @return long - The maximum audit log size, in bytes.
	 */
	private static long acquireMaxAuditLogSize() {
		final String METHODNAME = "acquireMaxAuditLogSize";
		log.entering(CLASSNAME, METHODNAME);
		
		final long MB = 1024 * 1024;
		long maxAuditLogSize = AUDIT_LOG_MAX_SIZE_DEFAULT * MB;
		int maxAuditLogMB;
		
		try {
			maxAuditLogMB = FHIRConfiguration.getInstance().loadConfiguration().getIntProperty(PROPERTY_AUDIT_LOG_MAXSIZE);
			maxAuditLogSize = maxAuditLogMB * MB;
		}
		catch(Throwable t) {
			log.fine("Could not obtain audit log max file size via configuration.");
		}
		
		
		log.exiting(CLASSNAME, METHODNAME, maxAuditLogSize);
		return maxAuditLogSize;
	}

}

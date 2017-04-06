/**
 * (C) Copyright IBM Corp. 2017,2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.watsonhealth.fhir.persistence.jdbc.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import com.ibm.watsonhealth.fhir.persistence.exception.FHIRPersistenceException;
import com.ibm.watsonhealth.fhir.persistence.jdbc.dao.api.ResourceDAO;
import com.ibm.watsonhealth.fhir.persistence.jdbc.dto.Resource;
import com.ibm.watsonhealth.fhir.persistence.jdbc.exception.FHIRPersistenceDBConnectException;
import com.ibm.watsonhealth.fhir.persistence.jdbc.exception.FHIRPersistenceDataAccessException;

/**
 * This Data Access Object class provides methods creating, updating, and retrieving rows in the FHIR Resource table.
 * @author markd
 *
 */
public class ResourceDAOBasicImpl extends FHIRDbDAOBasicImpl<Resource> implements ResourceDAO {
	
	private static final Logger log = Logger.getLogger(ResourceDAOBasicImpl.class.getName());
	private static final String CLASSNAME = ResourceDAOBasicImpl.class.getName(); 
	
	private static final String SQL_INSERT = "INSERT INTO RESOURCE (DATA, LAST_UPDATED, LOGICAL_ID, RESOURCE_TYPE, VERSION_ID)" +
											" VALUES(?,?,?,?,?)";
	
	private static final String SQL_READ = "SELECT DATA, ID, LAST_UPDATED, LOGICAL_ID, RESOURCE_TYPE, VERSION_ID FROM RESOURCE R WHERE R.LOGICAL_ID = ? AND R.RESOURCE_TYPE = ? AND R.VERSION_ID = " +
			                                 "(SELECT MAX(R2.VERSION_ID) FROM Resource R2 WHERE R2.LOGICAL_ID = ? AND R2.RESOURCE_TYPE = ?)";
	
	private static final String SQL_VERSION_READ = "SELECT DATA, ID, LAST_UPDATED, LOGICAL_ID, RESOURCE_TYPE, VERSION_ID FROM RESOURCE R WHERE R.LOGICAL_ID = ? AND R.RESOURCE_TYPE = ? AND R.VERSION_ID = ?";
	
	private static final String SQL_SEARCH_BY_ID = "SELECT DATA, ID, LAST_UPDATED, LOGICAL_ID, RESOURCE_TYPE, VERSION_ID FROM RESOURCE R WHERE R.ID IN ";
	
	private static final  String SQL_HISTORY = "SELECT DATA, ID, LAST_UPDATED, LOGICAL_ID, RESOURCE_TYPE, VERSION_ID FROM RESOURCE R WHERE R.LOGICAL_ID = ? "
															+ "ORDER BY R.VERSION_ID DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
	
	private static final  String SQL_HISTORY_COUNT = "SELECT COUNT(*) FROM RESOURCE R WHERE R.LOGICAL_ID = ?";
	
	private static final  String SQL_HISTORY_FROM_DATETIME = "SELECT DATA, ID, LAST_UPDATED, LOGICAL_ID, RESOURCE_TYPE, VERSION_ID FROM RESOURCE R WHERE R.LOGICAL_ID = ? AND R.LAST_UPDATED >= ?" + 
															 " ORDER BY R.VERSION_ID DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
	private static final  String SQL_HISTORY_FROM_DATETIME_COUNT = "SELECT COUNT(*) FROM RESOURCE R WHERE R.LOGICAL_ID = ? AND R.LAST_UPDATED >= ?";
	
	
	
	
	/**
	 * Constructs a DAO instance suitable for acquiring connections from a JDBC Datasource object.
	 */
	public ResourceDAOBasicImpl() {
		super();
	}
	
	/**
	 * Constructs a DAO instance suitable for acquiring connections based on the passed database type specific properties.
	 * @param dbProperties
	 */
	public ResourceDAOBasicImpl(Properties dbProperties) {
		super(dbProperties);
	}
	
	/**
	 * Creates and returns a Resource DTO based on the contents of the passed ResultSet
	 * @param resultSet A ResultSet containing FHIR persistent object data.
	 * @return Resource - A Resource DTO
	 * @throws FHIRPersistenceDataAccessException
	 */
	protected Resource createDTO(ResultSet resultSet) throws FHIRPersistenceDataAccessException {
		final String METHODNAME = "createDTO";
		log.entering(CLASSNAME, METHODNAME);
		
		Resource resource = new Resource();
		
		try {
			resource.setData(resultSet.getBytes("DATA"));
			resource.setId(resultSet.getLong("ID"));
			resource.setLastUpdated(resultSet.getTimestamp("LAST_UPDATED"));
			resource.setLogicalId(resultSet.getString("LOGICAL_ID"));
			resource.setResourceType(resultSet.getString("RESOURCE_TYPE"));
			resource.setVersionId(resultSet.getInt("VERSION_ID"));
		}
		catch (Throwable e) {
			throw new FHIRPersistenceDataAccessException("Failure creating Resource DTO.", e);
		}
		finally {
			log.exiting(CLASSNAME, METHODNAME);
		}
		
		return resource;
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.watsonhealth.fhir.persistence.jdbc.dao.impl.ResourceDAO#insert(com.ibm.watsonhealth.fhir.persistence.jdbc.dto.Resource)
	 */
	@Override
	public Resource insert(Resource resource) throws FHIRPersistenceDataAccessException, FHIRPersistenceDBConnectException {
		final String METHODNAME = "insert";
		log.entering(CLASSNAME, METHODNAME);
		
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
				
		try {
			connection = this.getConnection();
			stmt = connection.prepareStatement(SQL_INSERT, PreparedStatement.RETURN_GENERATED_KEYS);
			stmt.setBytes(1, resource.getData());
			stmt.setTimestamp(2, resource.getLastUpdated());
			stmt.setString(3, resource.getLogicalId());
			stmt.setString(4, resource.getResourceType());
			stmt.setInt(5,  resource.getVersionId());
					
			stmt.executeUpdate();
			resultSet = stmt.getGeneratedKeys();
			if (resultSet.next()) {
				resource.setId(resultSet.getLong(1));
				log.fine("Succesfully inserted Resource. id=" + resource.getId());
			}
			else
			{
				throw new FHIRPersistenceDataAccessException("Generated key not returned after new Resource inserted.");
			}
		}
		catch(FHIRPersistenceDataAccessException | FHIRPersistenceDBConnectException e) {
			throw e;
		}
		catch(Throwable e) {
			throw new FHIRPersistenceDataAccessException("Failure inserting Resource.", e);
		}
		finally {
			this.cleanup(resultSet, stmt, connection);
			log.exiting(CLASSNAME, METHODNAME);
		}
		
		return resource;
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.watsonhealth.fhir.persistence.jdbc.dao.impl.ResourceDAO#read(java.lang.String, java.lang.String)
	 */
	@Override
	public Resource read(String logicalId, String resourceType) throws FHIRPersistenceDataAccessException, FHIRPersistenceDBConnectException {
		final String METHODNAME = "read";
		log.entering(CLASSNAME, METHODNAME);
		
		Resource resource = null;
		List<Resource> resources;
				
		try {
			resources = this.runQuery(SQL_READ, logicalId, resourceType, logicalId, resourceType);
			if (!resources.isEmpty()) {
				resource = resources.get(0);
			}
		}
		finally {
			log.exiting(CLASSNAME, METHODNAME);
		}
				
		return resource;
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.watsonhealth.fhir.persistence.jdbc.dao.impl.ResourceDAO#versionRead(java.lang.String, java.lang.String, int)
	 */
	@Override
	public Resource versionRead(String logicalId, String resourceType, int versionId) throws FHIRPersistenceDataAccessException, FHIRPersistenceDBConnectException {
		final String METHODNAME = "versionRead";
		log.entering(CLASSNAME, METHODNAME);
		
		Resource resource = null;
		List<Resource> resources;
		Object[] selectParms = new Object[] {logicalId, resourceType, versionId};
		
		try {
			resources = this.runQuery(SQL_VERSION_READ, selectParms);
			if (!resources.isEmpty()) {
				resource = resources.get(0);
			}
		}
		finally {
			log.exiting(CLASSNAME, METHODNAME);
		}
				
		return resource;
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.watsonhealth.fhir.persistence.jdbc.dao.impl.ResourceDAO#search(java.lang.String)
	 */
	@Override
	public List<Resource> search(String sqlSelect) throws FHIRPersistenceDataAccessException, FHIRPersistenceDBConnectException {
		final String METHODNAME = "search";
		log.entering(CLASSNAME, METHODNAME);
		
		List<Resource> resources;
				
		try {
			resources = this.runQuery(sqlSelect);
		}
		finally {
			log.exiting(CLASSNAME, METHODNAME);
		}
				
		return resources;
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.watsonhealth.fhir.persistence.jdbc.dao.impl.ResourceDAO#searchForIds(java.lang.String)
	 */
	@Override
	public List<Long> searchForIds(String sqlSelect) throws FHIRPersistenceDataAccessException, FHIRPersistenceDBConnectException 
	{
		final String METHODNAME = "searchForIds";
		log.entering(CLASSNAME, METHODNAME);
		
		List<Long> resourceIds = new ArrayList<>();
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		String errMsg;
		
		try {
			connection = this.getConnection();
			stmt = connection.prepareStatement(sqlSelect);
			resultSet = stmt.executeQuery();
			while(resultSet.next())	 {
				resourceIds.add(resultSet.getLong(1));
			}
		}
		catch(FHIRPersistenceException e) {
			throw e;
		}
		catch (Throwable e) {
			errMsg = "Failure retrieving FHIR Resource Ids. SQL=" + sqlSelect;
			throw new FHIRPersistenceDataAccessException(errMsg,e);
		} 
		finally {
			this.cleanup(resultSet, stmt, connection);
			log.exiting(CLASSNAME, METHODNAME);
		}
		return resourceIds;
		
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.watsonhealth.fhir.persistence.jdbc.dao.impl.ResourceDAO#searchByIds(java.util.List)
	 */
	@Override
	public List<Resource> searchByIds(List<Long> resourceIds) throws FHIRPersistenceDataAccessException, FHIRPersistenceDBConnectException  {
		final String METHODNAME = "searchByIds";
		log.entering(CLASSNAME, METHODNAME);
		
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		String errMsg;
		StringBuilder idQuery = new StringBuilder();
		List<Resource> resources = new ArrayList<>();
		
		try {
			idQuery.append(SQL_SEARCH_BY_ID);
			idQuery.append("(");
			for (int i = 0; i < resourceIds.size(); i++) {
				if (i > 0) {
					idQuery.append(",");
				}
				idQuery.append(resourceIds.get(i));
			}
			idQuery.append(")");
						
			connection = this.getConnection();
			stmt = connection.prepareStatement(idQuery.toString());
			resultSet = stmt.executeQuery();
			resources = this.createDTOs(resultSet);
		}
		catch(FHIRPersistenceException e) {
			throw e;
		}
		catch (Throwable e) {
			errMsg = "Failure retrieving FHIR Resources. SQL=" + idQuery;
			throw new FHIRPersistenceDataAccessException(errMsg,e);
		} 
		finally {
			this.cleanup(resultSet, stmt, connection);
			log.exiting(CLASSNAME, METHODNAME);
		}
		return resources;
		
		
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.watsonhealth.fhir.persistence.jdbc.dao.impl.ResourceDAO#searchCount(java.lang.String)
	 */
	@Override
	public int searchCount(String sqlSelectCount) throws FHIRPersistenceDataAccessException, FHIRPersistenceDBConnectException {
		final String METHODNAME = "searchCount";
		log.entering(CLASSNAME, METHODNAME);
		
		int count;
				
		try {
			count = this.runCountQuery(sqlSelectCount);
		}
		finally {
			log.exiting(CLASSNAME, METHODNAME);
		}
		return count;
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.watsonhealth.fhir.persistence.jdbc.dao.impl.ResourceDAO#history(java.lang.String, java.sql.Timestamp, int, int)
	 */
	@Override
	public List<Resource> history(String logicalId, Timestamp fromDateTime, int offset, int maxResults) 
									throws FHIRPersistenceDataAccessException, FHIRPersistenceDBConnectException {
		final String METHODNAME = "history";
		log.entering(CLASSNAME, METHODNAME);
		
		List<Resource> resources;
				
		try {
			if (fromDateTime != null) {
				resources = this.runQuery(this.getSqlHistoryFromDateTime(), logicalId, fromDateTime, offset, maxResults);
			}
			else {
				resources = this.runQuery(this.getSqlHistory(), logicalId, offset, maxResults);
			}
		}
		finally {
			log.exiting(CLASSNAME, METHODNAME);
		}
		return resources;
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.watsonhealth.fhir.persistence.jdbc.dao.impl.ResourceDAO#historyCount(java.lang.String, java.sql.Timestamp)
	 */
	@Override
	public int historyCount(String logicalId, Timestamp fromDateTime) throws FHIRPersistenceDataAccessException, FHIRPersistenceDBConnectException {
		final String METHODNAME = "historyCount";
		log.entering(CLASSNAME, METHODNAME);
		
		int count;
				
		try {
			if (fromDateTime != null) {
				count = this.runCountQuery(this.getSqlHistoryFromDateTimeCount(), logicalId, fromDateTime);
			}
			else {
				count = this.runCountQuery(this.getSqlHistoryCount(), logicalId);
			}
		}
		finally {
			log.exiting(CLASSNAME, METHODNAME);
		}
		return count;
	}

	protected String getSqlHistoryCount() {
		return SQL_HISTORY_COUNT;
	}
	
	protected String getSqlHistoryFromDateTimeCount() {
		return SQL_HISTORY_FROM_DATETIME_COUNT;
	}
	
	protected String getSqlHistory() {
		return SQL_HISTORY;
	}
	
	protected String getSqlHistoryFromDateTime() {
		return SQL_HISTORY_FROM_DATETIME;
	}
}
package com.moon.dctm.monitoring.sessmon;

import com.documentum.fc.common.DfException;

public interface IServer {

	/**
	 * Default monitoring interval.
	 * This default monitoring interval
	 * specified in milliseconds is used
	 * if no other interval is provided.
	 */
	int DEFAULT_MONITORING_INTERVAL = 60000;
	
	/**
	 * Returns server name
	 * @return server name.
	 */
	String getName();
	
	/**
	 * Returns server host
	 * name.
	 * @return host name
	 * where this server instance 
	 * is deployed.
	 */
	String getHost();
	
	/**
	 * Returns server proximity
	 * to the docbroker.
	 * @return server proximity.
	 */
	int getProximity();
	
	/**
	 * Returns the maximum number of
	 * concurrent sessions supported by
	 * the server.
	 * @see init() method should be called
	 * before executing this method.
	 * @return maximum session count.
	 */
	int getMaxSessionCount();
	
	/**
	 * Initializes server object. This method
	 * establishes connection with Documentum
	 * server instance.
	 * 
	 * @param userName user name
	 * @param userPassword user password
	 * @param userDomain user domain 
	 * @throws DfException if initialization fails due to DFC exception
	 */
	void init(String userName, String userPassword, String userDomain) throws DfException;
	
	/**
	 * Adds filter for session monitoring.
	 * This method replaces an existing filter with
	 * the same name.
	 * @param filterName filter name
	 * @param filterToAdd filter object
	 */
	void addFilter(String filterName, IFilter filterToAdd);
	
	/**
	 * Returns filter.
	 * @param filterName filter name.
	 * @return filter or null if filter is not found.
	 */
	IFilter getFilter(String filterName);
	
	/**
	 * Refreshes all added filters.
	 * This method inspectes all
	 * active sessions and updates
	 * the state of all filters.
	 */
	boolean updateFilters();
	
	/**
	 * Returns server property
	 * @param propertyName property name.
	 * @return property value.
	 */
	String getProperty(String propertyName);
	
	/**
	 * Starts session monitoring.
	 * This method starts monitoring
	 * sessions for the associated
	 * server instance.
	 * @param interval session polling interval
	 */
	void startMonitoring(int interval);
	
	/**
	 * Starts session monitoring.
	 * This method starts monitoring
	 * sessions for the associated
	 * server instance with the default interval.
	 * @see DEFAULT_MONITORING_INTERVAL
	 */
	void startMonitoring();
	
}

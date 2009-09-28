/**
 * 
 */
package com.moon.dctm.monitoring.sessmon.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfLoginInfo;
import com.moon.dctm.monitoring.sessmon.IFilter;
import com.moon.dctm.monitoring.sessmon.IServer;

/**
 * @author AFILIPPOV001
 *
 */
public class DocbaseServer implements IServer {

	String connectionString=null;
	String name=null;
	String host=null;
	int proximity;
	Map attributes=null;
	int maxSessionCount =-1;
	Map filters = null;
	IDfSessionManager sMgr  = null;
	
	final static String DQL_MAX_SESSION_COUNT="select concurrent_sessions from dm_server_config where object_name=";
	final static String DQL_ATTR_MAX_SESSION_COUNT="concurrent_sessions";
	final static String DQL_GET_SESSIONS="execute show_sessions";
			
	DocbaseServer(String docbaseName, String serverName, String serverHost, int serverProximity){
		this.name=serverName;
		this.host=serverHost;
		this.proximity = serverProximity;
		
		attributes = new HashMap();
		
		//Use linked hash map to store filters
		//to maintain the filter processing order.
		filters = new LinkedHashMap();
		
		//Build the connection string
		//for this server instance
		StringBuffer connName = new StringBuffer();
		connName.append(docbaseName);
		connName.append(".");
		connName.append(this.getName());
		connName.append("@");
		connName.append(this.getHost());
		
		connectionString = connName.toString();
	}
	
	/* (non-Javadoc)
	 * @see com.moon.dctm.monitoring.sessmon.IServer#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see com.moon.dctm.monitoring.sessmon.IServer#getHost()
	 */
	public String getHost() {
		return host;
	}

	/* (non-Javadoc)
	 * @see com.moon.dctm.monitoring.sessmon.IServer#getProximity()
	 */
	public int getProximity() {
		return proximity;
	}

	/* (non-Javadoc)
	 * @see com.moon.dctm.monitoring.sessmon.IServer#getProperty(java.lang.String)
	 */
	public String getProperty(String propertyName) {
		return (String)attributes.get(propertyName);
	}

	
	void setProperty(String propertyName, Object propertyValue) {
		attributes.put(propertyName,propertyValue);
	}

	public int getMaxSessionCount() {
		//Return the maximum session count.
		return maxSessionCount;
	}

	public void init(String userName, String userPassword, String userDomain) throws DfException
	{
		
		//create Client objects
		IDfClientX clientx = new DfClientX();
		IDfClient client = clientx.getLocalClient();
			
		//create a Session Manager object
		sMgr = client.newSessionManager();
			
		//create an IDfLoginInfo object named loginInfoObj
		IDfLoginInfo loginInfoObj = clientx.getLoginInfo();
		loginInfoObj.setUser(userName);
		loginInfoObj.setPassword(userPassword);
		loginInfoObj.setDomain(userDomain);
			
		//bind the Session Manager to the login info
		sMgr.setIdentity(connectionString, loginInfoObj);
		
		//find out the maximum session count
		maxSessionCount = retrieveMaxSessionCount();
	}

	public void addFilter(String filterName, IFilter filterToAdd) {
		//Add filter to the internal list
		filters.put(filterName,filterToAdd);
		
	}

	public IFilter getFilter(String filterName) {
		// Return filter based on filter name
		IFilter foundFilter = (IFilter)filters.get(filterName);
		return foundFilter;
	}

	public Collection getFilters() {
		//Return the list of filters
		return filters.values();
	}
	
	int retrieveMaxSessionCount() throws DfException
	{
		 IDfSession session = null;
		 IDfCollection colQuery = null;
		 int maxSessCount = 0;
		 
		 try {
			IDfClientX clientx = new DfClientX();
			 //Create query object
			 IDfQuery qrySessions = clientx.getQuery();
			 //Create the query
			 StringBuffer query = new StringBuffer();
			 query.append(DQL_MAX_SESSION_COUNT);
			 query.append("'");
			 query.append(this.getName());
			 query.append("'");
			 //Set the query to find out the max session count
			 qrySessions.setDQL(query.toString());
			 //Obtain DFC session
			 session = sMgr.getSession(connectionString);
			 //Execute the query and read the return value
			 colQuery = qrySessions.execute(session, IDfQuery.DF_READ_QUERY);
			 
			 if(colQuery.next()){
				 //Retrieve the maximum number of sessions
				 maxSessCount = colQuery.getTypedObject().getInt(DQL_ATTR_MAX_SESSION_COUNT);
			 }	
		}finally{
			//Release DFC resources
			try {
				if(colQuery!=null){
					colQuery.close();
				}
			} catch (DfException e) {
				//Ignore error on collection close
			}
			//Release the session
			if(session!=null){
				sMgr.release(session);
			}
		}
		
		return maxSessCount;
	}
	
	public boolean updateFilters()
	{

		 IDfSession session = null;
		 IDfCollection colQuery = null;
		 boolean isUpdateSuccess=false;
		 
		 try {
			IDfClientX clientx = new DfClientX();
			 //Create query object
			 IDfQuery qrySessions = clientx.getQuery();
			 //Set the query to get the list of sessions
			 qrySessions.setDQL(DQL_GET_SESSIONS);
			 //Obtain DFC session
			 session = sMgr.getSession(connectionString);
			 //Execute the query and read the return value
			 colQuery = qrySessions.execute(session, IDfQuery.DF_READ_QUERY);
			 
			 while(colQuery.next()){
				//Process the current object.
				 processFilters(colQuery.getTypedObject());		
			 }
			 //complete filter processing
			 completeFilters();
			 
			 isUpdateSuccess = true;
		}catch(DfException dfex){
			//Log the error and continue processing
			DfLogger.error(this,null,null,dfex);
		}finally{
			//Release DFC resources
			try {
				if(colQuery!=null){
					colQuery.close();
				}
			} catch (DfException e) {
				//Ignore error on collection close
			}
			//Release the session
			if(session!=null){
				sMgr.release(session);
			}
		}
	
		return isUpdateSuccess;
	}
	
	void processFilters(IDfTypedObject sessionObject)
	{
		//Obtain iterator for all server filters
		Iterator serverFilters = getFilters().iterator();
		//Process all server filters		
		while(serverFilters.hasNext()){
			IFilter currentFilter = (IFilter)serverFilters.next();
			try {
				currentFilter.processSession(sessionObject);
			} catch (DfException e) {
				//Log the error and continue processing
				DfLogger.error(this,null,null,e);
			}
		}
	}
	
	void completeFilters()
	{
		//Obtain iterator for all server filters
		Iterator serverFilters = getFilters().iterator();
		//Finish processing for all server filters		
		while(serverFilters.hasNext()){
			IFilter currentFilter = (IFilter)serverFilters.next();
			currentFilter.complete();			
			//Log filter state
			logFilter(currentFilter);
		}
	}
	
	void logFilter(IFilter filterToLog)
	{
		//Obtain iterator for all server filters
		if(DfLogger.isInfoEnabled(this)){
			
			//Construct the logging string
			StringBuffer debugMessage = new StringBuffer();
			debugMessage.append(connectionString);
			debugMessage.append(" ");
			debugMessage.append(filterToLog);
			//Log the filter state
			DfLogger.info(this,debugMessage.toString(),null,null);
		}
	}

	public void startMonitoring(int interval) {
		//Start a monitoring thread
		Inspector currInspector = new Inspector(this);
		new Thread(currInspector).start();		
	}

	public void startMonitoring() {
		startMonitoring(DEFAULT_MONITORING_INTERVAL);
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		//Construction the string representation
		//of the filter
		StringBuffer stringObject = new StringBuffer();
		stringObject.append("Name =");
		stringObject.append(getName());
		stringObject.append(" Host =");
		stringObject.append(getHost());		
		
		return stringObject.toString();
	}
}

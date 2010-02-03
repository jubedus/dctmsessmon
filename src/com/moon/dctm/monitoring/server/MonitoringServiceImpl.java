package com.moon.dctm.monitoring.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.moon.dctm.monitoring.client.MonitoringService;
import com.moon.dctm.monitoring.sessmon.IDocbase;
import com.moon.dctm.monitoring.sessmon.IDocbroker;
import com.moon.dctm.monitoring.sessmon.IFilter;
import com.moon.dctm.monitoring.sessmon.IServer;
import com.moon.dctm.monitoring.sessmon.Starter;
import com.moon.dctm.monitoring.sessmon.filters.ActiveSessionCounter;
import com.moon.dctm.monitoring.shared.DocbaseServer;
import com.moon.dctm.monitoring.shared.MultiServiceException;
import com.moon.dctm.monitoring.shared.ServiceException;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class MonitoringServiceImpl extends RemoteServiceServlet implements
		MonitoringService {

	/** Name of the session attribute that stores reference
	 * to the Docbroker.*/
	protected static final String ATTR_DOCBROKER = "sessmon_attr_docbroker";
	
	/** Name of the context parameter with the Docbase user name.
	 * This user does not need to have access to any of the
	 * objects stored in the Docbase.*/
	protected static final String PARAM_USER_NAME = "sess_mon_user";
	/** Name of the context parameter with the Docbase user password.
	 * Password should be encrypted using DFC utility for better
	 * protection.*/
	protected static final String PARAM_PASSWORD = "sess_mon_password";
	/** Name of the context parameter with the Docbase user domain.
	 * This parameter is necessary only if a domain is used for user
	 * authentication.*/
	protected static final String PARAM_DOMAIN = "sess_mon_domain";
	
	/** Name of the context parameter that controls emulation mode.
	* If this parameter is found and is equal to TRUE then
	* emulation mode will be enabled. Otherwise, application
	* will run in the production mode.*/
	protected static final String PARAM_EMULATOR = "emulator_mode";

	/** Name of the context parameter with the monitoring interval.
	* Value of this parameter controls how often docbase servers
	* are monitored. The value should be specified in seconds.
	* If parameter is not available or if it's less than 30 seconds
	* then the default monitoring interval of 30 seconds will be used.
	*/
	protected static final String PARAM_MONITOR_INTERVAL = "monitoring_interval";
	
	/**
	 * Default monitoring interval. This interval is used
	 * if monitoring interval is not configured or the configured value
	 * is less than the default value.
	 */
	protected static final int DEFAULT_MONITOR_INTERVAL = 30;
	
	/**
	 * Map of available Content Server instances.
	 */
	private Map<String,DocbaseServer> serversUI = null;
	

	/**
	 * Class level logger.
	 */
	private final Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Current monitoring interval 
	 */
	private int monitorInterval = -1;
	
	/**
	 * Entry point to the server implementation.
	 * This method returns an array of Docbase
	 * servers for a specific docbase name. If docbase
	 * name is null then servers for all accessible docbases
	 * will be returned. If test mode is enabled then
	 * a set of test data will be returned instead of querying
	 * docbases.
	 * @param docbaseName name of the docbase, which should be
	 * monitored. If null then all docbases will be monitored.
	 * @return an array of docbase servers.
	 * @throws ServiceException if there is a DFC problem with
	 * Docbroker or DFC client.
	 * @return MultiServiceException if there is a problem with one 
	 * or multiple Docbase servers.
	 */	
	public DocbaseServer[] getServers(String docbaseName) throws ServiceException, MultiServiceException {
		
		logger.trace("Method getServers()");
		
		//Check if test mode is enabled...
		if(isEmulatorEnabled()){
			//Return test data
			return getServersEmulator();			
		}else{
			//Get the list of docbrokers
			try {
				return getDocbaseServers(docbaseName);
			} catch (Exception e) {
				//Log the error message
				logger.error("DFC error has occurred.",e);
				//Convert the exception for RPC
				ServiceException excSrvc = new ServiceException(e);
				throw excSrvc;
			}
		}

	}
	
	/**
	 * Returns a list of Docbase servers.
	 * This method initiates session monitoring if
	 * it hasn't been started yet. It will return the list of
	 * docbase servers accessible throught the
	 * configured docbroker.
	 * @param docbaseName docbase name to monitor. If null then
	 * all docbases will be monitored.
	 * @return list of docbase servers.
	 * @throws DfException if a DFC client problem occurs
	 * @throws MultiServiceException If there is a problem
	 * with one or multiple docbase servers.
	 */
	protected DocbaseServer[] getDocbaseServers(String docbaseName) throws Exception, MultiServiceException {	
		
		logger.trace("Method getDocbaseServers()");
		
		//Check if monitoring has started already
		IDocbroker broker = (IDocbroker)this.getServletContext().getAttribute(ATTR_DOCBROKER);
		//Start monitoring if it hasn't been started yet
		if(broker==null){
			logger.trace("Launching session monitoring.");
			//Retrieve user credentials
			String userName = this.getServletContext().getInitParameter(PARAM_USER_NAME);
			String userPassword = this.getServletContext().getInitParameter(PARAM_PASSWORD);
			String userDomain = this.getServletContext().getInitParameter(PARAM_DOMAIN);
			
			//Initiate monitoring
			launch(userName,userPassword,userDomain);
		}else{
			logger.trace("Retrieving monitoring details.");
			//Update RPC objects
			refreshUIservers(broker);
		}
		
		//Return the list of RPC objects to the UI
		DocbaseServer[] ret =serversUI.values().toArray(new DocbaseServer[0]);		
		return ret;
	}
	
	
	/**
	 * Launches session monitoring.
	 * This method launches a separate
	 * session monitoring thread for
	 * every server instance. User credentials
	 * passed to this method are used to 
	 * get the list of DFC sessions.
	 * @param userName user name
	 * @param userPassword user password
	 * @param userDomain user domain
	 * @throws DfException if there is a problem with Docbroker of DFC client.
	 * @throws MultiServiceException if there is a problem
	 * with one or multiple Docbase servers.
	 */
	protected void launch(String userName, String userPassword, String userDomain) throws Exception, MultiServiceException
	{
		logger.trace("Method launch()");
		
		//Declare a docbroker variable
		IDocbroker broker = null;
		//Get a docbroker instance
		broker = Starter.getLocalDocbroker();
		
		//Initialize the list of UI servers
		serversUI = new LinkedHashMap<String, DocbaseServer>();
		
		//Iterate thru servers of all docbases
		List<IDocbase> docbases = broker.getDobases();
		for(IDocbase currDocbase : docbases){
			List<IServer> servers = currDocbase.getServers();
			for(IServer currServer: servers){
				DocbaseServer currUIserver = null;
				try {
					//Initialize the UI docbase server
					currUIserver = new DocbaseServer(currDocbase.getName(), currServer.getName(),currServer.getHost());
					//Add to the list of US
					serversUI.put(currUIserver.getID(), currUIserver);
					//Initialize the server
					currServer.init(userName,userPassword,userDomain);
					//Set the max session count
					currUIserver.setMaxSessCount(currServer.getMaxSessionCount());
					//Instantiate filter
					IFilter filterSessCount = new ActiveSessionCounter();
					//Add filter to the server
					currServer.addFilter("ActiveSessionCounter",filterSessCount);
					//Start monitoring
					currServer.startMonitoring(getMonitoringInterval());
					//currUIserver
				} catch (Exception e) {
					//Failed to initialize monitoring for
					//a particular server instance

					ServiceException excService = new ServiceException(e);
					currUIserver.setLastException(excService);
					
					String serverName = (currServer!=null)?currServer.toString():"";
					String docbaseName = currDocbase.getName();
					String[] errArgs = {serverName,docbaseName};
					logger.error("Failed to initialize monitoring for server {0} of docbase {1}");
				}
			}
		}
		
		//Store docbroker in the context
		this.getServletContext().setAttribute(ATTR_DOCBROKER,broker);
	}
	
	/**
	 * Updates RPC objects with
	 * monitoring results. This method
	 * retrieves monitoring state and updates
	 * UI instances of Docbase servers with the
	 * latest information.
	 * @param broker Docbroker instance
	 * @throws Exception if there is a problem.
	 */
	protected void refreshUIservers(IDocbroker broker) throws Exception
	{
		logger.trace("Method refreshUIservers()");
		
		//Iterate thru servers of all docbases
		List<IDocbase> docbases = broker.getDobases();
		List<DocbaseServer> docbaseServers = new ArrayList<DocbaseServer>();
		for(IDocbase currDocbase : docbases){
			java.util.List<IServer> servers = currDocbase.getServers();
			for(IServer currServer : servers){
//				DocbaseServer docbaseServer = new DocbaseServer(currServer);
				//Instantiate an RPC replica of the docbase server
			    String name=currServer.getName();
			    String host=currServer.getHost();
				//Update the UI server
				DocbaseServer docbaseUIServer = serversUI.get(name+host);			    
			    int maxSessCount = currServer.getMaxSessionCount();
			    
			    
			    ActiveSessionCounter activeSession = (ActiveSessionCounter)currServer.getFilter("ActiveSessionCounter");
			    		    
			    if(activeSession!=null){
			    	
				    if(logger.isInfoEnabled()){
				    	logger.info(activeSession.toString());
				    }
				    
				    int currSessCount=activeSession.getActiveSessionCount();
				    int prevSessCount=activeSession.getPreviousSessionCount();
				    Date lastUpdate=activeSession.getInspectTime();
				    
				    docbaseUIServer.setMaxSessCount(maxSessCount);
				    docbaseUIServer.setCurrSessCount(currSessCount);
				    docbaseUIServer.setPrevSessCount(prevSessCount);
				    docbaseUIServer.setLastUpdate(lastUpdate);
				    docbaseUIServer.setLastException(null);
			    }
			}
		}
	}
	
	/**
	 * Business Layer emulator.
	 * This method returns test data
	 * without interacting with the business layer.
	 * 
	 * @return an array of test data.
	 * @throws MultiServiceException
	 */
	private DocbaseServer[] getServersEmulator()
	{
		//Test data
		String[][] testServers = {	{"MAIN","moon1","orbitx","100"},
									{"MAIN","moon2","orbitx","150"},
									{"MAIN","moon3","orbitx","200"},
									{"SPACE","saturn","universe","1000"},
									{"SPACE","jupiter","universe","1000"},
									{"STAR","earth","ground","1000"}};
		
	    //Testing UI without Business Layer
		DocbaseServer[] testDocbaseServers = new DocbaseServer[testServers.length];
		
		for(int i=0;i<testServers.length;i++){
			String[] testServer = testServers[i];
				int countMax =  Integer.parseInt(testServer[3]);
				int countNow = (int) (Math.random()*countMax);
				int countBefore = (int) (countMax * Math.random());
				
				DocbaseServer docbaseServer = new DocbaseServer(testServer[0],testServer[1],testServer[2],
						countMax,countNow,countBefore, new Date());
				
				testDocbaseServers[i]=	docbaseServer;		
		}
		return testDocbaseServers;
	}
	
	/**
	* Determines if emulation mode
	* is enabled.
	* @return true if emulation is enabled.
	*/
	protected boolean isEmulatorEnabled()
	{
		logger.trace("Method isEmulatorEnabled()");
	
		//Retrieve the parameter value
		String emulatorMode = this.getServletContext().getInitParameter(PARAM_EMULATOR);
		//Find out if emulation mode is enabled.
		boolean mode = (emulatorMode!=null && emulatorMode.trim().equalsIgnoreCase("TRUE"));
	
		return mode;
	} 
	
	/**
	 * Return the monitoring interval.
	 * @return the value of monitoring interval in milliseconds.
	 */
	protected int getMonitoringInterval(){
		
		logger.trace("Method getMonitoringInterval()");
		
		//Check if monitoring interval has been initialized
		if(monitorInterval < 0){
	 		//Retrieve the configured value
			String configuredInterval = this.getServletContext().getInitParameter(PARAM_MONITOR_INTERVAL);
			logger.info("Configured monitoring interval = "+configuredInterval);
			//Convert the configured value into a number
			try {
				monitorInterval = Integer.valueOf(configuredInterval);
				//Ensure that monitoring interval is not less than
				//the default value
				if(monitorInterval < DEFAULT_MONITOR_INTERVAL)
				{
					logger.info("Monitoring interval cannot be less than "+DEFAULT_MONITOR_INTERVAL);
					monitorInterval=DEFAULT_MONITOR_INTERVAL;
				}
				 
			} catch (Exception e) {
				monitorInterval = DEFAULT_MONITOR_INTERVAL;
			}
			logger.info("Monitoring interval is set to "+monitorInterval);
			monitorInterval*=1000;
			logger.info("Monitoring interval is converted to "+monitorInterval+" milliseconds");
		}
		
		logger.info("Returned monitroing interval is set to "+monitorInterval);
		return monitorInterval;
	}
}

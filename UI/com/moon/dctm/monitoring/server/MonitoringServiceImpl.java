package com.moon.dctm.monitoring.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.moon.dctm.monitoring.client.rpc.DocbaseServer;
import com.moon.dctm.monitoring.client.rpc.MonitoringService;
import com.moon.dctm.monitoring.client.rpc.MultiServiceException;
import com.moon.dctm.monitoring.client.rpc.ServiceException;
import com.moon.dctm.monitoring.sessmon.IDocbase;
import com.moon.dctm.monitoring.sessmon.IDocbroker;
import com.moon.dctm.monitoring.sessmon.IFilter;
import com.moon.dctm.monitoring.sessmon.IServer;
import com.moon.dctm.monitoring.sessmon.Starter;
import com.moon.dctm.monitoring.sessmon.filters.ActiveSessionCounter;

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
	
	/** Test switch. Application works in testing mode if this
	 * flag is set to TRUE. Test data is generated and displayed
	 * in test mode instead of accessing the Business Layer.*/
	private static final Boolean EMULATOR = false;
	/** Multiple exception test switch. This switch control testing
	 * of multiple exceptions. It works only if the main test switch
	 * is set to TRUE.*/
	private static final Boolean EMULATOR_MULTI_EXCEPTION = false;
	
	private Map<String,DocbaseServer> serversUI = null;
	
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
		
		DfLogger.trace(this,"Method getServers()",null, null);
		
		//Check if test mode is enabled...
		if(EMULATOR){
			//Return test data
			return getServersEmulator();			
		}else{
			//Get the list of docbrokers
			try {
				return getDocbaseServers(docbaseName);
			} catch (DfException e) {
				//Log the error message
				DfLogger.error(this,"DFC error has occurred.",null,e);
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
	protected DocbaseServer[] getDocbaseServers(String docbaseName) throws DfException, MultiServiceException {	
		
		DfLogger.trace(this,"Method getDocbaseServers()",null, null);
		
		//Check if monitoring has started already
		IDocbroker broker = (IDocbroker)this.getServletContext().getAttribute(ATTR_DOCBROKER);
		//Start monitoring if it hasn't been started yet
		if(broker==null){			
			//Retrieve user credentials
			String userName = this.getServletContext().getInitParameter(PARAM_USER_NAME);
			String userPassword = this.getServletContext().getInitParameter(PARAM_PASSWORD);
			String userDomain = this.getServletContext().getInitParameter(PARAM_DOMAIN);
			
			//create Client objects
//			IDfClientX clientx = new DfClientX();
//			IDfClient client = clientx.getLocalClient();			
//			client.initCrypto("C:\\Documentum\\config\\secure\\aek.key");
			
			//Initiate monitoring
			launch(userName,userPassword,userDomain);
			broker = (IDocbroker)this.getServletContext().getAttribute(ATTR_DOCBROKER);
		}
		
		//Iterate thru servers of all docbases
		List<IDocbase> docbases = broker.getDobases();
		List<DocbaseServer> docbaseServers = new ArrayList<DocbaseServer>();
		for(int i=0;i<docbases.size();i++){
			IDocbase currDocbase = docbases.get(i);
			String currDocbaseName = currDocbase.getName();
			java.util.List<IServer> servers = currDocbase.getServers();
			for(int j=0;j<servers.size();j++){
				IServer currServer = servers.get(j);
//				DocbaseServer docbaseServer = new DocbaseServer(currServer);
				//Instantiate an RPC replica of the docbase server
			    String name=currServer.getName();
			    String host=currServer.getHost();
				//Update the UI server
				DocbaseServer docbaseUIServer = serversUI.get(name+host);			    
			    int maxSessCount = currServer.getMaxSessionCount();
			    
			    
			    ActiveSessionCounter activeSession = (ActiveSessionCounter)currServer.getFilter("ActiveSessionCounter");
			    		    
			    if(activeSession!=null){
			    	
				    if(DfLogger.isInfoEnabled(this)){
				    	DfLogger.info(this, activeSession.toString(), null, null);
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
		
//		DocbaseServer[] ret = new DocbaseServer[serversUI.size()];
//		for(int i=0;i<serversUI.size();i++){
//			ret[i]=serversUI.values().get(i);
//		}
//		DocbaseServer[] ret =(DocbaseServer[]) docbaseServers. toArray(); 
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
	protected void launch(String userName, String userPassword, String userDomain) throws DfException, MultiServiceException
	{
		DfLogger.trace(this,"Method launch()",null, null);
		
		//Declare a docbroker variable
		IDocbroker broker = null;
		//Get a docbroker instance
		broker = Starter.getLocalDocbroker();
		//Declare multi exception
//		MultiServiceException excMulti = null;
		
		//Initialize the list of UI servers
		serversUI = new LinkedHashMap<String, DocbaseServer>();
		
		//Iterate thru servers of all docbases
		List<IDocbase> docbases = broker.getDobases();
		for(int i=0;i<docbases.size();i++){
			IDocbase currDocbase = docbases.get(i);
			List<IServer> servers = currDocbase.getServers();
			for(int j=0;j<servers.size();j++){
				IServer currServer=null;;
				DocbaseServer currUIserver = null;
				try {
					//Get the server instance
					currServer = servers.get(j);
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
					currServer.startMonitoring(30000);
					//currUIserver
				} catch (DfException e) {
					//Failed to initialize monitoring for
					//a particular server instance
//					if(excMulti==null){
//						excMulti  =  new MultiServiceException();
//					}
					//Create a service exception and
					//add it to the list of accumulated exceptions
					ServiceException excService = new ServiceException(e);
//					excMulti.addException(excService);
					currUIserver.setLastException(excService);
					
					String serverName = (currServer!=null)?currServer.toString():"";
					String docbaseName = currDocbase.getName();
					String[] errArgs = {serverName,docbaseName};
					DfLogger.error(this,"Failed to initialize monitoring for server {0} of docbase {1}",errArgs,null);
				}
			}
		}
		
		//Store docbroker in the context
		this.getServletContext().setAttribute(ATTR_DOCBROKER,broker);
		

		//Throw the exception if there were problems
//		if(excMulti!=null){
//			throw excMulti;
//		}
	}
	
	/**
	 * Business Layer emulator.
	 * This method returns test data
	 * without interacting with the business layer.
	 * @return an array of test data.
	 * @throws MultiServiceException
	 */
	private DocbaseServer[] getServersEmulator() throws MultiServiceException
	{
		//Verification of multiple exceptions
		if(EMULATOR_MULTI_EXCEPTION){
			MultiServiceException multi = new MultiServiceException();
			for (int i=0;i<3;i++){
				ServiceException exc = new ServiceException("Error #"+i);
				multi.addException(exc);			
			}
			throw multi;
		}
		
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
}

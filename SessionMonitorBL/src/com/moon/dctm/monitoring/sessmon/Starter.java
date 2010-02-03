package com.moon.dctm.monitoring.sessmon;

import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.moon.dctm.monitoring.sessmon.filters.ActiveSessionCounter;
import com.moon.dctm.monitoring.sessmon.impl.Factory;

public class Starter {


	
	/**
	 * Factory method for docbroker.
	 * @return an instance of IDocbroker
	 */
	public static IDocbroker getLocalDocbroker() throws DfException{
		return Factory.newDocbroker();
	}
	
	/**
	 * Test method for Session Monitoring.
	 * @param args command line arguments
	 * 1. user name
	 * 2. user password
	 * 3. user domain
	 */
	public static void main(String[] args){
		
		String userName = args[0];
		String userPwd = args[1];
		String userDomain = args[2];
		
		new Starter().launch(userName, userPwd, userDomain);

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
	 */
	public void launch(String userName, String userPassword, String userDomain)
	{
		//Declare a docbroker variable
		IDocbroker broker = null;
		try {
			//Get a docbroker instance
			broker = getLocalDocbroker();
		} catch (DfException e1) {
			DfLogger.error(this,"Failed to initialize the docbroker. Fatal error",null,null);
			DfLogger.error(this,null,null,e1);
			return;
		}
		
		
		//Iterate thru servers of all docbases
		java.util.List docbases = broker.getDobases();
		for(int i=0;i<docbases.size();i++){
			IDocbase currDocbase = (IDocbase)docbases.get(i);
			java.util.List servers = currDocbase.getServers();
			for(int j=0;j<servers.size();j++){
				IServer currServer=null;;
				try {
					//Initialize the server
					currServer = (IServer)servers.get(j);
					currServer.init(userName,userPassword,userDomain);
					//Add filter to the server
					currServer.addFilter("ActiveSessionCounter",new ActiveSessionCounter());
					//Start monitoring
					currServer.startMonitoring();
				} catch (DfException e) {
					//Failed to intialize monitoring for
					//a particular server instance
					String serverName = (currServer!=null)?currServer.toString():"";
					String docbaseName = currDocbase.getName();
					String[] errArgs = {serverName,docbaseName};
					DfLogger.error(this,"Failed to initialize monitoring for server {0} of docbase {1}",errArgs,null);
					DfLogger.error(this,null,null,e);
				}
			}
		}
		
	}
}

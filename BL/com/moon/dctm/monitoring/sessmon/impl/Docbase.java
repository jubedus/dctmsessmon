package com.moon.dctm.monitoring.sessmon.impl;

import java.util.ArrayList;
import java.util.List;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfDocbaseMap;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfAttr;
import com.moon.dctm.monitoring.sessmon.IDocbase;
import com.moon.dctm.monitoring.sessmon.IServer;

public class Docbase implements IDocbase {

	final static String SERVER_NAME="r_server_name";
	final static String SERVER_HOST="r_host_name";
	final static String SERVER_PROXIMITY="r_client_proximity";
	
	
	IDfTypedObject serverMap;
	String name = null;
	String ID = null;
	String description = null;
	List servers;
	
	/**
	 * Docbase constructor
	 * @param docbaseID docbase ID
	 * @param docbaseName docbase name
	 * @param docbaseDescription docbase description
	 * @throws DfException 
	 */
	Docbase(String docbaseID, String docbaseName, String docbaseDescription) throws DfException{	
		name = docbaseName;
		ID= docbaseID;
		description=docbaseDescription;
		
        IDfClientX clientx = new DfClientX();
        IDfClient client;
		//Get docbase servers
		client = clientx.getLocalClient();
	    IDfDocbaseMap docbaseMap =  client.getDocbaseMap();  
		serverMap = docbaseMap.getServerMapByName(docbaseName);
			
		//Instantiate the list of servers
		int serverCount = serverMap.getValueCount(SERVER_NAME);
		servers = new ArrayList();
			
		//Populate server list
		for(int i=0;i<serverCount;i++){
			String serverName = serverMap.getRepeatingString(SERVER_NAME,i);
			String serverHost = serverMap.getRepeatingString(SERVER_HOST,i);
			int serverProximity = serverMap.getRepeatingInt(SERVER_PROXIMITY,i);
			
			IServer server = new DocbaseServer(this.getName(),serverName,serverHost,serverProximity);
			servers.add(server);
			
		}
			
		//Printout docbase details
		logAttributes(serverMap);
	}
	
	public int getServerCount() {
		return servers.size();
	}

	public List getServers() {
		return servers;
	}

	public String getName() {
		return name;
	}

	public String getID() {
		return ID;
	}

	public String getDescription() {
		return description;
	}
	
	void logAttributes(IDfTypedObject objectToIspect) throws DfException{
		
		if(DfLogger.isDebugEnabled(this)){
			for(int i=0;i<objectToIspect.getAttrCount();i++){
				IDfAttr attr = objectToIspect.getAttr(i);
				//Construct the debug message
				String debugMsg = "Docbase {0} Attribute {1}: Repeating {2}: Values: {3}";
				String[] debugArgs = new String[4];
				debugArgs[0]=this.getName();
				debugArgs[1]=attr.getName();
				debugArgs[2]=attr.isRepeating()?"TRUE":"FALSE";
				debugArgs[3]=objectToIspect.getAllRepeatingStrings(attr.getName(),null);

				//Print out the debug message
				DfLogger.debug(this,debugMsg, debugArgs, null);
				
			}
		}
	}
}

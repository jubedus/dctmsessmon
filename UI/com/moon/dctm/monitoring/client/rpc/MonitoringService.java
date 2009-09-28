package com.moon.dctm.monitoring.client.rpc;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
public interface MonitoringService extends RemoteService {
	
	/**
	 * Provides an array of docbase servers.
	 * @param docbaseName name of the docbase
	 * @return an array of docbase servers.
	 * @throws MultiServiceException 
	 */
	DocbaseServer[] getServers(String docbaseName) throws ServiceException, MultiServiceException;
}

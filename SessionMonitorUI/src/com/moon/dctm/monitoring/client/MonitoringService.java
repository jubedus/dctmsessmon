package com.moon.dctm.monitoring.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.moon.dctm.monitoring.shared.DocbaseServer;
import com.moon.dctm.monitoring.shared.MultiServiceException;
import com.moon.dctm.monitoring.shared.ServiceException;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("monitor")
public interface MonitoringService extends RemoteService {
	
	/**
	 * Provides an array of docbase servers.
	 * @param docbaseName name of the docbase
	 * @return an array of docbase servers.
	 * @throws MultiServiceException
	 */
	DocbaseServer[] getServers(String docbaseName) throws ServiceException, MultiServiceException;
}

package com.moon.dctm.monitoring.client.rpc;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ServiceException extends Exception implements IsSerializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6328234916716331076L;
	  
	  public ServiceException() {
		  super();
	  }
	  
	  public ServiceException(String message) {
	    super(message);
	  }
	  
	  public ServiceException(Throwable caught) {
		    super(caught);
	  }
}

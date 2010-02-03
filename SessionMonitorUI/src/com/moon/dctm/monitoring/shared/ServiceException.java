package com.moon.dctm.monitoring.shared;

import java.io.Serializable;

public class ServiceException extends Exception implements Serializable{

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

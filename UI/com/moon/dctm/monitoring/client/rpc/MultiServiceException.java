package com.moon.dctm.monitoring.client.rpc;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class MultiServiceException extends Exception  implements IsSerializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5467628457090523391L;
	
	  private List<ServiceException> errors = new ArrayList<ServiceException>();
	  
	  public MultiServiceException() {
		  super();
	  }
	  
	  public void addException (ServiceException exceptionToAdd){
		  errors.add(exceptionToAdd);
	  }
	  
	  public List<ServiceException> getException(){
		  return errors;
	  }

	/* (non-Javadoc)
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		StringBuilder errMsg = new StringBuilder();
		
		for(int i=0;i<errors.size();i++){
			errMsg.append(errors.get(i));
			errMsg.append(" ");
		}
		
		return errMsg.toString();
	}

	  
}

package com.moon.dctm.monitoring.sessmon.util;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.common.DfException;

public class PasswordEncrypter {

	/**
	 * This class encrypts
	 * the passed in argument using DFC.
	 * This class can be used to encrypt the password
	 * for session monitoring.
	 * @param args the first element of the
	 * array will be encrypted and printed out
	 * to the console.
	 */
	public static void main(String[] args) {
	
		if(args.length==0){
			System.out.println("Usage Rules");
			System.out.println("PasswordEncrypter TXT");
			System.out.println("TXT - string to encrypt");
			return;
		}
		
		if(args.length!=1){
			System.out.println("Number of arguments passed in: "+args.length);
			return;			
		}
		
		try {
    		//create Client objects
    		IDfClientX clientx = new DfClientX();
    		IDfClient client = clientx.getLocalClient();
    				
    		String strEncrypted = client.encryptPassword(args[0]);
			System.out.println("Encrypted string "+strEncrypted);
		} catch (DfException e) {
			//There was an error while
			//encrypting the string.
			e.printStackTrace();
		}	

	}

}

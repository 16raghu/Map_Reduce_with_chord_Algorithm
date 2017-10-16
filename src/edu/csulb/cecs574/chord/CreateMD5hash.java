package edu.csulb.cecs574.chord;

import java.math.BigInteger;
import java.security.MessageDigest;

public class CreateMD5hash {
	
	public static String  createHash() throws Exception{
	          String s="This is a test";
	           MessageDigest m=MessageDigest.getInstance("MD5");
	           m.update(s.getBytes(),0,s.length());
	           String returnvalue = new BigInteger(1,m.digest()).toString(16);
	           System.out.println("MD5: "+returnvalue);
	          
			return returnvalue ;
	       }
		
		
	
}

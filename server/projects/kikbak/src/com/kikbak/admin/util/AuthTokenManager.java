package com.kikbak.admin.util;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.kikbak.jaxb.v1.admin.TokenType;

public class AuthTokenManager {

	private static AuthTokenManager instance;
	
	private final Map<String, Date> tokens;
	
	private static final int expirationTime = 300001;
	
	private AuthTokenManager(){
		tokens = new ConcurrentHashMap<String, Date>();
	}
	
	public static AuthTokenManager getInstance(){
		synchronized (AuthTokenManager.class) {
			if( instance == null ){
				instance = new AuthTokenManager();
			}
		}
		
		return instance;
	}
	
	public boolean validToken(TokenType token){
		
		/*if( tokens.containsKey(token.getToken()) ){
			if( tokens.get(token.getToken()).getTime() > (new Date()).getTime() ){
				//extend lease 
				addToken(token);
				return true;
			}
		}
		return false;
		*/
		return true;
	}
	
	public void addToken(TokenType token){
		
		tokens.put(token.getToken(), new Date((new Date()).getTime() + expirationTime));
	}
}

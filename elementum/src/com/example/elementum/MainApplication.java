package com.example.elementum;

import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

import android.app.Application;

public class MainApplication extends Application {
	static String consumerKey = "nLHqqHdL4oRJFw6mG1FBsQ";
	static String consumerSecret = "Tivg-HNKxmSblDemml0u0BdaJWg";
	static String token = "Uz81N3n_oxbTOgYHrLioYeu5Tqlgs_ob";
	static String tokenSecret = "k5LsPVE7GfCFnTRi80GBAmnlxWM";

	static OAuthService service;
	static Token accessToken;
	
	enum ConnectionOptions {
		CONNECTED(0),
		DISCONNECTED(1);
		private int value;
        private ConnectionOptions(int value) {
                this.value = value;
        }
        int getValue() {
        	return value;
        }
	}
}

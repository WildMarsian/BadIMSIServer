package org.imsi.badimsibox.badimsiserver;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.shiro.PropertiesProviderConstants;
import io.vertx.ext.auth.shiro.ShiroAuth;
import io.vertx.ext.auth.shiro.ShiroAuthRealmType;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.FormLoginHandler;
import io.vertx.ext.web.handler.RedirectAuthHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.UserSessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;

public class BadIMSIService extends AbstractVerticle {

	@Override
	public void start() {
		Router router = Router.router(vertx);
		       
    	router.get("/master/session/:state").handler(rc -> {
    		String name = rc.request().getParam("state");
    		// We have to give the right response
    		rc.response()
            	.putHeader("content-type", "application/json")
            	.end(new JsonObject().put("Master session state", name).encode());
    	});
		
		// Let's set up the cookies, request bodies and sessions
		router.route().handler(CookieHandler.create());
		router.route().handler(BodyHandler.create());
		router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
		
		// Simple auth using properties file for user/role info
		AuthProvider authProvider = ShiroAuth.create(vertx, ShiroAuthRealmType.PROPERTIES, new JsonObject().put(PropertiesProviderConstants.PROPERTIES_PROPS_PATH_FIELD, "classpath:auth.properties"));
		
		// Using session handler to make sure user is stored in the session between requests
		router.route().handler(UserSessionHandler.create(authProvider));
		
		router.route("/master/*").handler(RedirectAuthHandler.create(authProvider, "/index.html"));

		// Creating the static route towards the webroot folder
		router.route("/master/*").handler(StaticHandler.create().setCachingEnabled(false).setWebRoot("webroot/master"));
		
		router.route("/loginhandler").handler(FormLoginHandler.create(authProvider, "username", "password", "/index.html", "/master/index.html"));
		
		router.route("/logout").handler(context -> {
			context.clearUser();
			context.response().putHeader("location", "/").setStatusCode(302).end();
		});
		
		router.route().handler(StaticHandler.create());
		vertx.createHttpServer().requestHandler(router::accept).listen(8080);
	}
}

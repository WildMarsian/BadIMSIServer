package org.imsi.badimsibox.badimsiserver;

import org.apache.shiro.realm.Realm;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.shiro.ShiroAuth;
import io.vertx.ext.auth.shiro.ShiroAuthOptions;
import io.vertx.ext.auth.shiro.ShiroAuthRealmType;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.UserSessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;

public class BadIMSIService extends AbstractVerticle {

	@Override
	public void start() {
		Router router = Router.router(vertx);
		
		// Let's set up the cookies, request bodies and sessions
		router.route().handler(CookieHandler.create());
		router.route().handler(BodyHandler.create());
		router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
		
		// Simple auth using properties file for user/role info
		Realm realm = new R
		
		// Using session handler to make sure user is stored in the session between requests
		// router.route().handler(UserSessionHandler.create(authProvider));
		
		

		// Just us me for tests. Remove me at the end. Thank you :)
		router.get("/hello/world/:name/").handler(rc -> {
			String name = rc.request().getParam("name");
			rc.response().putHeader("content-type", "application/json")
					.end(new JsonObject().put("key", "Bonjour " + name).encode());
		});

		// Creating the static route towards the webroot folder
		router.route().handler(StaticHandler.create());

		vertx.createHttpServer().requestHandler(router::accept).listen(8080);
	}
}

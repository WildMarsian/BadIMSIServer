package org.imsi.badimsibox.badimsiserver;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;

public class BadIMSIService extends AbstractVerticle {
	
	@Override
	public void start() {
		Router router = Router.router(vertx);
		router.route().handler(CookieHandler.create());
		router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
		
		/*
		 * Ici on definit les differentes URI et comment elles seront gerrés par
		 * vertx a l'aide du Handler. Ici il s'agit d'une méthode get car il n'y
		 * a pas de modifications des donnés au niveau du service. Les parties
		 * de l'URI contenant des ":" sont des paramètres.
		 */

		router.get("/hello/world/:name/").handler(rc -> {
			// Recuperation du parametre name
			String name = rc.request().getParam("name");

			rc.response().putHeader("content-type", "application/json")
					.end(new JsonObject().put("key", "Bonjour " + name).encode());
		});

		router.get("/user/:username/password/:salted/").handler(rc -> {
			String user = rc.request().getParam("username");
			String password = rc.request().getParam("salted");
			router.route().handler(routingContext -> {
				
				System.out.println("test");
				
			});
			rc.response().putHeader("content-type", "application/json")
					.end(new JsonObject().put("user", user)
							.put("password", password).encode());
		});

		/*
		 * Ici on définit la methode appelee lors de l'ecoute et sur quel port
		 * cette écoute s'effectue
		 */
		vertx.createHttpServer().requestHandler(router::accept).listen(8080);
	}
}

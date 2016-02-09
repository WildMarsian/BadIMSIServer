package org.imsi.badimsibox.badimsiserver;

import io.vertx.core.Handler;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * 
 * @author aisuko
 * @version 1.0
 * 
 * A simple template manager for MVEL engine.
 * Don't forget to call setRouter first!
 * @see #setRouter(Router)
 *
 */
public class TemplateManager {
	private static Router ROUTER;
	
	/**
	 * Sets the router of the Vert.x server.
	 * To be called first!!
	 * @param router
	 */
	public static void setRouter(Router router) {
		ROUTER = router;
	}
	
	private static void setTemplateContent(Router router, String templateName, Handler<RoutingContext> context) {
		router.route(templateName).handler(context);
	}
	
	public static void templateContext(String templateName, Handler<RoutingContext> context) {
		setTemplateContent(ROUTER, templateName, context);
	}
}

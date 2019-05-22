package de.hsmannheim.routing;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import de.hsmannheim.routing.here.Route;
import de.hsmannheim.routing.here.RoutingAPIUtils;
import processing.core.PApplet;

/*
 * Simple routing app. Queries API to get a route and shows it on a map.
 * 
 * Click anywhere on the map to set start location, and see a route from map center to that point.
 * Right click to change destination location.
 */
public class SimpleRoutingApp extends PApplet {

	public static final Location CENTER_LOCATION = new Location(52.5, 13.4);

	UnfoldingMap map;
	RoutingAPIUtils routingAPI;

	Location startLocation = CENTER_LOCATION;
	Location endLocation;

	Route route;

	public void settings() {
		size(1024, 768, P2D);
	}

	public void setup() {
		map = new UnfoldingMap(this);
		map.zoomAndPanTo(13, startLocation);
		MapUtils.createDefaultEventDispatcher(this, map);

		routingAPI = new RoutingAPIUtils(this, "YOUR-APP-ID", "YOUR-APP-CODE");
	}

	public void draw() {
		map.draw();

		if (route != null) {
			// Draw route shape
			stroke(2255, 0, 0);
			strokeWeight(3);
			noFill();
			beginShape();
			for (Location location : route.getLocations()) {
				ScreenPosition pos = map.getScreenPosition(location);
				vertex(pos.x, pos.y);
			}
			endShape();
		}
	}

	public void mouseClicked() {
		if (mouseButton == LEFT) {
			endLocation = map.getLocation(mouseX, mouseY);
		}
		if (mouseButton == RIGHT) {
			startLocation = map.getLocation(mouseX, mouseY);
		}
		if (startLocation != null && endLocation != null) {
			route = routingAPI.loadRoute(startLocation, endLocation);
			println("Loaded " + route.getManeuvers().size() + " maneuvers");
		}
	}

	public static void main(String args[]) {
		PApplet.main(new String[] { SimpleRoutingApp.class.getName() });
	}

}

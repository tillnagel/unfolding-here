package de.hsmannheim.routing;

import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.utils.GeoUtils;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import de.hsmannheim.routing.here.Maneuver;
import de.hsmannheim.routing.here.Route;
import de.hsmannheim.routing.here.RoutingAPIUtils;

public class RadialRoutingTestApp extends PApplet {

	UnfoldingMap map;
	RoutingAPIUtils routingAPI;

	int maxTravelTime = 60 * 30; // 0.5h
	Location startLocation = new Location(52.5, 13.4);

	List<Route> routesList = new ArrayList<Route>();
	int directionsAmount = 36;
	float distance = 5;

	public void settings() {
		size(800, 600, P2D);
	}

	public void setup() {
		colorMode(HSB, 360, 100, 100);

		map = new UnfoldingMap(this);
		map.zoomAndPanTo(12, startLocation);
		MapUtils.createDefaultEventDispatcher(this, map);

		routingAPI = new RoutingAPIUtils(this, "YOUR-APP-ID", "YOUR-APP-CODE");
	}

	public void draw() {
		background(255);
		map.draw();

		noFill();
		float bearingDiff = 360 / directionsAmount;
		float bearing = 0;
		// println("Drawing all");
		for (Route route : routesList) {
			stroke(bearing, 100, 100);
			drawWeightedRoutes(route);
			// drawRoutes(route.getManeuvers());
			bearing += bearingDiff;
		}
	}

	public void drawWeightedRoutes(Route route) {
		boolean distanceBased = true;

		float maxDist = (float) GeoUtils.getDistance(startLocation, route.getCalculatedToLocation());
		beginShape();
		int i = 0;
		for (Location location : route.getLocations()) {
			ScreenPosition pos = map.getScreenPosition(location);

			if (distanceBased) {
				// weight depends on distance to start location
				float dist = (float) GeoUtils.getDistance(location, startLocation);
				// starts thick (15px) at center and gets thinner
				float w = map(dist, 0, maxDist, 15, 0);
				strokeWeight(w);
			} else {
				// weight depends on number of route segment
				strokeWeight(max(1, map(i, 0, route.getLocations().size() - 2, 2, 15)));
			}

			vertex(pos.x, pos.y);
			i++;
		}
		endShape();
	}

	public void drawRoutes(List<Maneuver> maneuvers) {
		beginShape();
		for (Maneuver m : maneuvers) {
			ScreenPosition pos = map.getScreenPosition(m.getLocation());
			vertex(pos.x, pos.y);
		}
		endShape();
	}

	public void keyPressed() {
		if (key == 't') {
			maxTravelTime -= 60;
		}
		if (key == 'T') {
			maxTravelTime += 60;
		}
		if (key == 'r') {
			loadAllRoutes();
		}
	}

	public void mouseClicked() {
		startLocation = map.getLocation(mouseX, mouseY);
		loadAllRoutes();
	}

	public void loadAllRoutes() {
		print("Loading all routes from " + startLocation + " to " + distance + "km, under " + maxTravelTime + "s ... ");

		routesList.clear();

		float bearingDiff = 360 / directionsAmount;
		float bearing = 0;
		for (int i = 0; i < directionsAmount; i++) {
			Route route = routingAPI.loadRoute(startLocation,
					GeoUtils.getDestinationLocation(startLocation, bearing, distance));
			routesList.add(route);

			bearing += bearingDiff;
			println(route.getLocations().size());
		}

		println("Done. Loaded " + routesList.size() + " routes.");
	}

	public static void main(String args[]) {
		PApplet.main(new String[] { RadialRoutingTestApp.class.getName() });
	}

}

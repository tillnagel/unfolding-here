package de.hsmannheim.routing;

import java.util.ArrayList;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.utils.GeoUtils;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import de.hsmannheim.routing.here.Route;
import de.hsmannheim.routing.here.RoutingAPIUtils;
import de.hsmannheim.routing.here.RoutingAPIUtils.RoutingMode;
import processing.core.PApplet;

public class RoundTripApp extends PApplet {

	UnfoldingMap map;
	RoutingAPIUtils routingAPI;

	Location centerLocation = new Location(52.5, 13.4);

	Route route;
	ArrayList<Location> locations = new ArrayList<Location>();
	int maxTravelTime = 60; // in minutes

	public void settings() {
		size(1024, 768, P2D);
	}

	public void setup() {
		map = new UnfoldingMap(this);
		map.zoomAndPanTo(13, centerLocation);
		MapUtils.createDefaultEventDispatcher(this, map);

		routingAPI = new RoutingAPIUtils(this, "YOUR-APP-ID", "YOUR-APP-CODE");
	}

	public void draw() {
		map.draw();

		if (route != null) {

			// Draw route shape
			stroke(255, 255, 0);
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

	public void findRoundTrip(Location startLocation, float tripDirection, int tripTime) {
		
		float walkingSpeed = 3f / 60f; // 3km/h
		float tripLength = tripTime * walkingSpeed;
		
		 
		float a = tripLength / 4f;
		float c = (float) Math.sqrt(2) * a;
		println(a + " , " + c);
		
		List<Location> locations = new ArrayList<Location>();
		locations.add(startLocation);
		locations.add(GeoUtils.getDestinationLocation(startLocation, 45, a));
		locations.add(GeoUtils.getDestinationLocation(startLocation, 0, c));
		locations.add(GeoUtils.getDestinationLocation(startLocation, -45, a));
		locations.add(startLocation);
		
		route = loadMultiStepRoute(locations);
		println("max travel time: " + tripTime + ", route travel time: " + route.getTravelTime());
		
	}

	public void keyPressed() {
		if (key == 'r') {
			locations.remove(locations.size() - 1);
			loadMultiStepRoute(locations);
		}
	}

	public void mouseClicked() {
		if (mouseButton == LEFT) {
			locations.add(map.getLocation(mouseX, mouseY));
		}
		if (mouseButton == RIGHT) {
			locations.clear();
			findRoundTrip(map.getLocation(mouseX, mouseY), 0, maxTravelTime);
		}
		if (locations.size() >= 2) {
			route = loadMultiStepRoute(locations);
			println("Loaded route with a total walking time: " + route.getTravelTime());
		}
	}

	public Route loadMultiStepRoute(List<Location> locations) {
		String apiString = routingAPI.getMultiStepRoutingAPIString(locations, RoutingMode.PEDESTRIAN);

		PApplet.println(apiString);

		String[] rawJSONResultArray = loadStrings(apiString);
		String rawJSONResult = rawJSONResultArray[0];
		return routingAPI.parseRoutingResult(rawJSONResult);
	}

	public static void main(String args[]) {
		PApplet.main(new String[] { RoundTripApp.class.getName() });
	}
}

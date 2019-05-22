package de.hsmannheim.routing;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.utils.GeoUtils;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import de.hsmannheim.routing.here.Maneuver;
import de.hsmannheim.routing.here.Route;
import de.hsmannheim.routing.here.RoutingAPIUtils;
import processing.core.PApplet;

/*
 * Routing test app. 
 * 
 * Left click to set start location, right click to set destination location.
 * 
 * Uses weighted lines to show distance to start location.
 * Press 'm' to toggle showing maneuvers.
 */
public class RoutingTestApp extends PApplet {

	UnfoldingMap map;
	RoutingAPIUtils routingAPI;

	Location centerLocation = new Location(52.5, 13.4);
	Location startLocation = centerLocation;
	Location endLocation;

	Route route;

	boolean showManeuvers = true;

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
			ScreenPosition prevPos = null;
			for (Location location : route.getLocations()) {
				ScreenPosition pos = map.getScreenPosition(location);
				float dist = (float) GeoUtils.getDistance(location, startLocation);
				float w = map(dist, 0, 5, 1, 10);
				strokeWeight(w);
				if (prevPos != null) {
					line(pos.x, pos.y, prevPos.x, prevPos.y);
				}
				prevPos = pos; 
			}

			if (showManeuvers) {
				// Draw maneuvers
				stroke(255, 0, 255, 150);
				strokeWeight(1);
				noFill();
				beginShape();
				ScreenPosition prevManeuverPos = null;
				String prevManeuverInfo = null;
				for (Maneuver m : route.getManeuvers()) {
					ScreenPosition pos = map.getScreenPosition(m.getLocation());
					vertex(pos.x, pos.y);

					if (prevManeuverPos != null) {
						float x = lerp(pos.x, prevManeuverPos.x, 0.5f);
						float y = lerp(pos.y, prevManeuverPos.y, 0.5f);
						text(prevManeuverInfo, x, y);
					}
					prevManeuverPos = pos;
					prevManeuverInfo = m.getLength() + "m, " + m.getTravelTime() + "s";
				}
				endShape();
			}
		}

		// Draw start and end marker
		noStroke();
		fill(0, 255, 0, 200);
		ScreenPosition startPos = map.getScreenPosition(startLocation);
		ellipse(startPos.x, startPos.y, 10, 10);
		if (endLocation != null) {
			fill(255, 0, 0, 200);
			ScreenPosition endPos = map.getScreenPosition(endLocation);
			ellipse(endPos.x, endPos.y, 10, 10);
		}
	}

	public void keyPressed() {
		if (key == 'm') {
			showManeuvers = !showManeuvers;
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
		PApplet.main(new String[] { RoutingTestApp.class.getName() });
	}

}

package de.hsmannheim.routing;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.ShapeFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import de.hsmannheim.routing.here.RoutingAPIUtils;
import processing.core.PApplet;

/**
 * Renders an isochrone map. Shows area reachable by foot in a specified time through the isoline technique.
 * 
 * Click to set new start location. Adapt the time to see different reachability areas.
 */
public class IsolineTestApp extends PApplet {

	UnfoldingMap map;
	RoutingAPIUtils routingAPI;

	Location startLocation = new Location(52.490, 13.471);
	int time = 30 * 60; // in seconds

	ShapeFeature isolineFeature = null;

	public void settings() {
		size(1024, 768, P2D);
	}

	public void setup() {

		map = new UnfoldingMap(this);
		map.zoomAndPanTo(14, startLocation);
		MapUtils.createDefaultEventDispatcher(this, map);

		routingAPI = new RoutingAPIUtils(this, "YOUR-APP-ID", "YOUR-APP-CODE");
		isolineFeature = routingAPI.loadIsolineByTime(startLocation, time);
	}

	public void draw() {
		map.draw();

		fill(255, 0, 0, 50);
		stroke(255, 0, 0, 200);
		beginShape();
		for (Location location : isolineFeature.getLocations()) {
			ScreenPosition pos = map.getScreenPosition(location);
			vertex(pos.x, pos.y);
		}
		endShape();
	}

	public void mouseClicked() {
		if (mouseButton == LEFT) {
			startLocation = map.getLocation(mouseX, mouseY);
			isolineFeature = routingAPI.loadIsolineByTime(startLocation, time);
		}
	}

	public static void main(String args[]) {
		PApplet.main(new String[] { IsolineTestApp.class.getName() });
	}

}

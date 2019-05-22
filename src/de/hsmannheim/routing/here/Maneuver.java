package de.hsmannheim.routing.here;

import de.fhpotsdam.unfolding.geo.Location;

/**
 * A simplified object representing HERE API's Maneuver.
 * 
 * See https://developer.here.com/documentation/routing/topics/resource-type-maneuver.html
 */
public class Maneuver {

	Location location;

	int travelTime; // in seconds
	int length; // in meters

	public Maneuver(Location location, int travelTime, int length) {
		super();
		this.location = location;
		this.travelTime = travelTime;
		this.length = length;
	}

	public Location getLocation() {
		return location;
	}

	public int getTravelTime() {
		return travelTime;
	}

	public int getLength() {
		return length;
	}

}

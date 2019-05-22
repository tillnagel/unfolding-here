package de.hsmannheim.routing.here;

import java.util.ArrayList;
import java.util.List;

import de.fhpotsdam.unfolding.geo.Location;

/**
 * A simplified object representing HERE API's Route.
 * 
 * See https://developer.here.com/documentation/routing/topics/resource-type-route.html
 */
public class Route {

	int distance;
	int travelTime;

	Location originalFromLocation;
	Location originalToLocation;

	List<Maneuver> maneuvers;
	List<Location> shapeLocations;

	public Route() {
		maneuvers = new ArrayList<Maneuver>();
		shapeLocations = new ArrayList<Location>();
	}

	public Route(Location originalFromLocation, Location originalToLocation) {
		this.originalFromLocation = originalFromLocation;
		this.originalToLocation = originalToLocation;
		maneuvers = new ArrayList<Maneuver>();
		shapeLocations = new ArrayList<Location>();
	}

	public List<Maneuver> getManeuvers() {
		return maneuvers;
	}

	public void setManeuvers(List<Maneuver> maneuvers) {
		this.maneuvers = maneuvers;
	}

	public List<Location> getLocations() {
		return shapeLocations;
	}

	public Location getCalculatedFromLocation() {
		return shapeLocations.get(0);
	}

	public Location getCalculatedToLocation() {
		return shapeLocations.get(shapeLocations.size() - 1);
	}

	public Location getOriginalFromLocation() {
		return originalFromLocation;
	}

	public Location getOriginalToLocation() {
		return originalToLocation;
	}

	public void setOriginalFromLocation(Location originalFromLocation) {
		this.originalFromLocation = originalFromLocation;
	}

	public void setOriginalToLocation(Location originalToLocation) {
		this.originalToLocation = originalToLocation;
	}

	public void setLocations(List<Location> locations) {
		this.shapeLocations = locations;
	}

	public void addManeuver(Maneuver maneuver) {
		maneuvers.add(maneuver);
	}

	public void addLocation(Location location) {
		shapeLocations.add(location);
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	/**
	 * Gets the calculated travel time for this route. Be aware this is for pedestrians, not for bikes (HERE API seems
	 * to use 4 km/h as walking speed().
	 * 
	 * @return The calculated travel time.
	 */
	public int getTravelTime() {
		return travelTime;
	}

	public void setTravelTime(int travelTime) {
		this.travelTime = travelTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((originalFromLocation == null) ? 0 : originalFromLocation.hashCode());
		result = prime * result + ((originalToLocation == null) ? 0 : originalToLocation.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Route other = (Route) obj;
		if (originalFromLocation == null) {
			if (other.originalFromLocation != null)
				return false;
		} else if (!originalFromLocation.equals(other.originalFromLocation))
			return false;
		if (originalToLocation == null) {
			if (other.originalToLocation != null)
				return false;
		} else if (!originalToLocation.equals(other.originalToLocation))
			return false;
		return true;
	}

}

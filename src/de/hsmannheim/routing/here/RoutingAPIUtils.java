package de.hsmannheim.routing.here;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.fhpotsdam.unfolding.data.Feature.FeatureType;
import de.fhpotsdam.unfolding.data.ShapeFeature;
import de.fhpotsdam.unfolding.geo.Location;
import processing.core.PApplet;

/**
 * A helper class to query HERE's API.
 * 
 * Provides access to routing and isolines. Note that lots of parameters are hard-coded, here.
 */
public class RoutingAPIUtils {

	PApplet p;
	String appAuthenticationString;

	public static enum RoutingMode {
		CAR, PEDESTRIAN
	}

	RoutingMode routingMode = RoutingMode.CAR;

	private String apiStringStart = "http://route.cit.api.here.com/routing/7.2/calculateroute.json?";
	private String apiStringWaypoint1 = "&waypoint0=geo!";
	private String apiStringWaypoint2 = "&waypoint1=geo!";
	private String apiStringModePrefix = "&mode=fastest;";
	private String apiStringModeSuffix = ";traffic:disabled";
	private String apiStringAttributes = "&routeattributes=sh";

	private String isolineApiStringStart = "https://isoline.route.cit.api.here.com/routing/7.2/calculateisoline.json?";

	private String isolineApiString1 = "&start=geo!";

	public RoutingAPIUtils(PApplet p, String appId, String appCode) {
		if ("YOUR-APP-ID".equals(appId)) {
			throw new RuntimeException("Please provide your custom appId and appCode. You can get these at https://developer.here.com");
		}
		this.p = p;
		this.appAuthenticationString = "app_id=" + appId + "&app_code=" + appCode;
	}

	/**
	 * Gets a route from start to end for car traffic.
	 * 
	 * @param startLocation
	 *            The start of the route.
	 * @param endLocation
	 *            The destination of the route.
	 * @return The complete route.
	 */
	public Route loadRoute(Location startLocation, Location endLocation) {
		return loadRoute(startLocation, endLocation, RoutingMode.CAR);
	}
	
	/**
	 * Gets a route from start to end for given modality.
	 * 
	 * @param startLocation
	 *            The start of the route.
	 * @param endLocation
	 *            The destination of the route.
	 * @param routingMode
	 *            The modality to use (pedestrian or car).
	 * @return The complete route.
	 */
	public Route loadRoute(Location startLocation, Location endLocation, RoutingMode routingMode) {
		String[] rawJSONResultArray = p.loadStrings(getRoutingAPIString(startLocation, endLocation, routingMode));
		if (rawJSONResultArray == null || rawJSONResultArray.length == 0) {
			return new Route(startLocation, endLocation);
		}
		String rawJSONResult = rawJSONResultArray[0];
		return parseRoutingResult(rawJSONResult);
	}

	
	/**
	 * Gets a route from start to end location for the specified modality.
	 * 
	 * @param startLocation
	 *            The start of the route.
	 * @param endLocation
	 *            The destination of the route.
	 * @param routingMode
	 *            The modality to use (pedestrian or car).
	 * @return The original result from the API. Needs to be parsed, e.g. with {@link #parseRoutingResult(String)}.
	 */
	private String getRoutingAPIString(Location startLocation, Location endLocation, RoutingMode routingMode) {
		String apiString = apiStringStart + appAuthenticationString + apiStringModePrefix
				+ routingMode.toString().toLowerCase() + apiStringModeSuffix + apiStringAttributes + apiStringWaypoint1
				+ startLocation.getLat() + "," + startLocation.getLon() + apiStringWaypoint2 + endLocation.getLat()
				+ "," + endLocation.getLon();

		PApplet.println(apiString);
		return apiString;
	}

	/**
	 * Gets a route including all locations for the specified modality.
	 * 
	 * @param locations
	 *            List of locations to be included in the route.
	 * @param routingMode
	 *            The modality to use (pedestrian or car).
	 * @return The original result from the API. Needs to be parsed, e.g. with {@link #parseRoutingResult(String)}.
	 */
	public String getMultiStepRoutingAPIString(List<Location> locations, RoutingMode routingMode) {
		String apiString = apiStringStart + appAuthenticationString + apiStringModePrefix
				+ routingMode.toString().toLowerCase() + apiStringModeSuffix + apiStringAttributes;

		int index = 0;
		for (Location waypoint : locations) {
			apiString += "&waypoint" + index + "=geo!" + waypoint.getLat() + "," + waypoint.getLon();
			index++;
		}

		return apiString;
	}

	private String getIsolineAPIStringByDistance(Location startLocation, int distance) {
		String apiString = isolineApiStringStart + appAuthenticationString + isolineApiString1 + startLocation.getLat()
				+ "," + startLocation.getLon() + "&range=" + distance + "&rangetype=distance&mode=shortest;pedestrian";
		return apiString;
	}

	private String getIsolineAPIStringByTime(Location startLocation, int time) {
		String apiString = isolineApiStringStart + appAuthenticationString + isolineApiString1 + startLocation.getLat()
				+ "," + startLocation.getLon() + "&range=" + time + "&rangetype=time&mode=shortest;pedestrian";
		return apiString;
	}

	/**
	 * Convenience method for loadIsolineByDistance().
	 * 
	 * @deprecated Use {@link #loadIsolineByDistance(Location, int)()} or {@link #loadIsolineByTime(Location, int)()}
	 *             instead.
	 */
	public ShapeFeature loadIsoline(Location startLocation, int distance) {
		return loadIsolineByDistance(startLocation, distance);
	}

	/**
	 * Loads isoline reachable within the given time.
	 * 
	 * @param startLocation
	 *            The starting point of the reachability area.
	 * @param time
	 *            The duration of the possible trips in seconds. This specifies the size of the reachability area.
	 * @return The polygon of the isoline, i.e. the reachability area.
	 */
	public ShapeFeature loadIsolineByTime(Location startLocation, int time) {
		processing.data.JSONObject jsonObject = p.loadJSONObject(getIsolineAPIStringByTime(startLocation, time));
		return parseIsolineRespone(jsonObject);
	}

	/**
	 * Loads isoline reachable within the given distance.
	 * 
	 * @param startLocation
	 *            The starting point of the reachability area.
	 * @param distance
	 *            The distance of the possible trips in meters.. This specifies the size of the reachability area.
	 * @return The polygon of the isoline, i.e. the reachability area.
	 */
	public ShapeFeature loadIsolineByDistance(Location startLocation, int distance) {
		processing.data.JSONObject jsonObject = p
				.loadJSONObject(getIsolineAPIStringByDistance(startLocation, distance));
		return parseIsolineRespone(jsonObject);
	}
	
	/**
	 * Parses the JSON returned from the Isoline API and returns it as a polygon. 
	 */
	protected ShapeFeature parseIsolineRespone(processing.data.JSONObject jsonObject) {
		processing.data.JSONObject response = jsonObject.getJSONObject("response");
		processing.data.JSONArray isolinesArray = response.getJSONArray("isoline");
		processing.data.JSONObject isoline = isolinesArray.getJSONObject(0); // could be many
		processing.data.JSONArray component = isoline.getJSONArray("component");
		processing.data.JSONArray isolineShape = component.getJSONObject(0).getJSONArray("shape");

		ShapeFeature shapeFeature = new ShapeFeature(FeatureType.POLYGON);
		for (String coordinate : isolineShape.getStringArray()) {
			Location location = parseCoordinateString(coordinate);
			shapeFeature.addLocation(location);
		}
		return shapeFeature;
	}

	public String[] getRawJSONIsoline(Location startLocation, int distance) {
		return p.loadStrings(getIsolineAPIStringByDistance(startLocation, distance));
	}

	/**
	 * Parses the JSON returned from the Route API and returns it as a route. 
	 */
	public Route parseRoutingResult(String rawJSONResult) {
		Route route = new Route();

		JSONObject json = new JSONObject(rawJSONResult);
		JSONObject responseObject = json.getJSONObject("response");
		JSONArray routesArray = responseObject.getJSONArray("route");

		try {
			for (int i = 0; i < routesArray.length(); i++) {
				JSONObject routeObj = routesArray.getJSONObject(i);

				JSONObject summaryObj = routeObj.getJSONObject("summary");
				route.setDistance(summaryObj.getInt("distance"));
				route.setTravelTime(summaryObj.getInt("travelTime"));

				JSONArray maneuverArr = routeObj.getJSONArray("leg").getJSONObject(0).getJSONArray("maneuver");
				for (int j = 0; j < maneuverArr.length(); j++) {
					JSONObject obj = maneuverArr.getJSONObject(j);
					JSONObject pos = obj.getJSONObject("position");

					double lng = pos.getDouble("longitude");
					double lat = pos.getDouble("latitude");
					Location location = new Location(lat, lng);
					int maneuverTravelTime = obj.getInt("travelTime");
					int maneuverLength = obj.getInt("length");

					Maneuver maneuver = new Maneuver(location, maneuverTravelTime, maneuverLength);
					route.addManeuver(maneuver);
				}

				JSONArray shapeArr = routeObj.getJSONArray("shape");
				if (shapeArr != null) {
					for (int j = 0; j < shapeArr.length(); j++) {
						String shapeCoordsStr = shapeArr.getString(j);
						route.addLocation(parseCoordinateString(shapeCoordsStr));
					}
				}
			}

		} catch (JSONException e) {
			PApplet.println(e);
		}

		return route;
	}

	/**
	 * Converts a Coordinate String into a Location. 
	 */
	public Location parseCoordinateString(String coordinateString) {
		String[] shapeCoords = coordinateString.split(",");
		double lat = Double.parseDouble(shapeCoords[0]);
		double lng = Double.parseDouble(shapeCoords[1]);
		Location location = new Location(lat, lng);
		return location;
	}

}

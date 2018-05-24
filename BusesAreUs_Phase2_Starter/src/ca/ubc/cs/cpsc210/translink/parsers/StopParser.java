package ca.ubc.cs.cpsc210.translink.parsers;

import ca.ubc.cs.cpsc210.translink.model.*;
import ca.ubc.cs.cpsc210.translink.parsers.exception.StopDataMissingException;
import ca.ubc.cs.cpsc210.translink.providers.DataProvider;
import ca.ubc.cs.cpsc210.translink.providers.FileDataProvider;
import ca.ubc.cs.cpsc210.translink.util.LatLon;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * A parser for the data returned by Translink stops query
 */
public class StopParser {

    private String filename;

    public StopParser(String filename) {
        this.filename = filename;
    }
    /**
     * Parse stop data from the file and add all stops to stop manager.
     *
     */
    public void parse() throws IOException, StopDataMissingException, JSONException{
        DataProvider dataProvider = new FileDataProvider(filename);

        parseStops(dataProvider.dataSourceToString());
    }
    /**
     * Parse stop information from JSON response produced by Translink.
     * Stores all stops and routes found in the StopManager and RouteManager.
     *
     * @param  jsonResponse    string encoding JSON data to be parsed
     * @throws JSONException when
     *     JSON data does not have expected format
     *     JSON data is not an array
     * @throws StopDataMissingException when
     *     JSON data is missing Name, StopNo, Routes or location (Latitude or Longitude) elements for any stop
     */

    public void parseStops(String jsonResponse)
            throws JSONException, StopDataMissingException {
        JSONArray stops = new JSONArray(jsonResponse);
        for (int index = 0; index < stops.length(); index++) {
            JSONObject stop = stops.getJSONObject(index);
            parseOneStop(stop);
        }
    }

    public void parseOneStop(JSONObject stop) throws JSONException, StopDataMissingException {
        if(!(stop.has("Name")) || !(stop.has("StopNo")) || !(stop.has("Latitude")) || !(stop.has("Longitude")) || !(stop.has("Routes")))
            throw new StopDataMissingException();
        String name = stop.getString("Name");
        int stopNo = Integer.parseInt(stop.get("StopNo").toString());
        Double lat = Double.parseDouble(stop.get("Latitude").toString());
        Double lon = Double.parseDouble(stop.get("Longitude").toString());
        Stop aStop = StopManager.getInstance().getStopWithId(stopNo, name, new LatLon(lat ,lon));
        String routes = stop.get("Routes").toString();
        String[] split = routes.split(", ");
        for (String s : split) {
            aStop.addRoute(RouteManager.getInstance().getRouteWithNumber(s));
        }

    }
}

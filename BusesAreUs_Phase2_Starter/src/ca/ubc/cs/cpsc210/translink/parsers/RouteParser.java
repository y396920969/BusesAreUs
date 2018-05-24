package ca.ubc.cs.cpsc210.translink.parsers;

import ca.ubc.cs.cpsc210.translink.model.Route;
import ca.ubc.cs.cpsc210.translink.model.RouteManager;
import ca.ubc.cs.cpsc210.translink.model.RoutePattern;
import ca.ubc.cs.cpsc210.translink.parsers.exception.RouteDataMissingException;
import ca.ubc.cs.cpsc210.translink.providers.DataProvider;
import ca.ubc.cs.cpsc210.translink.providers.FileDataProvider;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Parse route information in JSON format.
 */
public class RouteParser {
    private String filename;

    public RouteParser(String filename) {
        this.filename = filename;
    }
    /**
     * Parse route data from the file and add all route to the route manager.
     *
     */
    public void parse() throws IOException, RouteDataMissingException, JSONException{
        DataProvider dataProvider = new FileDataProvider(filename);

        parseRoutes(dataProvider.dataSourceToString());
    }
    /**
     * Parse route information from JSON response produced by Translink.
     * Stores all routes and route patterns found in the RouteManager.
     *
     * @param  jsonResponse    string encoding JSON data to be parsed
     * @throws JSONException when
     *     JSON data does not have expected format
     *     JSON data is not an array
     * @throws RouteDataMissingException when
     *     JSON data is missing RouteNo, Name, or Patterns elements for any route
     *     The value of the Patterns element is not an array for any route
     *     JSON data is missing PatternNo, Destination, or Direction elements for any route pattern
     */

    public void parseRoutes(String jsonResponse)
            throws JSONException, RouteDataMissingException {
        JSONArray routes = new JSONArray(jsonResponse);
        for (int index = 0; index < routes.length(); index++) {
            JSONObject route = routes.getJSONObject(index);
            parseOneRoute(route);
        }
    }


    public void parseOneRoute(JSONObject route) throws JSONException, RouteDataMissingException {
        if(!(route.has("Name")) || !(route.has("RouteNo")) || !(route.has("Patterns")))
            throw new RouteDataMissingException();
        String name = route.getString("Name");
        String routeNo = route.getString("RouteNo");

        JSONArray patterns;
        try {
            patterns = new JSONArray(route.get("Patterns").toString());
        } catch (JSONException e){
            throw new RouteDataMissingException();
        }
        Route r = RouteManager.getInstance().getRouteWithNumber(routeNo, name);

        for (int index = 0; index < patterns.length(); index++) {
            JSONObject pattern = patterns.getJSONObject(index);
            if (!(pattern.has("Destination")) || !(pattern.has("Direction")) || !(pattern.has("PatternNo")))
                throw new RouteDataMissingException();
            String destination = pattern.getString("Destination");
            String direction = pattern.getString("Direction");
            String patternNo = pattern.getString("PatternNo");
            RoutePattern rp = r.getPattern(patternNo, destination, direction);
        }
    }
}

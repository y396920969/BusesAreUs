package ca.ubc.cs.cpsc210.translink.parsers;

import ca.ubc.cs.cpsc210.translink.model.Arrival;
import ca.ubc.cs.cpsc210.translink.model.Route;
import ca.ubc.cs.cpsc210.translink.model.RouteManager;
import ca.ubc.cs.cpsc210.translink.model.Stop;
import ca.ubc.cs.cpsc210.translink.parsers.exception.ArrivalsDataMissingException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A parser for the data returned by the Translink arrivals at a stop query
 */
public class ArrivalsParser {

    /**
     * Parse arrivals from JSON response produced by TransLink query.  All parsed arrivals are
     * added to the given stop assuming that corresponding JSON object has a RouteNo: and an
     * array of Schedules:
     * Each schedule must have an ExpectedCountdown, ScheduleStatus, and Destination.  If
     * any of the aforementioned elements is missing, the arrival is not added to the stop.
     *
     * @param stop             stop to which parsed arrivals are to be added
     * @param jsonResponse    the JSON response produced by Translink
     * @throws JSONException  when JSON response does not have expected format
     * @throws ArrivalsDataMissingException  when no arrivals are found in the reply
     */
    public static void parseArrivals(Stop stop, String jsonResponse)
            throws JSONException, ArrivalsDataMissingException {
        JSONArray arrivals = new JSONArray(jsonResponse);
        for (int index = 0; index < arrivals.length(); index++) {
            JSONObject arrival = arrivals.getJSONObject(index);
            parseOneArrival(stop, arrival);
        }
        if(!stop.iterator().hasNext())
            throw new ArrivalsDataMissingException();
    }

    public static void parseOneArrival(Stop stop, JSONObject arrival) throws JSONException, ArrivalsDataMissingException {
        if (arrival.has("RouteNo")) {
            String routeNo = arrival.get("RouteNo").toString();
            Route r = RouteManager.getInstance().getRouteWithNumber(routeNo);
            JSONArray schedules = new JSONArray(arrival.get("Schedules").toString());
            for (int index = 0; index < schedules.length(); index++) {
                JSONObject schedule = schedules.getJSONObject(index);
                parseOneSchedule(r, stop, schedule);
            }
        }
    }

    public static void parseOneSchedule(Route route, Stop stop, JSONObject schedule) throws JSONException{
        if(schedule.has("Destination") && schedule.has("ExpectedCountdown") && schedule.has("ScheduleStatus")){
            Arrival a = new Arrival(Integer.parseInt(schedule.get("ExpectedCountdown").toString()), schedule.get("Destination").toString(),
            route);
            a.setStatus(schedule.get("ScheduleStatus").toString());
            stop.addArrival(a);
        }
    }
}

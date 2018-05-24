package ca.ubc.cs.cpsc210.translink.parsers;

import ca.ubc.cs.cpsc210.translink.model.Route;
import ca.ubc.cs.cpsc210.translink.model.RouteManager;
import ca.ubc.cs.cpsc210.translink.model.RoutePattern;
import ca.ubc.cs.cpsc210.translink.providers.DataProvider;
import ca.ubc.cs.cpsc210.translink.providers.FileDataProvider;
import ca.ubc.cs.cpsc210.translink.util.LatLon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser for routes stored in a compact format in a txt file
 */
public class RouteMapParser {
    private String fileName;

    public RouteMapParser(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Parse the route map txt file
     */
    public void parse() {
        DataProvider dataProvider = new FileDataProvider(fileName);
        try {
            String c = dataProvider.dataSourceToString();
            if (!c.equals("")) {
                int posn = 0;
                while (posn < c.length()) {
                    int endposn = c.indexOf('\n', posn);
                    String line = c.substring(posn, endposn);
                    parseOnePattern(line);
                    posn = endposn + 1;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Parse one route pattern, adding it to the route that is named within it
     * @param str
     */
    private void parseOnePattern(String str) {
        String routeNumber;
        String patternName;
        List<LatLon> elements = new ArrayList<>();
        List<Double> ds = new ArrayList<>();
        routeNumber = str.substring(str.indexOf("N") + 1, str.indexOf("-"));
        patternName = str.substring(str.indexOf("-") + 1, str.indexOf(";"));
        if(!routeNumber.equals("") && !patternName.equals("")) {
            if (!str.substring(str.indexOf(";") + 1).equals("")) {
                String[] split = str.substring(str.indexOf(";") + 1).split(";");
                for (String s : split) {
                    double d = Double.parseDouble(s);
                    ds.add(d);
                }
                List<Double> Lats = new ArrayList<>();
                List<Double> Lons = new ArrayList<>();
                for (int a = 0; a < ds.size(); a++) {
                    if ((a % 2) == 0) {
                        Lats.add(ds.get(a));
                    } else Lons.add(ds.get(a));
                }
                int x = 0;
                while (x < ds.size() / 2) {
                    LatLon locn = new LatLon(Lats.get(x), Lons.get(x));
                    elements.add(locn);
                    x++;
                }
            }
            storeRouteMap(routeNumber, patternName, elements);
        }
    }

    /**
     * Store the parsed pattern into the named route
     * Your parser should call this method to insert each route pattern into the corresponding route object
     * There should be no need to change this method
     *
     * @param routeNumber       the number of the route
     * @param patternName       the name of the pattern
     * @param elements          the coordinate list of the pattern
     */
    private void storeRouteMap(String routeNumber, String patternName, List<LatLon> elements) {
        Route r = RouteManager.getInstance().getRouteWithNumber(routeNumber);
        RoutePattern rp = r.getPattern(patternName);
        rp.setPath(elements);
    }
}

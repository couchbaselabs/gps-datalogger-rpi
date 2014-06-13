package com.couchbase.lite.rpi;

import com.couchbase.lite.*;
import com.couchbase.lite.replicator.Replication;
import de.taimos.gpsd4java.api.ObjectListener;
import de.taimos.gpsd4java.backend.GPSdEndpoint;
import de.taimos.gpsd4java.backend.ResultParser;
import de.taimos.gpsd4java.types.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by andy on 22/05/2014.
 */
public class GpsDataLogger {

    static final Logger log = Logger.getLogger(GpsDataLogger.class.getName());

    static Manager manager = null;
    static Database db;

    public static void main(String[] args) {
        System.err.println("Service starting");


        try {
            manager = new Manager(new JavaContext(), new ManagerOptions());
        } catch (IOException e) {
            System.err.println("ERROR: failed to create manager; message: "+e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        System.err.println("Manager created");

        try {
            db = manager.getDatabase("testdb");
        } catch (CouchbaseLiteException e) {
            System.err.println("ERROR: failed to create database; message: "+e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        System.err.println("Database created");

        try {
            Replication pusher = db.createPushReplication(new URL("http://192.168.0.10:4984/db"));
            pusher.setContinuous(true);
            pusher.start();
        } catch (MalformedURLException e) {
            System.err.println("ERROR: creating push replication");
            e.printStackTrace();
            System.exit(1);
        }

        System.err.println("Replication Started");

        try {
            String host = "localhost";
            int port = 2947;

            switch (args.length) {
                case 0:
                    // Nothing to do, use default
                    break;
                case 1:
                    // only server specified
                    host = args[0];
                    break;
                case 2:
                    // Server and port specified
                    host = args[0];
                    if (args[1].matches("\\d+")) {
                        port = Integer.parseInt(args[1]);
                    }
                    break;
                default:
                    break;
            }

            final GPSdEndpoint ep = new GPSdEndpoint(host, port, new ResultParser());

            ep.addListener(new ObjectListener() {

                @Override
                public void handleTPV(final TPVObject tpv) {
                    //GpsDataLogger.log.log(Level.INFO, "TPV: {0}", tpv);

                    Map<String, Object> props = tpvObjectToString(tpv);

                    Document doc = db.createDocument();

                    try {
                        doc.putProperties(props);
                    } catch (CouchbaseLiteException e) {
                        System.err.println("ERROR: failed to create GPC document; message: "+e.getMessage());
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
            });

            ep.start();

            GpsDataLogger.log.log(Level.INFO, "GPSD Version: {0}", ep.version());

            ep.watch(true, true);

            GpsDataLogger.log.log(Level.INFO, "GPS Data Logging started");

        } catch (final Exception e) {
            GpsDataLogger.log.log(Level.SEVERE, null, e);
        }
    }

    private static Map tpvObjectToString(final TPVObject tpv) {
        final Map<String, Object> dict = new HashMap<String, Object>();
        dict.put("tag", tpv.getTag());
        dict.put("device", tpv.getDevice());
        dict.put("timestamp", tpv.getTimestamp());
        dict.put("timestampError", tpv.getTimestampError());
        dict.put("latitude", tpv.getLatitude());
        dict.put("longitude", tpv.getLongitude());
        dict.put("altitude=", tpv.getAltitude());
        dict.put("latitudeError", tpv.getLatitudeError());
        dict.put("longitudeError", tpv.getLongitudeError());
        dict.put("altitudeError", tpv.getAltitudeError());
        dict.put("course", tpv.getCourse());
        dict.put("speed", tpv.getSpeed());
        dict.put("climbRate", tpv.getClimbRate());
        dict.put("courseError", tpv.getCourseError());
        dict.put("speedError", tpv.getSpeedError());
        dict.put("climbRateError", tpv.getClimbRateError());
        dict.put("mode", tpv.getMode().name());
        return dict;
    }
}

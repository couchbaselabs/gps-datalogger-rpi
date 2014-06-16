#!/bin/bash

java -cp classes:lib/couchbase-lite-java.jar:lib/commons-codec-1.3.jar:lib/commons-logging-1.1.3.jar:lib/couchbase-lite-java-core.jar:lib/couchbase-lite-java-native.jar:lib/gpsd4java-1.2.0.jar:lib/httpclient-4.0-beta1.jar:lib/httpcore-4.0-beta2.jar:lib/jackson-core-asl-1.9.2.jar:lib/jackson-mapper-asl-1.9.2.jar:lib/json-20090211.jar com.couchbase.lite.rpi.GpsDataLogger

/*
 * Copyright (c) 2008-2018, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.hazelcast.repository.query;

import java.util.Map;

import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metric;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;

import com.hazelcast.query.Predicate;
import com.hazelcast.query.impl.Extractable;

import static com.hazelcast.query.impl.predicates.PredicateUtils.canonicalizeAttribute;

/**
 * Geo Predicate - Used to calculate near and within queries
 * <li>Finds all the Points within the given distance range from source Point.
 * <li>Finds all the Points within given Circle.
 *
 * @param <K> key of map entry
 * @param <V> value of map entry
 * @author Ulhas R Manekar
 */
public class GeoPredicate<K, V>
        implements Predicate<K, V> {

    private static final double KM_TO_MILES = 0.621371;
    private static final double KM_TO_NEUTRAL = 0.539957;
    private static final double R = 6372.8;

    final String attributeName;
    final Point queryPoint;
    final Distance distance;

    /**
     * Constructor accepts the name of the attribute which is of type Point.
     * Constructs a new geo predicate on the given point
     * @param attribute    the name of the attribute in a object within Map which is of type Point.
     * @param point        the source point from where the distance is calculated.
     * @param distance     the Distance object with value and unit of distance.
     */
    public GeoPredicate(String attribute, Point point, Distance distance) {
        this.attributeName = canonicalizeAttribute(attribute);
        this.queryPoint = point;
        this.distance = distance;
    }

    @Override
    public boolean apply(Map.Entry<K, V> mapEntry) {
        Object attributeValue = readAttributeValue(mapEntry);
        if (attributeValue instanceof Point) {
            return compareDistance((Point) attributeValue);
        } else {
            throw new IllegalArgumentException(String.format("Cannot use %s predicate with attribute other than Point",
                    getClass().getSimpleName()));
        }
    }

    private boolean compareDistance(Point point) {
        double calculatedDistance = calculateDistance(point.getX(), point.getY(), this.queryPoint.getX(),
                this.queryPoint.getY(), this.distance.getMetric());
        return calculatedDistance < this.distance.getValue();
    }

    /**
     * This method users Haversine formula to calculate the distance between two points
     * Formula is explained here - https://www.movable-type.co.uk/scripts/gis-faq-5.1.html
     * Sample Java code is here - https://rosettacode.org/wiki/Haversine_formula#Java
     * @param lat1 - Latitude of first point.
     * @param lng1 - Longitude of first point.
     * @param lat2 - Latitude of second point.
     * @param lng2 - Longitude of second point.
     * @param metric - metric to specify where its KILOMETERS, MILES or NEUTRAL
     * @return
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2, Metric metric) {
        if ((lat1 == lat2) && (lng1 == lng2)) {
            return 0;
        } else {
            double dLat = Math.toRadians(lat2 - lat1);
            double dLon = Math.toRadians(lng2 - lng1);
            double lat1Radians = Math.toRadians(lat1);
            double lat2Radians = Math.toRadians(lat2);

            double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1Radians) * Math.cos(lat2Radians);
            double c = 2 * Math.asin(Math.sqrt(a));
            double dist = R * c;

            if (Metrics.MILES.equals(metric)) {
                dist = dist * KM_TO_MILES;
            } else if (Metrics.NEUTRAL.equals(metric)) {
                dist = dist * KM_TO_NEUTRAL;
            }

            return dist;
        }
    }

    private Object readAttributeValue(Map.Entry<K, V> entry) {
        Extractable extractable = (Extractable) entry;
        return extractable.getAttributeValue(this.attributeName);
    }
}

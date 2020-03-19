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

import static com.hazelcast.query.impl.IndexUtils.canonicalizeAttribute;

import java.util.Map;

import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metric;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;

import com.hazelcast.query.Predicate;
import com.hazelcast.query.impl.Extractable;
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

    private static final long serialVersionUID = 1L;
    private static final double MILES_TO_KM = 1.609344;
    private static final double MILES_TO_NEUTRAL = 0.8684;
    private static final double MINUTES_IN_DEGREE = 60;
    private static final double STATUTE_MILES_IN_NAUTICAL_MILE = 1.1515;

    final String attributeName;
    final Point queryPoint;
    final Distance distance;

    /**
     * Constructor accepts the name of the attribute which is of type Point.
     * Constructs a new geo predicate on the given point
     *
     * @param attribute    the name of the attribute in a object within Map which is of type Point.
     * 
     * @param point        the source point from where the distance is calculated.
     * 
     * @param distance     the Distance object with value and unit of distance.
     * 
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
        final boolean withinRange = calculatedDistance < this.distance.getValue();
        return withinRange;
    }

    private double calculateDistance(double lat1, double lng1, double lat2, double lng2, Metric metric) {
        if ((lat1 == lat2) && (lng1 == lng2)) {
            return 0;
        } else {
            double theta = lng1 - lng2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1))
            * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * MINUTES_IN_DEGREE * STATUTE_MILES_IN_NAUTICAL_MILE;
            if (Metrics.KILOMETERS.equals(metric)) {
                dist = dist * MILES_TO_KM;
            } else if (Metrics.NEUTRAL.equals(metric)) {
                dist = dist * MILES_TO_NEUTRAL;
            }
            return (dist);
        }
    }

    private Object readAttributeValue(Map.Entry<K, V> entry) {
        Extractable extractable = (Extractable) entry;
        return extractable.getAttributeValue(this.attributeName);
    }
}

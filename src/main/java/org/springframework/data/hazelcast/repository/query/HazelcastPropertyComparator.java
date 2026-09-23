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

import java.io.Serial;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Map.Entry;

/**
 * <p>
 * Implement a limited form of custom comparison between entries. The fields used for the comparison and the
 * ascending/descending can be specified at run time.
 * </P>
 * <p>
 * The attribute is read with plain reflection rather than through Hazelcast's {@code ReflectionHelper}, whose
 * {@code extractValue} method is internal API and has changed shape in 4.1 and again in 5.7. Only a single
 * property name has to be resolved here, because {@link HazelcastSortAccessor} rejects nested paths before a
 * comparator is ever built.
 * </P>
 *
 * @author Neil Stevenson
 */
public class HazelcastPropertyComparator
        implements Comparator<Entry<?, ?>>, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String attributeName;
    private final int direction;

    /**
     * Resolving the accessor is comparatively expensive and a comparator is invoked once per comparison,
     * so remember the last one. Not serialized, as it is rebuilt on the member that does the sorting.
     */
    private transient Class<?> accessorType;
    private transient Object accessor;

    public HazelcastPropertyComparator(String attributeName, boolean ascending) {
        this.attributeName = attributeName;
        this.direction = (ascending ? 1 : -1);
    }

    /**
     * <p>
     * Extract the named attribute from each entry, and use this in the comparison.
     * </P>
     *
     * @param o1 An entry in a map
     * @param o2 Another entry in the map
     * @return Comparison result
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public int compare(Entry<?, ?> o1, Entry<?, ?> o2) {

        try {

            Object o1Field = this.extractValue(o1.getValue());
            Object o2Field = this.extractValue(o2.getValue());

            if (o1Field == o2Field) {
                return 0;
            }
            if (o1Field == null) {
                return this.direction;
            }
            if (o2Field == null) {
                return -1 * this.direction;
            }
            if (o1Field instanceof Comparable && o2Field instanceof Comparable) {
                return this.direction * ((Comparable) o1Field).compareTo(o2Field);
            }

        } catch (Exception ex) {
            return 0;
        }

        return 0;
    }

    /**
     * Read {@link #attributeName} from the given object, preferring a getter over direct field access, in the
     * same order of preference that Hazelcast itself applies.
     *
     * @param target The value side of a map entry, possibly null
     * @return The attribute value, possibly null
     * @throws ReflectiveOperationException If the attribute cannot be found or read
     */
    private Object extractValue(Object target)
            throws ReflectiveOperationException {

        if (target == null) {
            return null;
        }

        Object accessorToUse = this.resolveAccessor(target.getClass());

        if (accessorToUse instanceof Method method) {
            return method.invoke(target);
        }
        return ((Field) accessorToUse).get(target);
    }

    private Object resolveAccessor(Class<?> targetType)
            throws ReflectiveOperationException {

        if (targetType.equals(this.accessorType) && this.accessor != null) {
            return this.accessor;
        }

        try {
            PropertyDescriptor propertyDescriptor = new PropertyDescriptor(this.attributeName, targetType);
            Method method = propertyDescriptor.getReadMethod();
            if (method != null) {
                method.setAccessible(true);
                return this.rememberAccessor(targetType, method);
            }
        } catch (IntrospectionException ignore) {}

        for (Class<?> klass = targetType; klass != null; klass = klass.getSuperclass()) {
            try {
                Field field = klass.getDeclaredField(this.attributeName);
                field.setAccessible(true);
                return this.rememberAccessor(targetType, field);
            } catch (NoSuchFieldException ignore) {}
        }

        throw new NoSuchFieldException(String.format("No attribute '%s' on '%s'", this.attributeName, targetType));
    }

    private Object rememberAccessor(Class<?> targetType, Object accessorToUse) {
        this.accessorType = targetType;
        this.accessor = accessorToUse;
        return accessorToUse;
    }
}

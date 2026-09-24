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

import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.beans.IntrospectionException;
import java.beans.Introspector;
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
 * The attribute is read with JavaBeans introspection and reflection rather than through Hazelcast's
 * {@code ReflectionHelper}, whose {@code extractValue} method is internal API and has changed shape in 4.1
 * and again in 5.7. Only a single property name has to be resolved here, because {@link HazelcastSortAccessor}
 * rejects nested paths before a comparator is ever built.
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
     * so remember the last one in a thread-safe cache. Not serialized, as it is rebuilt on the member
     * that does the sorting.
     */
    private transient volatile ResolvedAccessor resolvedAccessor;

    public HazelcastPropertyComparator(String attributeName, boolean ascending) {
        this.attributeName = attributeName;
        this.direction = (ascending ? 1 : -1);
    }

    /**
     * Extract the named attribute from each entry, and use this in the comparison.
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
                // Avoid overflow when reversing the comparison.
                return this.direction * Integer.signum(((Comparable) o1Field).compareTo(o2Field));
            }

        } catch (Exception ex) {
            return 0;
        }

        return 0;
    }

    /**
     * Read {@link #attributeName} from the given object, preferring a JavaBeans read method over direct
     * field access.
     *
     * @param target The value side of a map entry, possibly null
     * @return The attribute value, possibly null
     * @throws ReflectiveOperationException If the attribute cannot be found or read
     */
    @Nullable
    private Object extractValue(@Nullable Object target)
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

    /**
     * The check and the cache write are deliberately not atomic. Two threads racing on the same type may both
     * resolve it, which is accepted rather than overlooked, because the work is idempotent and cheap while
     * making it atomic would add locking to a method called once per comparison. The race is safe because
     * {@link ResolvedAccessor} is immutable and published by a single volatile write, so a reader never sees a
     * type from one resolution beside a member from another.
     */
    private Object resolveAccessor(Class<?> targetType)
            throws ReflectiveOperationException {

        ResolvedAccessor current = this.resolvedAccessor;
        if (current != null && targetType.equals(current.type)) {
            return current.member;
        }

        Method readMethod = this.findReadMethod(targetType);
        if (readMethod != null) {
            readMethod.setAccessible(true);
            return this.rememberAccessor(targetType, readMethod);
        }

        for (Class<?> klass = targetType; klass != null; klass = klass.getSuperclass()) {
            try {
                Field field = klass.getDeclaredField(this.attributeName);
                field.setAccessible(true);
                return this.rememberAccessor(targetType, field);
            } catch (NoSuchFieldException ignore) {}
        }

        throw new NoSuchFieldException(String.format("No attribute '%s' on '%s'", this.attributeName, targetType));
    }

    /**
     * Finds the getter for {@link #attributeName}, including read-only and computed properties.
     * Uses {@link Introspector}, which honours custom {@code BeanInfo}.
     *
     * @param targetType The type to introspect
     * @return The getter, or {@literal null} if none is found
     */
    private Method findReadMethod(Class<?> targetType) {
        try {
            for (PropertyDescriptor descriptor : Introspector.getBeanInfo(targetType).getPropertyDescriptors()) {
                if (this.attributeName.equals(descriptor.getName())) {
                    return descriptor.getReadMethod();
                }
            }
        } catch (IntrospectionException ignore) {}
        return null;
    }

    private Object rememberAccessor(Class<?> targetType, Object accessorToUse) {
        this.resolvedAccessor = new ResolvedAccessor(targetType, accessorToUse);
        return accessorToUse;
    }

    /**
     * A resolved accessor together with the type it was resolved against.
     */
    private static final class ResolvedAccessor {
        private final Class<?> type;
        private final Object member;

        ResolvedAccessor(Class<?> type, Object member) {
            this.type = type;
            this.member = member;
        }
    }
}

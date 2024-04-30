package org.springframework.data.hazelcast.repository.support;

import org.springframework.data.domain.Sort;

import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

/**
 * A special {@link Sort} that provides a {@link Comparator} for more optimized sorts (because Reflection which is by default is slow).
 */
public class WithComparatorSort extends Sort {
  private final Comparator<Entry<?, ?>> comparator;

  public WithComparatorSort(
          final List<Order> orders,
          final Comparator<Entry<?, ?>> comparator) {
    super(orders);
    this.comparator = comparator;
  }

  public Comparator<Entry<?, ?>> getComparator() {
    return comparator;
  }
}

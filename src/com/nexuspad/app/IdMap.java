package com.nexuspad.app;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.HashMap;
import java.util.Map;

public final class IdMap<T> {
    private Map<T, Long> mMap = new HashMap<T, Long>();
    private long mLastId;

    /**
     * adds the item to the mapping if the map does not already contain it;</br>
     * generates an incremental long ID for each unique item
     */
    public void addIfAbsent(T item) {
        if (!mMap.containsKey(item)) {
            mLastId += 1;
            mMap.put(item, mLastId);
        }
    }

    public void addIf(T item, Predicate<? super T> predicate) {
        if (!Iterables.tryFind(mMap.keySet(), predicate).isPresent()) {
            mLastId += 1;
            mMap.put(item, mLastId);
        }
    }

    /**
     * retrieve the id added by {@link #addIfAbsent(T)} previously
     *
     * @param item the key, must be added previously
     * @return the long id
     * @throws IllegalStateException if the key does not exist in the map
     */
    public long getId(T item) {
        final Long id = mMap.get(item);
        if (id == null) {
            throw new IllegalStateException("no mapping for item: " + item);
        }
        return id;
    }

    public long getIdIf(T item, final Predicate<? super T> predicate) {
        final Optional<Map.Entry<T, Long>> entry = Iterables.tryFind(mMap.entrySet(), new Predicate<Map.Entry<T, Long>>() {
            @Override
            public boolean apply(Map.Entry<T, Long> input) {
                return predicate.apply(input.getKey());
            }
        });
        if (entry.isPresent()) {
            return entry.get().getValue();
        } else {
            throw new IllegalStateException("no mapping for item: " + item);
        }
    }
}

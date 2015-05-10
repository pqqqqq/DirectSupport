package com.pqqqqq.directsupport.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kevin on 2015-05-03.
 * A class that maps a generic type T's instances of U.
 */
public class MappedManager<T, U> {
    private Map<T, U> map = new HashMap<T, U>();

    /*public U getOrAdd(T key) {
        U value = getValue(key);
        if (value != null) {
            return value;
        }

        map.put(key, clone);
        return clone;
    }*/

    public U add(T key, U value) {
        return map.put(key, value);
    }

    public U getValue(T key) {
        return map.get(key);
    }

    public U remove(T key) {
        return map.remove(key);
    }

    public boolean contains(T key) {
        return map.containsKey(key);
    }

    public int size() {
        return map.size();
    }

    public void clear() {
        map.clear();
    }

    public Map<T, U> getMap() {
        return map;
    }
}

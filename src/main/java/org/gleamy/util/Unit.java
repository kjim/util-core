package org.gleamy.util;

public class Unit {
    private Unit() {}

    private static final Unit instance = new Unit();

    public Unit getInstance() {
        return instance;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof Unit;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}

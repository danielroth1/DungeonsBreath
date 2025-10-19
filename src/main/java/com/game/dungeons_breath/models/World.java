package com.game.dungeons_breath.models;

import java.util.ArrayList;
import java.util.List;

public class World {

    private double width;
    private double height;
    private List<Unit> units;

    public World(double width, double height) {
        this.width = width;
        this.height = height;
        units = new ArrayList<Unit>();
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public List<Unit> getUnits() {
        return units;
    }

    public void moveUnit(Unit unit, double dx, double dy) {
        unit.setX(Math.max(0, Math.min(width - unit.getWidth(), unit.getX() + dx)));
        unit.setY(Math.max(0, Math.min(height - unit.getHeight(), unit.getY() + dy)));
    }

    public List<Unit> getUnitsInside(double x, double y) {
        List<Unit> collidingUnits = new ArrayList<Unit>();
        for (Unit unit : units) {
            if (isInside(unit, x, y))
                collidingUnits.add(unit);
        }
        return collidingUnits;
    }

    /**
     * @param unit
     * @return all units colliding with the given unit.
     */
    public List<Unit> getUnitsColliding(Unit unit) {
        List<Unit> collidingUnits = new ArrayList<Unit>();
        for (Unit u : units) {
            if (u != unit && isColliding(u, unit))
                collidingUnits.add(u);
        }
        return collidingUnits;
    }

    /**
     * @param unit1
     * @param unit2
     * @return if unit1 and unit2 are colliding with each other.
     */
    public boolean isColliding(Unit unit1, Unit unit2) {
        double x1 = unit1.getX();
        double xw1 = unit1.getX() + unit1.getWidth();
        double x2 = unit2.getX();
        double xw2 = unit2.getX() + unit2.getWidth();
        if (xw1 < x2 || xw2 < x1)
            return false;

        double y1 = unit1.getY();
        double yw1 = unit1.getY() + unit1.getHeight();
        double y2 = unit2.getY();
        double yw2 = unit2.getY() + unit2.getHeight();
        if (yw1 < y2 || yw2 < y1)
            return false;

        return true;
    }

    private boolean isInside(Unit unit, double x, double y) {
        return unit.getX() <= x && x <= unit.getX() + unit.getWidth() &&
                unit.getY() <= y && y <= unit.getY() + unit.getHeight();
    }
}

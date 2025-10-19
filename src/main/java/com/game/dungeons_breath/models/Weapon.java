package com.game.dungeons_breath.models;

import java.util.ArrayList;
import java.util.List;

public class Weapon {

    private int magazinSize;
    private int numBulletsInMagazine;
    private int reloadingTimeMs;
    private int reloadingTimeLeftMs; // time left of current reloading
    private boolean isReloading; // reloading state
    private List<IWeaponListener> listeners;

    public Weapon(int magazinSize, int numBulletsInMagazine, int reloadingTimeMs) {
        this.magazinSize = magazinSize;
        this.numBulletsInMagazine = numBulletsInMagazine;
        this.reloadingTimeMs = reloadingTimeMs;
        isReloading = false;
        listeners = new ArrayList<IWeaponListener>();
    }

    public void addListener(IWeaponListener listener) {
        listeners.add(listener);
    }

    public void update(int timePassedDeltaMs) {
        if (numBulletsInMagazine == 0 && !isReloading) { // start reloading
            isReloading = true;
            reloadingTimeLeftMs = reloadingTimeMs;
            for (IWeaponListener listener : listeners) {
                listener.OnWeaponStartReloading();
            }
        }

        if (isReloading) {
            reloadingTimeLeftMs -= timePassedDeltaMs;
        }

        if (reloadingTimeLeftMs <= 0 && isReloading) { // reloading done
            isReloading = false;
            numBulletsInMagazine = magazinSize;
        }
    }

    public boolean isReloading() {
        return isReloading;
    }

    public int getMagazinSize() {
        return magazinSize;
    }

    public int getNumBulletsInMagazine() {
        return numBulletsInMagazine;
    }

    public void setNumBulletsInMagazine(int numBulletsInMagazine) {
        this.numBulletsInMagazine = numBulletsInMagazine;
    }
}

package com.juniper.game.systems;

import com.badlogic.ashley.systems.IntervalSystem;
import com.juniper.game.GdxWorldData;

public class TimedSystem extends IntervalSystem{

    GdxWorldData gdxWorldData;


    public TimedSystem(float interval, GdxWorldData gdxWorldData) {
        super(interval);
        this.gdxWorldData = gdxWorldData;
    }

    @Override
    protected void updateInterval() {
        gdxWorldData.printEntities();
    }
}

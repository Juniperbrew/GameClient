package com.juniper.game.systems;

import com.badlogic.ashley.systems.IntervalSystem;
import com.juniper.game.GameClient;
import com.juniper.game.GdxWorldData;

public class TimedSystem extends IntervalSystem{

    GdxWorldData gdxWorldData;
    GameClient gameClient;


    public TimedSystem(float interval, GdxWorldData gdxWorldData, GameClient gameClient) {
        super(interval);
        this.gdxWorldData = gdxWorldData;
        this.gameClient = gameClient;
    }

    @Override
    protected void updateInterval() {
        //gdxWorldData.printEntities();
        gdxWorldData.dumpData();
        System.out.println("Mapchange duration:"+ gameClient.mapChangeDuration/1000f+"us");
        System.out.println("Sync duration:"+ gameClient.syncDuration/1000f+"us");
        System.out.println("Update duration:"+ gameClient.updateDuration/1000f+"us");
        System.out.println("GUI update duration:"+ gameClient.guiUpdateDuration/1000f+"us");
    }
}

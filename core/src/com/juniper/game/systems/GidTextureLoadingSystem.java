package com.juniper.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.juniper.game.GdxWorldData;
import com.juniper.game.Mappers;
import com.juniper.game.components.Gid;
import com.juniper.game.components.Sprite;

public class GidTextureLoadingSystem extends IteratingSystem {

    GdxWorldData gdxWorldData;

    public GidTextureLoadingSystem(Family family, GdxWorldData gdxWorldData) {
        super(family);
        this.gdxWorldData = gdxWorldData;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        int gid = Mappers.gidM.get(entity).gid;
        TextureRegion texture = gdxWorldData.getActiveMap().getTileSets().getTile(gid).getTextureRegion();

        //Engine seems to notify listeners that an entity has been added when the components change
        entity.add(new Sprite(texture));
        entity.remove(Gid.class);
        System.out.println("Found texture for " + Mappers.nameM.get(entity).name);
    }
}

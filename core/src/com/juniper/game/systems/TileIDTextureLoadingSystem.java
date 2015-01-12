package com.juniper.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.juniper.game.GdxWorldData;
import com.juniper.game.Mappers;
import com.juniper.game.components.shared.TileID;
import com.juniper.game.components.client.Sprite;

public class TileIDTextureLoadingSystem extends IteratingSystem {

    GdxWorldData gdxWorldData;

    public TileIDTextureLoadingSystem(Family family, GdxWorldData gdxWorldData) {
        super(family);
        this.gdxWorldData = gdxWorldData;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        //FIXME it seems the libtiled tileIDs start from 0 while in libgdx they start from 1 matching the .tmx file
        //so we need to add 1 to the tileID sent from the server which used libtiled to read the value
        int tileID = Mappers.tileidM.get(entity).id+1;
        TextureRegion texture = gdxWorldData.getActiveMap().getTileSets().getTile(tileID).getTextureRegion();

        //Engine seems to notify listeners that an entity has been added when the components change
        entity.add(new Sprite(texture));
        entity.remove(TileID.class);
        System.out.println("Found texture for " + Mappers.nameM.get(entity).name);
    }
}

package com.juniper.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.juniper.game.Mappers;
import com.juniper.game.components.Position;

public class TextureRenderingSystem extends IteratingSystem {

    Batch batch;

    public TextureRenderingSystem(Family family, Batch batch){
        super(family);
        this.batch = batch;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Position position = Mappers.positionM.get(entity);
        TextureRegion texture = Mappers.spriteM.get(entity).texture;
        //FIXME figure out a way to batch these calls
        batch.begin();
        batch.draw(texture, position.x,position.y);
        batch.end();
    }
}

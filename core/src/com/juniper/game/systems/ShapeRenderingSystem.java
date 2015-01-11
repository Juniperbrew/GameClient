package com.juniper.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.juniper.game.GdxWorldData;
import com.juniper.game.Mappers;
import com.juniper.game.components.Position;

public class ShapeRenderingSystem extends IteratingSystem {

    ShapeRenderer shapeRenderer;

    public ShapeRenderingSystem(Family family, ShapeRenderer shapeRenderer) {
        super(family);
        this.shapeRenderer = shapeRenderer;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Position position = Mappers.positionM.get(entity);
        //Dont draw rectangle if the entity has a sprite
        if(Mappers.spriteM.get(entity) == null){
            //FIXME figure out a way to batch these calls
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.rect(position.x, position.y, 10, 10);
            shapeRenderer.end();
        }
    }
}

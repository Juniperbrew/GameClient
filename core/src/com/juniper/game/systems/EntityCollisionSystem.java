package com.juniper.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.juniper.game.GdxWorldData;
import com.juniper.game.Mappers;
import com.juniper.game.components.client.Movement;
import com.juniper.game.components.shared.Bounds;
import com.juniper.game.components.shared.Position;

import java.util.Vector;

/**
 * Created by Juniperbrew on 14.1.2015.
 */
public class EntityCollisionSystem extends ListeningEntitySystem {

    GdxWorldData gdxWorldData;

    public EntityCollisionSystem(Family family, GdxWorldData gdxWorldData) {
        super(family);
        this.gdxWorldData = gdxWorldData;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Position position = Mappers.positionM.get(entity);
        Movement movement = Mappers.movementM.get(entity);
        Position newPosition = new Position(position.x+movement.deltaX,position.y+movement.deltaY);
        Bounds bounds = Mappers.boundsM.get(entity);
        Rectangle playerRectXMovement = new Rectangle(position.x+movement.deltaX, position.y, bounds.width, bounds.height);
        Rectangle playerRectYMovement = new Rectangle(position.x, position.y+movement.deltaY, bounds.width, bounds.height);

        for(Entity e : gdxWorldData.getEntities()){
            if(e.equals(entity)){
                //Dont collide with yourself
                continue;
            }
            Position ePosition = Mappers.positionM.get(e);
            Bounds eBounds = Mappers.boundsM.get(e);
            Rectangle eRect = new Rectangle(ePosition.x,ePosition.y,eBounds.width,eBounds.height);
            Rectangle intersection = new Rectangle();
            if(Intersector.intersectRectangles(eRect,playerRectXMovement, intersection)) {
                movement.deltaX = 0;
                Mappers.movementM.get(e).syncServer = true;
            }
            if(Intersector.intersectRectangles(eRect,playerRectYMovement, intersection)) {
                movement.deltaY = 0;
                Mappers.movementM.get(e).syncServer = true;
            }
        }
    }
}

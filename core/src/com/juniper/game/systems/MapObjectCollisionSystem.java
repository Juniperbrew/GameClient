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

import java.util.Iterator;

/**
 * Created by Juniperbrew on 14.1.2015.
 */
public class MapObjectCollisionSystem extends ListeningEntitySystem {

    GdxWorldData gdxWorldData;
    MapObjects objects;

    public MapObjectCollisionSystem(Family family, GdxWorldData gdxWorldData) {
        super(family);
        MapLayers layers = gdxWorldData.getActiveMap().getLayers();
        for(MapLayer layer : layers){
            if(!(layer instanceof TiledMapTileLayer)){
                objects = layer.getObjects();
            }
        }
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Position position = Mappers.positionM.get(entity);
        Movement movement = Mappers.movementM.get(entity);
        //Position newPosition = new Position(position.x+movement.deltaX,position.y+movement.deltaY);
        Bounds bounds = Mappers.boundsM.get(entity);
        Rectangle playerRectXMovement = new Rectangle(position.x+movement.deltaX, position.y, bounds.width, bounds.height);
        Rectangle playerRectYMovement = new Rectangle(position.x, position.y+movement.deltaY, bounds.width, bounds.height);

        //We only support rectangle objects so this will hopefully crash if we find any other object
        for(MapObject object : objects){
            RectangleMapObject obj = (RectangleMapObject) object;
            if(obj.getProperties().get("blocked") != null){
                //FIXME this could probably be made more efficient
                Rectangle objRect = obj.getRectangle();
                Rectangle intersection = new Rectangle();
                if(Intersector.intersectRectangles(objRect,playerRectXMovement, intersection)) {
                    movement.deltaX = 0;
                }
                if(Intersector.intersectRectangles(objRect,playerRectYMovement, intersection)) {
                    movement.deltaY = 0;
                }
            }
        }
    }
}

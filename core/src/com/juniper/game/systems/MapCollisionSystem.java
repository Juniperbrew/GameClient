package com.juniper.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.utils.Array;
import com.juniper.game.GdxWorldData;
import com.juniper.game.Mappers;
import com.juniper.game.components.client.Movement;
import com.juniper.game.components.shared.Bounds;
import com.juniper.game.components.shared.Position;
import com.juniper.game.util.MapMask;

/**
 * Created by Juniperbrew on 13.1.2015.
 */
public class MapCollisionSystem extends IteratingSystem {

    MapMask solidMask;
    GdxWorldData gdxWorldData;
    Array<TiledMapTileLayer> layers;
    int width;
    int height;

    public MapCollisionSystem(Family family, GdxWorldData gdxWorldData) {
        super(family);
        this.gdxWorldData = gdxWorldData;
        TiledMap map = gdxWorldData.getActiveMap();

        layers = new Array<TiledMapTileLayer>();
        for ( MapLayer rawLayer : map.getLayers() )
        {
            if(rawLayer instanceof TiledMapTileLayer){
                layers.add((TiledMapTileLayer) rawLayer);
            }
        }
        width = layers.get(0).getWidth();
        height = layers.get(0).getHeight();

        solidMask = new MapMask(height,width,layers,"blocked");
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Movement delta = Mappers.movementM.get(entity);
        if(delta.deltaY == 0 && delta.deltaX == 0){
            //No movement no collision
            return;
        }
        Position pos = Mappers.positionM.get(entity);
        Bounds bounds = Mappers.boundsM.get(entity);
        Position newPos = new Position(pos.x+delta.deltaX,pos.y+delta.deltaY);

        //FIXME Is it enough to use the midpoints of each side as collision point?
        //Find mid point on left and right side of collision box and check if they are inside a tile
        if((delta.deltaX > 0 && collides((int)newPos.x+bounds.width,(int)newPos.y+(bounds.height/2)))
                || (delta.deltaX < 0 && collides((int) newPos.x, (int) newPos.y + (bounds.height/2)))){
            delta.deltaX = 0;
            //FIXME reverting move should cause jittering when colliding?
            newPos.x = pos.x;
        }
        //Find mid point on top and bottom side of collision box and check if they are inside a tile
        if((delta.deltaY > 0 && collides((int)newPos.x+(bounds.width/2),(int)newPos.y+bounds.height))
            || (delta.deltaY < 0 && collides((int)newPos.x +(bounds.width/2),(int)newPos.y))){
            delta.deltaY = 0;
        }

    }

    private boolean collides(int x, int y){
        return solidMask.atScreen(x,y);
    }
}

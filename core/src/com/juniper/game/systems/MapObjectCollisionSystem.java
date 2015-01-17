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
import com.esotericsoftware.kryonet.Client;
import com.juniper.game.GdxWorldData;
import com.juniper.game.Mappers;
import com.juniper.game.components.client.Movement;
import com.juniper.game.components.client.PlayerControlled;
import com.juniper.game.components.shared.Bounds;
import com.juniper.game.components.shared.Position;
import com.juniper.game.network.Network;

import java.util.Iterator;

/**
 * Created by Juniperbrew on 14.1.2015.
 */
public class MapObjectCollisionSystem extends ListeningEntitySystem {

    GdxWorldData gdxWorldData;
    MapObjects objects;
    Client client;

    public MapObjectCollisionSystem(Family family, GdxWorldData gdxWorldData, Client client) {
        super(family);
        this.client = client;
        this.gdxWorldData = gdxWorldData;
        objects = gdxWorldData.getObjects();
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Position position = Mappers.positionM.get(entity);
        Movement movement = Mappers.movementM.get(entity);
        Bounds bounds = Mappers.boundsM.get(entity);
        Rectangle playerRectXMovement = new Rectangle(position.x+movement.deltaX, position.y, bounds.width, bounds.height);
        Rectangle playerRectYMovement = new Rectangle(position.x, position.y+movement.deltaY, bounds.width, bounds.height);
        Rectangle playerRect = new Rectangle(position.x+movement.deltaX, position.y+movement.deltaY, bounds.width, bounds.height);

        //We only support rectangle objects so this will hopefully crash if we find any other object
        for(MapObject object : objects){
            RectangleMapObject obj = (RectangleMapObject) object;
            String objType = (String) obj.getProperties().get("type");
            if(objType==null){
                continue;
            }
            if(objType.equals("Collision")){
                //FIXME this could probably be made more efficient
                Rectangle objRect = obj.getRectangle();
                Rectangle intersection = new Rectangle();
                if(Intersector.intersectRectangles(objRect,playerRectXMovement, intersection)) {
                    movement.deltaX = 0;
                }
                if(Intersector.intersectRectangles(objRect,playerRectYMovement, intersection)) {
                    movement.deltaY = 0;
                }
            }else if(objType.equals("Teleport")){
                //Only allow the local player teleport for now
                if(Mappers.playerControlledM.get(entity) != null) {
                    Rectangle objRect = obj.getRectangle();
                    String entrance = obj.getName();
                    //If player is inside teleport square and he wasn't recently teleported to it we teleport him
                    if (objRect.contains(playerRect) && (Mappers.playerControlledM.get(entity).lastTeleportDestination==null)) {
                        String destination = (String) obj.getProperties().get("destination");
                        Rectangle destinationRect = ((RectangleMapObject) objects.get(destination)).getRectangle();
                        int destX = (int) (destinationRect.x + destinationRect.width / 2 - bounds.width / 2);
                        int destY = (int) (destinationRect.y + destinationRect.height / 2 - bounds.height / 2);
                        position.x = destX;
                        position.y = destY;
                        //Player bounding box needs to be updated since there may still be collision checks left in this loop
                        playerRect = new Rectangle(position.x+movement.deltaX, position.y+movement.deltaY, bounds.width, bounds.height);
                        Mappers.playerControlledM.get(entity).lastTeleportDestination = destination;
                    }
                    //If player is no longer overlapping the teleport destination we allow him to teleport back with it
                    if (!objRect.overlaps(playerRect) && entrance.equals(Mappers.playerControlledM.get(entity).lastTeleportDestination)) {
                        Mappers.playerControlledM.get(entity).lastTeleportDestination = null;
                    }
                }
            }else if(objType.equals("Exit")){
                //Only allow the local player exit for now
                if(Mappers.playerControlledM.get(entity) != null) {
                    Rectangle objRect = obj.getRectangle();
                    String entrance = obj.getName();
                    //If player is inside exit square and he wasn't recently moved to it we teleport him
                    if (objRect.contains(playerRect) && (Mappers.playerControlledM.get(entity).lastTeleportDestination==null)) {
                        String destMap = (String) obj.getProperties().get("exit");
                        String destination = (String) obj.getProperties().get("location");

                        Mappers.playerControlledM.get(entity).lastTeleportDestination = destination;

                        Network.GoToMap goToMap = new Network.GoToMap();
                        goToMap.mapName = destMap;
                        goToMap.destinationObject = destination;
                        System.out.println("Moving to " +destination+" in "+ destMap);
                        client.sendTCP(goToMap);
                        //FIXME disable player movement untill map change has been confirmed
                        gdxWorldData.getSystem(PlayerControlSystem.class).setProcessing(false);
                    }
                    //If player is no longer overlapping the teleport destination we allow him to teleport back with it
                    if (!objRect.overlaps(playerRect) && entrance.equals(Mappers.playerControlledM.get(entity).lastTeleportDestination)) {
                        Mappers.playerControlledM.get(entity).lastTeleportDestination = null;
                    }
                }
            }
            if(obj.getProperties().get("blocked") != null){

            }
        }
    }
}

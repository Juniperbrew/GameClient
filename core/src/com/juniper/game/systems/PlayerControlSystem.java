package com.juniper.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.juniper.game.GameClient;
import com.juniper.game.GdxWorldData;
import com.juniper.game.Mappers;
import com.juniper.game.components.client.AnimatedSprite;
import com.juniper.game.components.client.Movement;
import com.juniper.game.components.shared.Bounds;
import com.juniper.game.components.shared.Position;

public class PlayerControlSystem extends ListeningEntitySystem {

    GdxWorldData gdxWorldData;
    GameClient gameClient;

    public PlayerControlSystem(Family family,GdxWorldData gdxWorldData, GameClient gameClient) {
        super(family);
        this.gdxWorldData = gdxWorldData;
        this.gameClient = gameClient;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {

        Position pos = Mappers.positionM.get(entity);
        Bounds bounds = Mappers.boundsM.get(entity);
        Movement movement = Mappers.movementM.get(entity);
        int moveSpeed = 300; //pixels per sec

        AnimatedSprite animatedSprite = Mappers.animatedM.get(entity);

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            movement.deltaX -= moveSpeed*deltaTime;
            animatedSprite.setRotation(270);
            gameClient.showIngameMessage(null);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            movement.deltaX += moveSpeed*deltaTime;
            animatedSprite.setRotation(90);
            gameClient.showIngameMessage(null);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            movement.deltaY += moveSpeed*deltaTime;
            animatedSprite.setRotation(0);
            gameClient.showIngameMessage(null);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            movement.deltaY -= moveSpeed*deltaTime;
            animatedSprite.setRotation(180);
            gameClient.showIngameMessage(null);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.E)) {
            System.out.println("E pressed");
            Rectangle activation = new Rectangle(pos.x,pos.y,bounds.width,bounds.height);
            switch(Mappers.animatedM.get(entity).rotation){
                case 0: activation.y += bounds.height; break;
                case 90: activation.x += bounds.width; break;
                case 180: activation.y -= bounds.height; break;
                case 270: activation.x -= bounds.width; break;
            }
            activateObject(activation);

        }

        //If player moved we update stuff
        if(movement.deltaX != 0 || movement.deltaY != 0){
            animatedSprite.needsStateTimeUpdate = true;
            //Mappers.playerControlledM.get(entity).hasMoved = true;
            Mappers.movementM.get(entity).syncServer = true;
        }
    }

    private void activateObject(Rectangle rect){
        for(MapObject obj : gdxWorldData.getObjects()){
            RectangleMapObject objRect = (RectangleMapObject) obj;
            if(rect.overlaps(objRect.getRectangle())){
                System.out.println(obj.getName()+" activated");
                if(objRect.getProperties().get("message") != null){
                    System.out.println("MESSAGE:"+objRect.getProperties().get("message"));
                    gameClient.showIngameMessage((String) objRect.getProperties().get("message"));
                }
            }
        }
    }
}

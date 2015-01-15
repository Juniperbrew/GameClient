package com.juniper.game.systems;


import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.juniper.game.Mappers;
import com.juniper.game.components.client.AnimatedSprite;
import com.juniper.game.components.shared.Position;

import java.util.Vector;

public class TextureRenderingSystem extends ListeningEntitySystem {

    private Batch batch;

    public TextureRenderingSystem(Family family, Batch batch){
        super(family);
        this.batch = batch;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Position position = Mappers.positionM.get(entity);
        //FIXME figure out a way to batch these calls
        batch.begin();
        //If entity doesnt have sprite component it has a AnimatedSprite component
        if(Mappers.spriteM.get(entity) != null) {
            TextureRegion texture = Mappers.spriteM.get(entity).texture;
            batch.draw(texture, position.x, position.y);
        }else{
            AnimatedSprite animation = Mappers.animatedM.get(entity);
            if(animation.needsStateTimeUpdate){
                animation.addStateTime(deltaTime);
            }else{
                animation.clearStateTime();
            }
            TextureRegion texture = animation.getKeyFrame();
            batch.draw(texture, position.x, position.y);
        }
        batch.end();
    }
}

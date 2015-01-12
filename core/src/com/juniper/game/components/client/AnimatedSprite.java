package com.juniper.game.components.client;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Created by Juniperbrew on 11.1.2015.
 */
public class AnimatedSprite extends Component {
    public Animation animation0;
    public Animation animation90;
    public Animation animation180;
    public Animation animation270;
    public Texture animationSheet;
    TextureRegion[] animationFrames;
    SpriteBatch spriteBatch;
    TextureRegion currentFrame;
    public com.badlogic.gdx.graphics.g2d.Sprite sprite;
    private float stateTime = 0;
    public int rotation = 180;
    public boolean needsStateTimeUpdate;

    public AnimatedSprite(TextureAtlas textureAtlas){
        animation0 = new Animation(1/10f, textureAtlas.findRegions("n"));
        animation90 = new Animation(1/10f, textureAtlas.findRegions("e"));
        animation180 = new Animation(1/10f, textureAtlas.findRegions("s"));
        animation270 = new Animation(1/10f, textureAtlas.findRegions("w"));
    }

    public void addStateTime(float addTime){
        stateTime += addTime;
    }

    public TextureRegion getKeyFrame(){
        switch(rotation){
            case 0: return animation0.getKeyFrame(stateTime, true);
            case 90: return animation90.getKeyFrame(stateTime, true);
            case 180: return animation180.getKeyFrame(stateTime, true);
            case 270: return animation270.getKeyFrame(stateTime, true);
            default: return null;
        }
    }

    public void clearStateTime(){
        stateTime = 0;
    }

    public void setRotation(int degrees){
        rotation = degrees;
    }
}

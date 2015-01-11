package com.juniper.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Sprite extends Component {
    public TextureRegion texture;

    public Sprite() {
    }

    public Sprite(TextureRegion texture) {
        this.texture = texture;
    }
}

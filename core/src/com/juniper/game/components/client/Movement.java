package com.juniper.game.components.client;

import com.badlogic.ashley.core.Component;

/**
 * Created by Juniperbrew on 13.1.2015.
 */
public class Movement extends Component {
    public float deltaX;
    public float deltaY;
    public boolean syncServer;

    public Movement() {
    }

    public Movement(float deltaX, float deltaY) {
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }
}

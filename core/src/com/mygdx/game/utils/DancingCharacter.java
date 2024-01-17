package com.mygdx.game.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class DancingCharacter {
    private Animation<TextureRegion> animation;
    private Image image;
    private float stateTime;

    public DancingCharacter(double institutionLatitude, double institutionLongitude, float beginTileX, float beginTileY, String characterType, int frames) {
        initializeDancingCharacter(institutionLatitude, institutionLongitude, beginTileX, beginTileY, characterType, frames);
    }

    private void initializeDancingCharacter(double institutionLatitude, double institutionLongitude, float beginTileX, float beginTileY, String characterType, int framesNum) {
        TextureRegion[] frames = new TextureRegion[framesNum];
        for (int i = 0; i < framesNum; i++) {
            frames[i] = new TextureRegion(new Texture(Gdx.files.internal("dancing_" + characterType + "_" + (i + 1) + ".png")));
        }

        animation = new Animation<>(0.2f, frames);
        stateTime = 0f;
        Vector2 initialPosition = MapRasterTiles.getPixelPositionFloat(institutionLatitude, institutionLongitude, beginTileX, beginTileY);
        image = new Image(animation.getKeyFrame(stateTime, true));
        image.setPosition(initialPosition.x, initialPosition.y);
    }

    public void update(float delta) {
        stateTime += delta;
        TextureRegion currentFrame = animation.getKeyFrame(stateTime, true);
        image.setDrawable(new TextureRegionDrawable(currentFrame));
    }

    public Image getImage() {
        return image;
    }
}


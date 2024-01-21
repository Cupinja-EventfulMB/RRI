package com.mygdx.game.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;

public class DancingCharacter {
    private Animation<TextureRegion> animation;
    private Image image;
    private float stateTime;
    private ParticleEffect particleEffect;

    public void setParticleEffect(ParticleEffect particleEffect) {
        this.particleEffect = particleEffect;
    }

    public ParticleEffect getParticleEffect() {
        return particleEffect;
    }

    public DancingCharacter(TextureAtlas textureAtlas, double institutionLatitude, double institutionLongitude, float beginTileX, float beginTileY, String characterType, int framesNum) {
        initializeDancingCharacter(textureAtlas, institutionLatitude, institutionLongitude, beginTileX, beginTileY, characterType, framesNum);
    }

    private void initializeDancingCharacter(TextureAtlas textureAtlas, double institutionLatitude, double institutionLongitude, float beginTileX, float beginTileY, String characterType, int framesNum) {
        Array<TextureAtlas.AtlasRegion> frames = new Array<>();
        for (int i = 0; i < framesNum; i++) {
            TextureAtlas.AtlasRegion region = textureAtlas.findRegion("dancing_" + characterType + "_" + (i + 1));
            if (region != null) {
                frames.add(region);
            } else {
                // Handle error when region is not found
                Gdx.app.error("DancingCharacter", "Region not found: dancing_" + characterType + "_" + (i + 1));
            }
        }

        animation = new Animation<TextureRegion>(0.2f, frames);
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

    public float getX() {
        return image.getX();
    }

    public float getY() {
        return image.getY();
    }
}


package com.mygdx.game.utils;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mygdx.game.assets.AssetDescriptors;
import com.mygdx.game.assets.RegionNames;

public class Assets {
    public static final AssetManager assetManager = new AssetManager();
    public static TextureAtlas gameplayAtlas;
    public static TextureRegion institution;

    public static void load() {
        assetManager.load(AssetDescriptors.GAMEPLAY);

        while (!assetManager.update()) {
            float progress = assetManager.getProgress();
           // System.out.println("Loading... " + progress * 100 + "%");
        }

        gameplayAtlas = assetManager.get(AssetDescriptors.GAMEPLAY);
        institution = gameplayAtlas.findRegion(RegionNames.INSTITUTION);

    }

    public static void dispose() {
        assetManager.dispose();
       /* talkieSound.dispose();
        eatingSound.dispose();
        demogorgonSound.dispose();
        shootSound.dispose();
        powerUpSound.dispose();
        font.dispose();*/
    }
}

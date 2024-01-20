package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mygdx.game.assets.AssetDescriptors;
import com.mygdx.game.assets.RegionNames;
import com.mygdx.game.lang.Context;
import com.mygdx.game.lang.LangKt;
import com.mygdx.game.utils.Config;
import com.mygdx.game.utils.Constants;
import com.mygdx.game.utils.DancingCharacter;
import com.mygdx.game.utils.Geolocation;
import com.mygdx.game.utils.Location;
import com.mygdx.game.utils.MapRasterTiles;
import com.mygdx.game.utils.MongoDBManager;
import com.mygdx.game.utils.ZoomXY;

import org.bson.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EMBMap extends ApplicationAdapter implements GestureDetector.GestureListener {
    private MongoDBManager mongoDBManager;
    private ShapeRenderer shapeRenderer;
    private Vector3 touchPosition;

    private TiledMap tiledMap;
    private TiledMapRenderer tiledMapRenderer;
    private OrthographicCamera camera;

    private Texture[] mapTiles;
    private ZoomXY beginTile;   // top left tile

    private SpriteBatch spriteBatch;

    // buttons
    private FitViewport hudViewport;
    private Stage hudStage;
    private Skin skin;
    private boolean showLangExample = false;

    // animation
    private Stage stage;
    private FitViewport viewport;
    private Boolean ranOnce = false;
    private boolean eventAnimationVisible = false;
    private Table currentInfoTable = null;
    List<Location> locations = new ArrayList<>();
    private boolean infoTavleVisible = false;

    // center geolocation
    private final Geolocation CENTER_GEOLOCATION = new Geolocation(46.557314, 15.637771);

    // test marker
    private final Geolocation MARKER_GEOLOCATION = new Geolocation(46.559070, 15.638100);
    // marker and dialog institution
    private Array<TextureRegion> markerInstitutionTextures;
    private Dialog markerInfoDialog;
    // animations
    private List<DancingCharacter> dancingCharacters;
    private boolean isZooming = false;
    private float targetZoom;
    private float zoomTimer = 0; // elapsed time during the zoom effect
    private float targetX = 0;
    private float targetY = 0;
    private boolean isZoomedIn = false;
    public static final AssetManager assetManager = new AssetManager();
    private TextureAtlas gameplayAtlas;
    private Music danceMusic;



    private void loadTexturesAndSkin() {
        markerInstitutionTextures = new Array<>();
       // markerInstitutionTextures.add(new TextureRegion.findRegion(RegionNames.INSTITUTION));

        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

    }

    @Override
    public void create() {
        // connect to MongoDB
        String mongodbUrl = Config.getMongoDBUrl();

        // connect to MongoDB using the retrieved URL
        mongoDBManager = new MongoDBManager(mongodbUrl, "cupinja");

        MapRasterTiles.loadTileCache();

        if (mongoDBManager.testConnection()) {
            System.out.println("Connected to MongoDB successfully!");

            MongoDatabase database = mongoDBManager.getDatabase();

            MongoCollection<Document> collection = database.getCollection("locations");

            FindIterable<Document> documents = collection.find();
            for (Document document : documents) {
                String institution = document.getString("institution");
                String city = document.getString("city");
                String street = document.getString("street");
                String description = document.getString("description");
                String email = document.getString("email");

                Double lat = document.getDouble("x");
                Double lng = document.getDouble("y");

                if (lat != null && lng != null) {
                    Geolocation geolocation = new Geolocation(lat, lng);
                    Location location = new Location(institution, city, street, geolocation,description, email);
                    locations.add(location);
                } else {
                    System.out.println("Skipping document with missing or null lat/lng values.");
                }
            }

        } else {
            System.out.println("Failed to connect to MongoDB.");
        }

        for (Location location : locations) {
            System.out.println("Location:");
            System.out.println("Institution: " + location.getInstitution());
            System.out.println("City: " + location.getCity());
            System.out.println("Street: " + location.getStreet());
            System.out.println("Geolocation: " + location.getGeolocation().lat + ", " + location.getGeolocation().lng);
            System.out.println("-------------");
        }

        assetManager.load(AssetDescriptors.GAMEPLAY);
        assetManager.load(AssetDescriptors.DANCE_MUSIC);
        assetManager.finishLoading();
        gameplayAtlas = assetManager.get(AssetDescriptors.GAMEPLAY);

        loadTexturesAndSkin();

        danceMusic = assetManager.get(AssetDescriptors.DANCE_MUSIC);

        markerInstitutionTextures.add(gameplayAtlas.findRegion(RegionNames.INSTITUTION));

        shapeRenderer = new ShapeRenderer();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.MAP_WIDTH, Constants.MAP_HEIGHT);
        camera.position.set(Constants.MAP_WIDTH / 2f, Constants.MAP_HEIGHT / 2f, 0);
        camera.viewportWidth = Constants.MAP_WIDTH / 2f;
        camera.viewportHeight = Constants.MAP_HEIGHT / 2f;
        camera.zoom = 2f;
        camera.update();

        spriteBatch = new SpriteBatch();
        hudViewport = new FitViewport(Constants.HUD_WIDTH, Constants.HUD_HEIGHT);
        viewport = new FitViewport(Constants.MAP_WIDTH / 2f, Constants.MAP_HEIGHT / 2f, camera);

        touchPosition = new Vector3();

        try {
            System.out.println("Cache size before fetching tiles: " + MapRasterTiles.getTileCacheSize());

            ZoomXY centerTile = MapRasterTiles.getTileNumber(CENTER_GEOLOCATION.lat, CENTER_GEOLOCATION.lng, Constants.ZOOM);
            mapTiles = MapRasterTiles.getRasterTileZone(centerTile, Constants.NUM_TILES);
            beginTile = new ZoomXY(Constants.ZOOM, centerTile.x - ((Constants.NUM_TILES - 1) / 2), centerTile.y - ((Constants.NUM_TILES - 1) / 2));

            System.out.println("Cache size after fetching tiles: " + MapRasterTiles.getTileCacheSize());
        } catch (IOException e) {
            e.printStackTrace();
        }

        tiledMap = new TiledMap();
        MapLayers layers = tiledMap.getLayers();

        TiledMapTileLayer layer = new TiledMapTileLayer(Constants.NUM_TILES, Constants.NUM_TILES, MapRasterTiles.TILE_SIZE, MapRasterTiles.TILE_SIZE);
        int index = 0;
        for (int j = Constants.NUM_TILES - 1; j >= 0; j--) {
            for (int i = 0; i < Constants.NUM_TILES; i++) {
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                cell.setTile(new StaticTiledMapTile(new TextureRegion(mapTiles[index], MapRasterTiles.TILE_SIZE, MapRasterTiles.TILE_SIZE)));
                layer.setCell(i, j, cell);
                index++;
            }
        }
        layers.add(layer);

        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);

        // buttons
        hudStage = new Stage(hudViewport, spriteBatch);
        hudStage.addActor(createButtons());

        Gdx.input.setInputProcessor(new InputMultiplexer(hudStage, new GestureDetector(this)));

        stage = new Stage(viewport, spriteBatch);

        //animation
        dancingCharacters = new ArrayList<>();

    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 1);

        handleInput();

        camera.update();

        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();

        drawMarkers(spriteBatch);

        hudStage.act(Gdx.graphics.getDeltaTime());
        stage.act(Gdx.graphics.getDeltaTime());

        hudStage.draw();
        stage.draw();

        // lang
        if (showLangExample) {
            LangKt.run(new Context(shapeRenderer, camera, beginTile));
            ranOnce = true;
        }

        for (DancingCharacter dancingCharacter : dancingCharacters) {
            dancingCharacter.update(Gdx.graphics.getDeltaTime());
        }
    }

    private void initializeDancingCharacters(){
        for (Location location : locations) {
            String institutionName = location.getInstitution();
            if ("Festivalna dvorana Lent Maribor".equals(institutionName)) {
                DancingCharacter dancingMan = new DancingCharacter(gameplayAtlas, location.getGeolocation().lat, location.getGeolocation().lng, beginTile.x, beginTile.y - 0.02f, "man", 2);
                DancingCharacter dancingWoman = new DancingCharacter(gameplayAtlas, location.getGeolocation().lat, location.getGeolocation().lng, beginTile.x + 0.1f, beginTile.y, "woman", 5);
                dancingCharacters.add(dancingMan);
                dancingCharacters.add(dancingWoman);
                stage.addActor(dancingMan.getImage());
                stage.addActor(dancingWoman.getImage());
            }
            if ("Dvorana Tabor".equals(institutionName)) {
                DancingCharacter dancingMan = new DancingCharacter(gameplayAtlas, location.getGeolocation().lat, location.getGeolocation().lng, beginTile.x - 0.2f, beginTile.y - 0.02f, "man2", 2);
                DancingCharacter dancingWoman = new DancingCharacter(gameplayAtlas, location.getGeolocation().lat, location.getGeolocation().lng, beginTile.x + 0.1f, beginTile.y, "woman2", 3);
                dancingCharacters.add(dancingMan);
                dancingCharacters.add(dancingWoman);
                stage.addActor(dancingMan.getImage());
                stage.addActor(dancingWoman.getImage());
            }
            if ("SNG".equals(institutionName)) {
                DancingCharacter dancingMan = new DancingCharacter(gameplayAtlas, location.getGeolocation().lat, location.getGeolocation().lng, beginTile.x, beginTile.y - 0.02f, "man3", 2);
                DancingCharacter dancingWoman = new DancingCharacter(gameplayAtlas, location.getGeolocation().lat, location.getGeolocation().lng, beginTile.x - 0.1f, beginTile.y - 0.02f, "woman3", 2);
                dancingCharacters.add(dancingMan);
                dancingCharacters.add(dancingWoman);
                stage.addActor(dancingMan.getImage());
                stage.addActor(dancingWoman.getImage());
            }
            if ("Stuk".equals(institutionName)) {
                DancingCharacter dancingPair = new DancingCharacter(gameplayAtlas, location.getGeolocation().lat, location.getGeolocation().lng, beginTile.x - 0.2f, beginTile.y - 0.02f, "pair", 3);
                dancingCharacters.add(dancingPair);
                stage.addActor(dancingPair.getImage());
            }
        }
    }

    private void clearDancingCharacters() {
        for (DancingCharacter dancingCharacter : dancingCharacters) {
            dancingCharacter.getImage().remove();
        }
        dancingCharacters.clear();
    }

    private void drawMarkers(SpriteBatch spriteBatch) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.setAutoShapeType(true);

        spriteBatch.begin();

        for (Location location : locations) {
            Geolocation geolocation = location.getGeolocation();
            Vector2 marker = MapRasterTiles.getPixelPosition(geolocation.lat, geolocation.lng, beginTile.x, beginTile.y);
            spriteBatch.draw(markerInstitutionTextures.first(), marker.x, marker.y, 100, 100);

            // the marker is clicked
            if (Gdx.input.justTouched()) {
                Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                camera.unproject(touchPos);

                float markerWidth = 100;
                float markerHeight = 100;

                if (touchPos.x >= marker.x && touchPos.x <= marker.x + markerWidth &&
                        touchPos.y >= marker.y && touchPos.y <= marker.y + markerHeight) {
                    if(eventAnimationVisible){
                        if (isZoomedIn) {
                            zoomOut();
                        } else {
                            zoomIn(marker.x, marker.y, markerWidth, markerHeight);
                            performZoomEffect();
                        }
                    } else {
                        showMarkerInfo(location);
                    }
                }
            }
        }

        spriteBatch.end();

        performZoomEffect();
    }

    private void zoomIn(float markerX, float markerY, float markerWidth, float markerHeight) {
        isZooming = true;
        zoomTimer = 0;

        targetX = markerX + markerWidth / 2f;
        targetY = markerY + markerHeight / 2f;
        targetZoom = 0.5f;

        isZoomedIn = true;
    }

    private void zoomOut() {
        isZooming = true;
        zoomTimer = 0;

        targetX = Constants.MAP_WIDTH / 2f;
        targetY = Constants.MAP_HEIGHT / 2f;
        targetZoom = 2.0f;

        isZoomedIn = false;
    }

    private void performZoomEffect() {
        if (isZooming) {
            zoomTimer += Gdx.graphics.getDeltaTime();

            // ease-out function for smoother animation
            float zoomDuration = 0.3f;
            float alpha = MathUtils.clamp(zoomTimer / zoomDuration, 0, 1);
            float easedAlpha = Interpolation.circleOut.apply(alpha);

            camera.position.x = MathUtils.lerp(camera.position.x, targetX, easedAlpha);
            camera.position.y = MathUtils.lerp(camera.position.y, targetY, easedAlpha);
            camera.zoom = MathUtils.lerp(camera.zoom, targetZoom, easedAlpha);

            if (zoomTimer >= zoomDuration) {
                isZooming = false;
            }
        }
    }


    private void showMarkerInfo(Location location) {
        final float fadeOutDuration = 0.5f;

        // Close the currently open info table, if any
        if (currentInfoTable != null) {
            currentInfoTable.addAction(Actions.sequence(
                    Actions.fadeOut(fadeOutDuration),
                    Actions.hide(),
                    Actions.removeActor()
            ));
            currentInfoTable = null; // having only one info table open
        }


        Pixmap bgPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        bgPixmap.setColor(0.5f, 0.5f, 0.5f, 0.9f);
        bgPixmap.fill();

        TextureRegionDrawable textureRegionDrawable = new TextureRegionDrawable(new Texture(bgPixmap));

        final Table infoTable = new Table(skin);
        infoTable.setFillParent(true);
        infoTable.center();
        infoTable.setBackground(textureRegionDrawable);

        Label.LabelStyle labelTitleStyle = new Label.LabelStyle(skin.get("title-plain", Label.LabelStyle.class));
        Label.LabelStyle labelSubtitleStyle = new Label.LabelStyle(skin.get("subtitle", Label.LabelStyle.class));
        Label.LabelStyle labelDescriptionEmailStyle = new Label.LabelStyle(skin.get("subtitle", Label.LabelStyle.class));

        Label descriptionLabel = new Label( location.getDescription(), labelDescriptionEmailStyle);
        descriptionLabel.setAlignment(Align.center);
        descriptionLabel.setFontScale(1.1f);

        Label locationLabel = new Label("Location: " + location.getStreet() + ", " + location.getCity(), labelSubtitleStyle);
        locationLabel.setAlignment(Align.center);
        locationLabel.setFontScale(1.1f);

        infoTable.add(new Label("Institution: " + location.getInstitution(), labelTitleStyle)).padBottom(10).row();
        infoTable.add(locationLabel).padBottom(10).row();
        infoTable.add(descriptionLabel).padBottom(10).row();
        infoTable.add(setImage(location)).padBottom(10).row();
        infoTable.add(new Label("Email: " + location.getEmail(), labelDescriptionEmailStyle)).padBottom(10).row();


        TextButton exitButton = new TextButton("Exit", skin, "round");
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                infoTable.addAction(Actions.sequence(
                        Actions.hide(),
                        Actions.removeActor()
                ));
                infoTable.remove();
                currentInfoTable = null;
                infoTavleVisible = false;
            }
        });
        infoTable.add(exitButton).padTop(20);

        infoTable.addAction(Actions.sequence(
                Actions.fadeIn(fadeOutDuration),
                Actions.visible(true),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        infoTavleVisible = true;
                    }
                })
        ));

        currentInfoTable = infoTable;
        hudStage.addActor(infoTable);
    }

    private Image setImage(Location location) {
        Image institutionImage = new Image();
        if (Objects.equals(location.getInstitution(), "Stuk")) {
            institutionImage = new Image(new TextureRegion(gameplayAtlas.findRegion(RegionNames.STUK)));
        } else if (Objects.equals(location.getInstitution(), "Lutkovno gledalisce")) {
            institutionImage = new Image(new TextureRegion(gameplayAtlas.findRegion(RegionNames.LUTKOVNO_GLEDALISCE)));
        } else if (Objects.equals(location.getInstitution(), "SNG")) {
            institutionImage = new Image(new TextureRegion(gameplayAtlas.findRegion(RegionNames.SNG)));
        } else if (Objects.equals(location.getInstitution(), "Oder Minoriti")) {
            institutionImage = new Image(new TextureRegion(gameplayAtlas.findRegion(RegionNames.MINORITI)));
        }  else if (Objects.equals(location.getInstitution(), "Narodni dom Maribor")) {
            institutionImage = new Image(new TextureRegion(gameplayAtlas.findRegion(RegionNames.NARODNI_DOM)));
        } else if (Objects.equals(location.getInstitution(), "Dvorana Tabor")) {
            institutionImage = new Image(new TextureRegion(gameplayAtlas.findRegion(RegionNames.DVORANA_TABOR)));
        } else if (Objects.equals(location.getInstitution(), "Dvorana Lent")) {
            institutionImage = new Image(new TextureRegion(gameplayAtlas.findRegion(RegionNames.LENT)));
        }
        return institutionImage;
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        hudStage.dispose();
        danceMusic.dispose();
        gameplayAtlas.dispose();

      //  MapRasterTiles.saveTileCache();
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        touchPosition.set(x, y, 0);
        camera.unproject(touchPosition);
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        camera.translate(-deltaX, deltaY);
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        if (initialDistance >= distance)
            camera.zoom += 0.02;
        else
            camera.zoom -= 0.02;
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    @Override
    public void pinchStop() {

    }

    private void handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            camera.zoom += 0.02;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            camera.zoom -= 0.02;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            camera.translate(-3, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            camera.translate(3, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            camera.translate(0, -3, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            camera.translate(0, 3, 0);
        }

        camera.zoom = MathUtils.clamp(camera.zoom, 0.5f, 2f);

        float effectiveViewportWidth = camera.viewportWidth * camera.zoom;
        float effectiveViewportHeight = camera.viewportHeight * camera.zoom;

        camera.position.x = MathUtils.clamp(camera.position.x, effectiveViewportWidth / 2f, Constants.MAP_WIDTH - effectiveViewportWidth / 2f);
        camera.position.y = MathUtils.clamp(camera.position.y, effectiveViewportHeight / 2f, Constants.MAP_HEIGHT - effectiveViewportHeight / 2f);
    }

    public void createDiscoBall(Geolocation location) {
        Image discoBall = new Image(new TextureRegion(gameplayAtlas.findRegion(RegionNames.DISCO_BALL)));
        discoBall.setWidth(75f);
        discoBall.setHeight(75f);

        Vector2 position = MapRasterTiles.getPixelPosition(location.lat, location.lng, beginTile.x, beginTile.y);
        float shiftAmount = 40f;
        discoBall.setPosition(position.x - shiftAmount, position.y);

        // Add a bouncing animation
        float bounceHeight = 10f;
        float duration = 1.0f;

        discoBall.addAction(Actions.sequence(
                Actions.moveBy(0, bounceHeight, duration / 2, Interpolation.linear),
                Actions.moveBy(0, -bounceHeight, duration / 2, Interpolation.linear),
                Actions.forever(
                        Actions.sequence(
                                Actions.moveBy(0, bounceHeight, duration / 2, Interpolation.linear),
                                Actions.moveBy(0, -bounceHeight, duration / 2, Interpolation.linear)
                        )
                )
        ));

        stage.addActor(discoBall);

    }

    public void createMasks(Geolocation location) {
        Image masks = new Image(new TextureRegion(gameplayAtlas.findRegion(RegionNames.MASKS)));
        masks.setWidth(70f);
        masks.setHeight(70f);

        Vector2 position = MapRasterTiles.getPixelPosition(location.lat, location.lng, beginTile.x, beginTile.y);
        float shiftAmount = 30f;
        masks.setPosition(position.x - shiftAmount, position.y);

        float targetRotation = 100f;
        float rotationDuration = 2.0f;

        Action rotationAction = Actions.forever(
                Actions.sequence(
                        Actions.rotateTo(targetRotation, rotationDuration),
                        Actions.rotateTo(0, rotationDuration)
                )
        );

        masks.addAction(rotationAction);

        stage.addActor(masks);

    }

    public void createMicrophones(Geolocation location) {
        Image microphones = new Image(new TextureRegion(gameplayAtlas.findRegion(RegionNames.MICROPHONE)));
        microphones.setWidth(50f);
        microphones.setHeight(50f);

        Vector2 position = MapRasterTiles.getPixelPosition(location.lat, location.lng, beginTile.x, beginTile.y);
        float shiftAmount = -100f;
        microphones.setPosition(position.x - shiftAmount, position.y);

        float targetScale = 1.5f;
        float scaleDuration = 0.8f;

        Action scaleAction = Actions.forever(
                Actions.sequence(
                        Actions.scaleTo(targetScale, targetScale, scaleDuration),
                        Actions.scaleTo(1.0f, 1.0f, scaleDuration)
                )
        );

        microphones.addAction(scaleAction);

        stage.addActor(microphones);

    }

    private void createEventAnimation() {
        for (Location location : locations) {
            if (Objects.equals(location.getInstitution(), "Stuk")) {
                createDiscoBall(location.getGeolocation());
            } else if (Objects.equals(location.getInstitution(), "SNG") || Objects.equals(location.getInstitution(), "Lutkovno gledalisce") || Objects.equals(location.getInstitution(), "Narodni dom Maribor") || Objects.equals(location.getInstitution(), "ODER MINORITI")) { /// add the otherqqqq
                createMasks(location.getGeolocation());
            } else if (Objects.equals(location.getInstitution(), "Festivalna dvorana Lent Maribor") || Objects.equals(location.getInstitution(), "Dvorana Tabor")) {
                createMicrophones(location.getGeolocation());
            }
        }
    }

    private void clearEventAnimation() {
        Array<Actor> discoBalls = stage.getActors();
        Array<Actor> discoBallsToRemove = new Array<>();
        Array<Actor> masks = stage.getActors();
        Array<Actor> masksToRemove = new Array<>();
        Array<Actor> microphones = stage.getActors();
        Array<Actor> microphonesToRemove = new Array<>();

        for (Actor discoBall : discoBalls) {
            discoBallsToRemove.add(discoBall);
        }
        for (Actor mask : masks) {
            masksToRemove.add(mask);
        }
        for (Actor microphone : microphones) {
            microphonesToRemove.add(microphone);
        }

        // Remove all disco balls collected in the separate list added because of the unexpected behaviour
        for (Actor discoBall : discoBallsToRemove) {
            discoBall.remove();
        }
        for (Actor mask : masksToRemove) {
            mask.remove();
        }
        for (Actor microphone : microphonesToRemove) {
            microphone.remove();
        }
    }


    private Actor createButtons() {
        Table table = new Table();
        table.defaults().pad(20);

        TextButton langButton = new TextButton("Lang", skin, "round");
        langButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(!infoTavleVisible){
                    showLangExample = !showLangExample;
                    ranOnce = false;
                }
            }
        });

        TextButton eventButton = new TextButton("Events", skin, "round");
        eventButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(!infoTavleVisible){
                    if (eventAnimationVisible) {
                        clearEventAnimation();
                    } else {
                        createEventAnimation();
                    }
                    eventAnimationVisible = !eventAnimationVisible;
                }
            }
        });

        TextButton danceButton = new TextButton("Dance", skin, "round");
        danceButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (eventAnimationVisible) {
                    clearDancingCharacters();
                    danceMusic.stop();
                } else {
                    initializeDancingCharacters();
                    danceMusic.play();
                    danceMusic.setLooping(true);
                }
                eventAnimationVisible = !eventAnimationVisible;
            }
        });

        TextButton quitButton = new TextButton("Quit", skin, "round");
        quitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(!infoTavleVisible){
                    Gdx.app.exit();
                }
            }
        });

        Table buttonTable = new Table();
        buttonTable.defaults().padLeft(30).padRight(30);

        buttonTable.add(langButton).padBottom(15).expandX().fill().row();
        buttonTable.add(eventButton).padBottom(15).expandX().fill().row();
        buttonTable.add(danceButton).padBottom(15).expandX().fill().row();
        buttonTable.add(quitButton).fillX();


        table.add(buttonTable);
        table.left().bottom();
        table.setFillParent(true);
        table.pack();

        return table;
    }
}

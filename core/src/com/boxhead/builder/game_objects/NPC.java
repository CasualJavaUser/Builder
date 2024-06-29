package com.boxhead.builder.game_objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.boxhead.builder.*;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Pathfinding;
import com.boxhead.builder.utils.Vector2i;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class NPC extends GameObject{

    public static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2);

    private static int nextId = 0;
    private final int id;

    protected transient TextureRegion currentTexture;
    protected transient Textures.NpcAnimation currentAnimation;
    protected boolean flipped = false;

    protected Vector2i prevPosition;
    protected final Vector2 spritePosition;
    protected int nextStep;
    protected float walkingSpeed = 1;

    protected Vector2i[] path = null;
    protected int pathStep;
    protected transient Future<?> pathfinding;

    private static final int STEP_INTERVAL = 50;

    private float stateTime = 0;

    public NPC(Textures.TextureId textureId, Vector2i gridPosition) {
        super(textureId, gridPosition);
        prevPosition = gridPosition;
        spritePosition = gridPosition.toVector2();
        currentTexture = getTexture();  //idle texture
        id = nextId;
        nextId++;
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (prevPosition.equals(gridPosition)) {
            stateTime = 0;
            currentTexture = getTexture();
        } else if (!Logic.isPaused()) {
            stateTime += 0.01f / (Logic.getTickSpeed() * 200);

            currentTexture = Textures.getAnimation(currentAnimation).getKeyFrame(stateTime, true);
            flipped = prevPosition.x > gridPosition.x;
        }

        batch.draw(
                currentTexture,
                spritePosition.x * World.TILE_SIZE,
                spritePosition.y * World.TILE_SIZE,
                (int) (currentTexture.getRegionWidth() / 2f),
                (int) (currentTexture.getRegionHeight() / 2f),
                currentTexture.getRegionWidth(),
                currentTexture.getRegionHeight(),
                flipped ? -1 : 1,
                1,
                0
        );
    }

    protected void navigateTo(Vector2i tile) {
        alignSprite();
        path = null;
        pathfinding = executor.submit(() -> {
            path = Pathfinding.findPath(gridPosition.clone(), tile);
            pathStep = 0;
            nextStep = STEP_INTERVAL;
        });
    }

    protected void navigateTo(BoxCollider collider) {
        alignSprite();
        path = null;
        pathfinding = executor.submit(() -> {
            path = Pathfinding.findPath(gridPosition.clone(), collider.cloneAndTranslate(Vector2i.zero()));
            pathStep = 0;
            nextStep = STEP_INTERVAL;
        });
    }

    /**
     * Follows along a path created with the {@code navigateTo()} method.
     *
     * @return <b>true</b> if destination is reached, <b>false</b> otherwise.
     */
    protected boolean followPath() {
        if (path == null || !pathfinding.isDone()) {
            return false;
        }

        float speedModifier = World.getTile(gridPosition).speed * walkingSpeed;
        if (nextStep >= STEP_INTERVAL / speedModifier) {
            if (pathStep == path.length - 1) {
                alignSprite();
                return true;
            }
            if (!World.isNavigable(path[pathStep + 1])) {
                navigateTo(path[path.length - 1]);
                return false;
            }
            pathStep++;
            prevPosition = gridPosition.clone();
            gridPosition.set(path[pathStep]);
            nextStep = 0;
        }
        nextStep++;
        spritePosition.add(((gridPosition.x - prevPosition.x) / (float) STEP_INTERVAL) * speedModifier,
                ((gridPosition.y - prevPosition.y) / (float) STEP_INTERVAL) * speedModifier);
        return false;
    }

    protected void alignSprite() {
        spritePosition.set(gridPosition.x, gridPosition.y);
        prevPosition.set(gridPosition);
    }

    public Vector2 getSpritePosition() {
        return spritePosition;
    }

    public boolean isMouseOver() {
        Vector2 mousePos = GameScreen.getMouseWorldPosition();
        return mousePos.x >= spritePosition.x * World.TILE_SIZE && mousePos.x < (spritePosition.x * World.TILE_SIZE + currentTexture.getRegionWidth()) &&
                mousePos.y >= spritePosition.y * World.TILE_SIZE && mousePos.y < (spritePosition.y * World.TILE_SIZE + currentTexture.getRegionHeight());
    }

    public abstract void wander();

    protected Vector2i randomPosInRange(int range) {
        double angle = BuilderGame.generalPurposeRandom().nextDouble() * 2 * Math.PI;
        double distance = BuilderGame.generalPurposeRandom().nextDouble() * range;
        return gridPosition.plus((int)(Math.cos(angle) * distance), (int)(Math.sin(angle) * distance));
    }

    public int getId() {
        return id;
    }
}

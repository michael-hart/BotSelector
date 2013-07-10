package net.bot.entities;

import net.bot.event.handler.EntityEventHandler;
import static net.bot.util.RandomUtil.rand;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.Color;
import org.lwjgl.util.vector.Vector2f;

public class EntityBot extends Entity {
	
	private static final int MAX_SPEED = 7;
	private static final int MIN_SPEED = 2;
	private static final float SPEED_MULTIPLIER = 1000F; // = 0.007 max, 0.002 min
	
	private static final int FRAMES_BEFORE_FOOD_DECREMENT = 60;
	private static final float FOOD_DECREMENT = 0.005F;
	
	private static final float OFFSPRING_SIZE = 0.3F;
	
	private static final float MAXIMUM_FORCE_DISTANCE = 0.1F;
	
	/*
	 * Acceleration/Curved paths
	 * How is this going to work?
	 * 
	 * Give a bot a destination position vector
	 * Bot attempts to get there with acceleration and velocity
	 * 
	 * 
	 */
//	private Vector2f mAcceleration, mRateOfAcc, intendedAcc;
	
	public EntityBot() {
		super();
		mColor = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
		mPosition = new Vector2f(rand.nextFloat(), rand.nextFloat());
		
		// Any neater way to do this?
		float xVel = (float) ((rand.nextInt(MAX_SPEED-MIN_SPEED) + MIN_SPEED)/SPEED_MULTIPLIER);
		float yVel = (float) ((rand.nextInt(MAX_SPEED-MIN_SPEED) + MIN_SPEED)/SPEED_MULTIPLIER);
		if (rand.nextInt(10) % 2 == 0) xVel *= -1;
		if (rand.nextInt(10) % 2 == 0) yVel *= -1;
		
		mFoodLevel = rand.nextFloat();
		mVelocity = new Vector2f(xVel, yVel);
		mSize = foodToSize(mFoodLevel);
		mResolvedForce = new Vector2f(0,0);
		
		// TODO implement acceleration
//		mAcceleration = new Vector2f();
//		mRateOfAcc = new Vector2f();
//		intendedAcc = new Vector2f();
		
	}
	
	public EntityBot(Color color, Vector2f position, Vector2f velocity, float foodLevel) {
		super();
		mColor = color;
		mPosition = position;
		mVelocity = velocity;
		mFoodLevel = foodLevel;
		mSize = foodToSize(foodLevel);
		mResolvedForce = new Vector2f(0,0);
	}

	@Override
	public void update() {
		// New position
		Vector2f.add(mPosition, mVelocity, mPosition);
		Vector2f.add(mVelocity, (Vector2f) mResolvedForce.scale((float) (1.0/mSize)), mVelocity);
		// Bounce off walls
		if (mPosition.x < 0 || mPosition.x > 1) {
			mVelocity.x *= -1;
		}
		if (mPosition.y < 0 || mPosition.y > 1) {
			mVelocity.y *= -1;
		}
		// Too old?
		if (++mFramesAlive % FRAMES_BEFORE_FOOD_DECREMENT == 0) {
			mFoodLevel -= FOOD_DECREMENT;
		}
		if (mFoodLevel < 0) {
			mState = State.STARVED;
		}
		// If food level high enough, 1 in 120 chance of spawning
		if (mFoodLevel >= 0.4 && rand.nextInt(120) == 0) {
			spawnClone(mColor, mPosition, mVelocity, mFoodLevel);
		}
		mSize = foodToSize(mFoodLevel);
		mResolvedForce = new Vector2f(0,0);
	}

	@Override
	public void draw() {
		glPushMatrix();
		
		double angle = 0;
		if (mVelocity.x == 0) {
			angle = mVelocity.y < 0 ? 180 : 0;
		} else if (mVelocity.x > 0) {
			angle = 90 - Math.toDegrees(Math.atan(mVelocity.y / mVelocity.x));
		} else {
			angle = -90 + Math.toDegrees(Math.atan(mVelocity.y / -mVelocity.x));
		}
		
		glTranslatef(mPosition.x, mPosition.y, 0);
		glRotated(angle, 0D, 0D, -1D);
		
		glBegin(GL_TRIANGLES);
		glColor3f(mColor.getRed()/256F, mColor.getGreen()/256F, mColor.getBlue()/256F);
		glVertex3f(0, mSize, 0);
		glVertex3f(mSize, -mSize, 0);
		glVertex3f(-mSize, -mSize, 0);
		glEnd();
		
		glPopMatrix();
		
	}
	
	public void consume(Entity food) {
		if (food.getState() != State.CONSUMED) {
			food.setState(State.CONSUMED); // Set food to eaten
			mFoodLevel += food.getFoodLevel(); // Eat food
			
			// Update momentum using masses, instead of assuming same mass
			mVelocity.scale(mSize);
			food.getVelocity().scale(food.getSize());
			Vector2f.add(mVelocity, food.getVelocity(), mVelocity);
			mVelocity.scale((float) (1.0/(mSize + food.getSize())));
			
		}
	}
	
	private float foodToSize(float food) {
		// y = mx + c
		return (float) (0.01 + food * 0.025);
	}
	
	private void spawnClone(Color color, Vector2f position, Vector2f velocity, float foodLevel) {
		// New bots should be of food level OFFSPRING_SIZE
		if (foodLevel <= OFFSPRING_SIZE) return;
		mFoodLevel -= OFFSPRING_SIZE;
		
		/*
		 * Algorithm:
		 * Generate offspring vector 
		 * 	0.5x to 0.8x original vector speed
		 * 	-90<theta<90 rotation of original vector
		 * Alter original velocity to conserve momentum
		 * v1 = (m1u1-m3v2)/(m1-m3)
		 */
		
		// Generate theta: -PI/2 < THETA < PI/2
		float theta = (float) (Math.PI * (rand.nextFloat() - 0.5));
		float x = (float) (velocity.x * Math.cos(theta) - velocity.y * Math.sin(theta));
		float y = (float) (velocity.x * Math.sin(theta) + velocity.y * Math.cos(theta));
		
		Vector2f newVelocity = new Vector2f(x, y);
		
		// 0.5x to 0.8x vector allowed
		float lowerBound = 0.8F, upperBound = 1.5F;
		float scale = rand.nextFloat() * (upperBound-lowerBound) + lowerBound;
		newVelocity.scale(scale);
		
		Vector2f momentum = new Vector2f(velocity.x, velocity.y);
		momentum.scale(foodToSize(foodLevel));
		
		newVelocity.scale(OFFSPRING_SIZE);
		Vector2f newMomentum = new Vector2f();
		Vector2f.sub(momentum, newVelocity, newMomentum);
		// Undo scale from before 
		newVelocity.scale(5F);
		
		newMomentum.scale((float) (1.0/(foodToSize(foodLevel)-OFFSPRING_SIZE)));
		mVelocity = newMomentum;
		
		Vector2f newPosition = new Vector2f(position.x, position.y);
		
		// Shift the little babby out of the way of big mummy by backtracking 60 frames of velocity
		newVelocity.scale(60F);
		Vector2f.sub(newPosition, newVelocity, newPosition);
		newVelocity.scale((float) (1/60.0));
		
		Color newColor = new Color(color.getRed(), color.getGreen(), color.getBlue());
		
		EntityBot offspring = new EntityBot(newColor, newPosition, newVelocity, OFFSPRING_SIZE);
		EntityEventHandler.botCreated(offspring);
	}
	
	/**
	 * Given an entity and a constant G, calculates and adds force acting upon the bot.
	 * Also negates the force if the other entity is larger.
	 * @param entity
	 */
	public void addForce(Entity entity) {
		// Check distance
		Vector2f distance = new Vector2f(entity.getPosition().x - mPosition.x, entity.getPosition().y - mPosition.y);
		if (distance.length() > MAXIMUM_FORCE_DISTANCE) return;
		double force = (G * (mSize * entity.getSize()))/distance.lengthSquared();
		double theta = Vector2f.angle(mPosition, entity.getPosition());
		Vector2f resolved = new Vector2f((float)(force * Math.sin(theta)), (float)(force * Math.cos(theta)));
		if (entity.getSize() > mSize) {
			resolved.negate();
		}
		Vector2f.add(mResolvedForce, resolved, mResolvedForce);
	}
	
}
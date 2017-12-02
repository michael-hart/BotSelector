package net.bot.entities;

import static net.bot.util.RandomUtil.rand;

import static org.lwjgl.opengl.GL11.*;

import net.bot.util.Colour;
import net.bot.util.Vector2f;

public class EntityFoodSpeck extends Entity {
	
	private static final int MIN_FOOD_SIZE = 2;
	private static final int MAX_FOOD_SIZE = 8;
	private static final float SIZE_MULTIPLIER = 1000;
	
//	private static final int MIN_FRAMES_ALIVE = 60000;
//	private static final int MAX_FRAMES_ALIVE = 600000;
	
//	private int mFramesBeforeDeath;
	
	public EntityFoodSpeck() {
		super();
		setSize((rand.nextInt(MAX_FOOD_SIZE - MIN_FOOD_SIZE) + MIN_FOOD_SIZE) / SIZE_MULTIPLIER);
//		mFramesBeforeDeath = rand.nextInt(MAX_FRAMES_ALIVE - MIN_FRAMES_ALIVE) + MIN_FRAMES_ALIVE;
		setPosition(new Vector2f(rand.nextFloat(), rand.nextFloat()));
		setVelocity(new Vector2f(0,0));
		setFoodLevel(getSize() * 10);
	}
	
	@Override
	public void update(double delta) {
		// No logic required
	}

	@Override
	public void draw() {
		
		glPushMatrix();
		
		Colour colour = getColour();
		Vector2f position = getPosition();
		glColor3f(colour.getRed()/256F, colour.getGreen()/256F, colour.getBlue()/256F);
		glTranslatef(position.x, position.y, 0);

		glBegin(GL_QUADS);
		float temp = getSize()/2;
		glVertex2f(temp, temp);
		glVertex2f(temp, -temp);
		glVertex2f(-temp, -temp);
		glVertex2f(-temp, temp);
		glEnd();
		
		glPopMatrix();
		
	}

	@Override
	public void consume(Entity entity) {
		// Do nothing
	}

}

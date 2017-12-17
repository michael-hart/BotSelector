package net.bot;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.bot.entities.AbstractEntityBot;
import net.bot.entities.Entity;
import net.bot.entities.Entity.State;
import net.bot.entities.EntityFoodSpeck;
import net.bot.event.handler.DisplayEventHandler;
import net.bot.event.listener.IDisplayEventListener;
import net.bot.food.FoodSource;
import net.bot.maths.Vector2f;

/**
 * The controller of the application, spinning up threads for required parts
 * and performing control logic calculations
 */
public class SimulationController implements Runnable {

    private static Logger log = LogManager.getLogger(SimulationController.class);
    
    private SimulationModel mModel;

    private List<EntityFoodSpeck> mFoodAdditions, mFoodDeletions;
    private List<AbstractEntityBot> mBotAdditions, mBotDeletions;
    private List<FoodSource> mSourceAdditions, mSourceDeletions;

    private long lastUpdateTime = System.currentTimeMillis();
    // Set iteration period to be 60ups, but in milliseconds
    private long iterationPeriod = (long) (1000/60.0);

    private boolean running = true;

    public SimulationController(SimulationModel model) {
        mModel = model;
        mFoodAdditions = new ArrayList<>();
        mFoodDeletions = new ArrayList<>();
        mBotAdditions = new ArrayList<>();
        mBotDeletions = new ArrayList<>();
        mSourceAdditions = new ArrayList<>();
        mSourceDeletions = new ArrayList<>();
    }

    @Override
    public void run() {
        log.info("Started running the controller thread successfully");
        while (running) {
            // Update everything
            double delta = getUpdateDelta();
            try {
                updateEntities(delta);
                updateFoodSources(delta);
            } catch (InterruptedException e1) {
                log.error(e1);
            }
            // Wait for the next iteration
            try {
                Thread.sleep(iterationPeriod);
            } catch (InterruptedException e) {
                log.error(e);
            }
        }
    }

    public void stop() {
        running = false;
    }

    public void updateEntities(double delta) throws InterruptedException {

        // TODO remove after disease testing
        /*
         * for (AbstractEntityBot bot: mBotEntityList) { if (!bot.isDiseased()) {
         * EntityDiseasedBot newBot = new EntityDiseasedBot(bot);
         * mBotsToAdd.add(newBot); mBotsToRemove.add(bot); } }
         */

        List<AbstractEntityBot> bots = mModel.getBots();
        List<EntityFoodSpeck> foodSpecks = mModel.getFood();

        // Check for age in food specks
        for (EntityFoodSpeck speck : foodSpecks) {
            speck.update(delta);
        }

        // Sort out collisions
        for (int i = 0; i < bots.size(); i++) {
            AbstractEntityBot bot = bots.get(i);
            bot.update(delta);

            for (int j = 0; j < bots.size(); j++) {
                if (j > i) {
                    collideOrConsume(bot, bots.get(j));
                }
                if (j != i) {
                    // Add forces for acceleration
                    bot.addForce(bots.get(j));
                }
            }
            for (EntityFoodSpeck speck : foodSpecks) {
                // Check for collision here
                collideOrConsume(bot, speck);
                bot.addForce(speck);
            }
        }

        for (AbstractEntityBot bot : bots) {
            if (bot.getState() != State.ALIVE) {
                mBotDeletions.add(bot);
            }
        }
        bots.removeAll(mBotDeletions);
        mBotDeletions.clear();
        bots.addAll(mBotAdditions);
        mBotAdditions.clear();

        for (EntityFoodSpeck food : foodSpecks) {
            if (food.getState() == State.CONSUMED) {
                mFoodDeletions.add(food);
//                mFoodAdditions.add(new EntityFoodSpeck());
            }
        }
        foodSpecks.removeAll(mFoodDeletions);
        mFoodDeletions.clear();
        foodSpecks.addAll(mFoodAdditions);
        mFoodAdditions.clear();

        mModel.releaseBots();
        mModel.releaseFood();
    }


    private void updateFoodSources(double delta) throws InterruptedException {
        List<FoodSource> sources = mModel.getSources();
        for (FoodSource source : sources) {
            source.update(delta);
        }
        sources.removeAll(mSourceDeletions);
        mSourceDeletions.clear();
        sources.addAll(mSourceAdditions);
        mSourceAdditions.clear();
        mModel.releaseSources();
    }


    private void collideOrConsume(AbstractEntityBot bot, Entity entity) {
        Vector2f compare = new Vector2f();
        Vector2f.sub(bot.getPosition(), entity.getPosition(), compare);
        if (compare.length() <= bot.getSize() + entity.getSize()) {
            // Collision!!
            if (bot.getSize() == entity.getSize()) {
                // Same size or same colour, so bounce off
                Vector2f newBot = new Vector2f();
                Vector2f newEntity = new Vector2f();
                float massA = bot.getSize(), massB = entity.getSize();

                Vector2f velA = new Vector2f(bot.getVelocity().x, bot.getVelocity().y);
                Vector2f velB = new Vector2f(entity.getVelocity().x, entity.getVelocity().y);

                velA.scale((float) ((massA - massB) / (massA + massB)));
                velB.scale((float) ((massB * 2) / (massA + massB)));
                Vector2f.add(velA, velB, newBot);

                velA = new Vector2f(bot.getVelocity().x, bot.getVelocity().y);
                velB = new Vector2f(entity.getVelocity().x, entity.getVelocity().y);

                velB.scale((float) ((massB - massA) / (massA + massB)));
                velA.scale((float) ((massA * 2) / (massA + massB)));
                Vector2f.add(velA, velB, newEntity);

                bot.setVelocity(newBot);
                entity.setVelocity(newEntity);
                if (entity instanceof AbstractEntityBot) {
                    bot.resolveContagiousDiseases((AbstractEntityBot) entity);
                }
            } else if (bot.getColour().equals(entity.getColour())) {
                if (entity instanceof AbstractEntityBot) {
                    bot.resolveContagiousDiseases((AbstractEntityBot) entity);
                }
                return;
            } else if (bot.getSize() < entity.getSize()) {
                entity.consume(bot);
            } else if (bot.getSize() > entity.getSize()) {
                bot.consume(entity);
            }
            // else if (bot.getSize() < entity.getSize()) {
            // entity.consume(bot);
            // } else if (bot.getColor().equals(entity.getColor())) {
            // // Do nothing :-)
            // return;
            // } else {
            // bot.consume(entity);
            // }
        }
    }

    private long getUpdateDelta() {
        long delta = System.currentTimeMillis() - lastUpdateTime;
        lastUpdateTime = System.currentTimeMillis();
        return delta;
    }

    public static void main(String[] args) throws InterruptedException {
        // Create a thread for the display
        SimulationDisplay display = new SimulationDisplay();
        Thread displayThread = new Thread(display);

        // Wait for thread to finish initialising
        InitCompleteListener listener = new InitCompleteListener();
        DisplayEventHandler.addListener(listener);
        displayThread.start();
        while (!listener.ready) {
            Thread.sleep(10);
        };
        DisplayEventHandler.removeListener(listener);

        // Create a thread for the controller
        SimulationController controller = new SimulationController(display.getModel());
        Thread controllerThread = new Thread(controller);

        // Start the controller
        controllerThread.start();

        // Wait for display to finish, then close and wait for controller
        while (displayThread.isAlive()) {
            Thread.sleep(10);
        }

        controller.stop();
        controllerThread.join();
    }

    public static class InitCompleteListener implements IDisplayEventListener {
        public boolean ready = false;
        public void onUpdate(double delta) {
        }
        public void onInitComplete() {
            ready = true;
        }
    }
}

package net.bot;

import static net.bot.util.SimRegisterConstants.FOOD_SPECKS;
import static net.bot.util.SimRegisterConstants.INITIAL_BOT_ENTITIES;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import net.bot.entities.AbstractEntityBot;
import net.bot.entities.EntityBot;
import net.bot.entities.EntityFoodSpeck;
import net.bot.event.handler.EntityEventHandler;
import net.bot.event.handler.FoodSourceEventHandler;
import net.bot.event.listener.IEntityEventListener;
import net.bot.event.listener.IFoodSourceEventListener;
import net.bot.food.FoodSource;
import net.bot.util.RandomUtil;

/**
 * Simple model containing the application data during runtime
 */
public class SimulationModel {

    private List<AbstractEntityBot> mBotEntityList;
    private List<EntityFoodSpeck> mFoodEntityList;
    private List<FoodSource> mFoodSourceList, mFoodSourceToAdd, mFoodSourceToRemove;

    private Semaphore mBotSem, mFoodSem, mSourceSem;

    public SimulationModel() {

        mBotSem = new Semaphore(1);
        mFoodSem = new Semaphore(1);
        mSourceSem = new Semaphore(1);

        mBotEntityList = new ArrayList<AbstractEntityBot>();
        for (int i = 0; i < INITIAL_BOT_ENTITIES; i++) {
            mBotEntityList.add(new EntityBot());
        }

        mFoodEntityList = new ArrayList<EntityFoodSpeck>();
        for (int i = 0; i < FOOD_SPECKS; i++) {
            mFoodEntityList.add(new EntityFoodSpeck());
        }

        EntityEventHandler.addListener(new IEntityEventListener() {
            @Override
            public void onFoodDestroyed(EntityFoodSpeck speck) {
                mFoodEntityList.remove(speck);
            }

            @Override
            public void onFoodCreated(EntityFoodSpeck speck) {
                mFoodEntityList.add(speck);
            }

            @Override
            public void onBotDestroyed(AbstractEntityBot bot) {
                mBotEntityList.remove(bot);
            }

            @Override
            public void onBotCreated(AbstractEntityBot bot) {
                mBotEntityList.add(bot);
            }
        });

        mFoodSourceList = new ArrayList<FoodSource>();
        for (int i = 0; i < 5; i++) {
            mFoodSourceList.add(new FoodSource(0.1f, 20, RandomUtil.rand.nextFloat() * 15f));
        }

        mFoodSourceToAdd = new ArrayList<FoodSource>();
        mFoodSourceToRemove = new ArrayList<FoodSource>();
        FoodSourceEventHandler.addListener(new IFoodSourceEventListener() {

            @Override
            public void onFoodSourceDestroyed(FoodSource source) {
                mFoodSourceToRemove.add(source);
            }

            @Override
            public void onFoodSourceCreated(FoodSource source) {
                mFoodSourceToAdd.add(source);
            }
        });
    }

    public List<AbstractEntityBot> getBots() throws InterruptedException {
        mBotSem.acquire();
        return mBotEntityList;
    }

    public void releaseBots() {
        mBotSem.release();
    }

    public List<EntityFoodSpeck> getFood() throws InterruptedException {
        mFoodSem.acquire();
        return mFoodEntityList;
    }

    public void releaseFood() {
        mFoodSem.release();
    }

    public List<FoodSource> getSources() throws InterruptedException {
        mSourceSem.acquire();
        return mFoodSourceList;
    }

    public void releaseSources() {
        mSourceSem.release();
    }

}

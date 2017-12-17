package net.bot;

import net.bot.event.handler.DisplayEventHandler;
import net.bot.event.listener.IDisplayEventListener;

public class LaunchSimulation {

    public static class InitCompleteListener implements IDisplayEventListener {
        public boolean ready = false;
        public void onUpdate(double delta) {
        }
        public void onInitComplete() {
            ready = true;
        }
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

}

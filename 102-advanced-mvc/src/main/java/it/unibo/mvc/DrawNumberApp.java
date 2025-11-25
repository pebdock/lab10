package it.unibo.mvc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {
    private static final String OUTPUT = "src" + File.separator +
    "main" + File.separator +
    "resources" + File.separator +
    File.separator + "output.txt";

    private static final String FILEPATH = "src" + File.separator +
    "main" + File.separator +
    "resources" + File.separator +
    File.separator + "config.yml";

    private static final int MIN = 0;
    private static final int MAX = 100;
    private static final int ATTEMPTS = 10;

    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * @param views
     *            the views to attach
     */
    public DrawNumberApp(final DrawNumberView... views) {
        /*
         * Side-effect proof
         */
        int tempMinimum = MIN;
        int tempMaximum = MAX;
        int tempAttempts = ATTEMPTS;
        try(final BufferedReader buffered = new BufferedReader(new FileReader(FILEPATH))) {
            String readedLine;
            while((readedLine = buffered.readLine()) != null) {
                final String[] savedSettings = readedLine.split(":");
                if(savedSettings.length == 2) {
                    final String setting = savedSettings[0].trim();
                    final int settingValue = Integer.parseInt(savedSettings[1].trim());
                    if("minimum".equals(setting)) {
                        tempMinimum = settingValue;
                    } else if("maximum".equals(setting)) {
                        tempMaximum = settingValue;
                    } else if("attempts".equals(setting)) {
                        tempAttempts = settingValue;
                    } else {
                        throw new IllegalStateException("The settings file is not valid");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }
        this.model = new DrawNumberImpl(tempMinimum, tempMaximum, tempAttempts);
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * @param args
     *            ignored
     * @throws FileNotFoundException 
     */
    public static void main(final String... args) throws FileNotFoundException {
        new DrawNumberApp(
            new DrawNumberViewImpl(),
            new DrawNumberViewImpl(),
            new PrintStreamView(System.out),
            new PrintStreamView(OUTPUT)
        );
    }

}

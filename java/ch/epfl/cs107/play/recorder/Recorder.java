package ch.epfl.cs107.play.recorder;

import ch.epfl.cs107.play.math.RandomGenerator;
import ch.epfl.cs107.play.math.Vector;
import ch.epfl.cs107.play.recorder.recordEntry.*;
import ch.epfl.cs107.play.window.Button;
import ch.epfl.cs107.play.window.Keyboard;
import ch.epfl.cs107.play.window.Mouse;
import ch.epfl.cs107.play.window.Window;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class Recorder {
    public static String RECORD_DIRECTORY = "records";
    private static final int KEYBOARD_MAX_KEYCODE = KeyEvent.KEY_LAST;
    private static final int MOUSE_BUTTON_MAX_KEYCODE = 2;
    private final Keyboard keyboard;
    private final Mouse mouse;
    private long startTime;
    private Record record;
    private Vector lastMousePosition;

    public Recorder(Window window) {
        this.keyboard = window.getKeyboard();
        this.mouse = window.getMouse();
    }

    public void start() {
        startTime = System.currentTimeMillis();
        record = new Record();
        long randomSeed = RandomGenerator.getInstance().nextLong();
        RandomGenerator.getInstance().setSeed(randomSeed);
        record.setRandomSeed(randomSeed);
    }

    public void stop(String filename) throws IllegalArgumentException {
        if (filename == null) throw new IllegalArgumentException();
        try {
            File directory = new File(RECORD_DIRECTORY);
            File file = new File(directory, filename);
            file.getParentFile().mkdirs();
            file.createNewFile();
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
            out.writeObject(record);
            out.close();
        } catch (Exception e) {
            System.out.println("ERROR: An error happened while saving record");
            e.printStackTrace();
        }
    }

    public void update() {
        if (record == null) return;

        long time = System.currentTimeMillis() - startTime;

        for (int key = 0; key <= KEYBOARD_MAX_KEYCODE; ++key) {
            Button button = keyboard.get(key);

            if (button.isPressed())
                record.addEntry(new KeyboardPressedRecordEntry(time, key));
            if (button.isReleased())
                record.addEntry(new KeyboardReleasedRecordEntry(time, key));
        }
        for (int key = 0; key <= MOUSE_BUTTON_MAX_KEYCODE; ++key) {
            Button button = mouse.getButton(key);
            if (button.isPressed())
                record.addEntry(new MouseButtonPressedRecordEntry(time, key));
            if (button.isReleased())
                record.addEntry(new MouseButtonReleasedRecordEntry(time, key));
        }
        final Vector mousePosition = mouse.getPosition();
        if (!mousePosition.equals(lastMousePosition)) {
            lastMousePosition = mousePosition;
            record.addEntry(new MouseMoveRecordEntry(time, mousePosition.x, mousePosition.y));
        }
    }
}

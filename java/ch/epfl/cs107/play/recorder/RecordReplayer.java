package ch.epfl.cs107.play.recorder;

import ch.epfl.cs107.play.math.RandomGenerator;
import ch.epfl.cs107.play.recorder.recordEntry.RecordEntry;
import ch.epfl.cs107.play.window.Window;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

public class RecordReplayer {
    private final Window window;
    private Record record;
    private Robot robot;
    private long startTime;
    private int currentRecordEntryIndex;

    public RecordReplayer(Window window) {
        this.window = window;
        try {
            robot = new Robot();
        } catch (AWTException e) {
            System.out.println("ERROR: An error happened while creating an input emulator");
            e.printStackTrace();
        }
    }

    public void start(String filename) {
        try {
            File directory = new File(Recorder.RECORD_DIRECTORY);
            File file = new File(directory, filename);
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
            record = (Record) in.readObject();
            in.close();
        } catch (Exception e) {
            System.out.println("ERROR: An error happened while loading record");
            e.printStackTrace();
        }
        startTime = System.currentTimeMillis();
        currentRecordEntryIndex = 0;
        RandomGenerator.getInstance().setSeed(record.getRandomSeed());
    }

    public void update() {
        if (record == null) return;
        long time = System.currentTimeMillis() - startTime;

        RecordEntry currentEntry = record.getEntry(currentRecordEntryIndex);
        while (currentEntry != null && currentEntry.getTime() < time) {
            currentEntry.replay(robot, window);

            ++currentRecordEntryIndex;
            currentEntry = record.getEntry(currentRecordEntryIndex);
        }
    }
}

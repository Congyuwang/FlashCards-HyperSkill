package flashcards;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Stack;

public class Logger {
    Stack<String> logs = new Stack<>();

    public void log(String log) {
        logs.add(log);
    }

    public void save(String path) throws IOException {
        File file = new File(path);
        FileWriter fileWriter = new FileWriter(file);
        for (String s : logs) {
            fileWriter.write(s);
        }
        fileWriter.close();
    }
}

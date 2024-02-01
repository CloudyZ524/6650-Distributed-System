package ContextSwitching;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class WriterThread extends Thread {
  int LINES = 1000;

  @Override
  public void run() {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter("test_immediate.txt", true))) {
      for (int i = 0; i < LINES; i++) {
        String data = System.currentTimeMillis() + ", " + this.getId() + ", " + i + "\n";
          writer.write(data);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}

package ContextSwitching;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Writer2Thread extends Thread {
  int LINES = 1000;


  @Override
  public void run() {
    StringBuilder stringBuilder = new StringBuilder();
    for (int i = 0; i < LINES; i++) {
      String data = System.currentTimeMillis() + ", " + this.getId() + ", " + i + "\n";
      stringBuilder.append(data);
    }

    try (BufferedWriter writer = new BufferedWriter(new FileWriter("test_2.txt", true))) {
      writer.write(stringBuilder.toString());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}

package ContextSwitching;

import java.util.ArrayList;
import java.util.List;

public class Writer3Thread extends Thread {
  int LINES = 1000;
  private List<String> localData = new ArrayList<>();


  @Override
  public void run() {
    for (int i = 0; i < LINES; i++) {
      String data = System.currentTimeMillis() + ", " + this.getId() + ", " + i + "\n";
      localData.add(data);
    }
  }

  public List<String> getLocalData() {
    return localData;
  }
}


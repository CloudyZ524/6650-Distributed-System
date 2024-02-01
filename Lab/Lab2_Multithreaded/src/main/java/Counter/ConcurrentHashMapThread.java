package Counter;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashMapThread implements Runnable {
  private ConcurrentHashMap<Integer, Integer> concurrentHashMap;
  private Integer numElements;

  public ConcurrentHashMapThread(Integer numElements) {
    this.concurrentHashMap = new ConcurrentHashMap<>();
    this.numElements = numElements;
  }

  @Override
  public void run() {
    long startTime = System.currentTimeMillis();

    for (int i = 0; i < numElements; i++) {
      concurrentHashMap.put(i, i);
    }
    long endTime = System.currentTimeMillis();
    System.out.println("Time taken to add to ConcurrentHashMap: " + (endTime - startTime) + " ms");
  }
}

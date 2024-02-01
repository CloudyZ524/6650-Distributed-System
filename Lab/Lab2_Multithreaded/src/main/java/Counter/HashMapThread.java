package Counter;

import java.util.HashMap;

public class HashMapThread implements Runnable {
  private HashMap<Integer, Integer> hashmap;
  private Integer numElements;

  public HashMapThread(Integer numElements) {
    this.hashmap = new HashMap<>();
    this.numElements = numElements;
  }

  @Override
  public void run() {
    long startTime = System.currentTimeMillis();

    for (int i = 0; i < numElements; i++) {
      hashmap.put(i, i);
    }
    long endTime = System.currentTimeMillis();
    System.out.println("Time taken to add to HashMap: " + (endTime - startTime) + " ms");
  }
}

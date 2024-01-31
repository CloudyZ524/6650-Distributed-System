package Counter;

import java.util.Hashtable;

public class HashTableThread implements Runnable{
  private Hashtable<Integer, Integer> hashtable;
  private Integer numElements;

  public HashTableThread(Integer numElements) {
    this.hashtable = new Hashtable<>();
    this.numElements = numElements;
  }

  @Override
  public void run() {
    long startTime = System.currentTimeMillis();

    for (int i = 0; i < numElements; i++) {
      hashtable.put(i, i);
    }
    long endTime = System.currentTimeMillis();
    System.out.println("Time taken to add to Counter.HashTable: " + (endTime - startTime) + " ms");
  }
}

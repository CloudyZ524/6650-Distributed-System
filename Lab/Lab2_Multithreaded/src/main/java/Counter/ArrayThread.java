package Counter;

import java.util.ArrayList;

public class ArrayThread implements Runnable{
  private ArrayList array;
  private Integer numElements;

  public ArrayThread(Integer numElements) {
    this.array = new ArrayList<>();
    this.numElements = numElements;
  }

  @Override
  public void run() {
    long startTime = System.currentTimeMillis();

    for (int i = 0; i < numElements; i++) {
      array.add(i);
    }
    long endTime = System.currentTimeMillis();
    System.out.println("Time taken to add to ArrayList: " + (endTime - startTime) + " ms");
  }
}

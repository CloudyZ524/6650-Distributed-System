package ContextSwitching;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WriterToFile {
  public static void main(String[] args) throws InterruptedException {
    WriterToFile writerToFile = new WriterToFile();

    writerToFile.Approach1();

    writerToFile.Approach2();

    writerToFile.Approach3();

  }

  public void Approach1() throws InterruptedException {
    long startTime = System.currentTimeMillis();
    Thread[] threads = new Thread[500];

    for (int i = 0; i < threads.length; i++) {
      threads[i] = new WriterThread();
      threads[i].start();
    }

    for (Thread thread : threads) {
      thread.join();
    }

    long endTime = System.currentTimeMillis();
    System.out.println("Time taken (approach 1): " + (endTime - startTime) + " ms");
  }

  public void Approach2() throws InterruptedException {
    long startTime = System.currentTimeMillis();
    Thread[] threads = new Thread[500];

    for (int i = 0; i < threads.length; i++) {
      threads[i] = new Writer2Thread();
      threads[i].start();
    }

    for (Thread thread : threads) {
      thread.join();
    }

    long endTime = System.currentTimeMillis();
    System.out.println("Time taken (approach 2): " + (endTime - startTime) + " ms");
  }

  public void Approach3() throws InterruptedException {
    StringBuilder fileBuilder = new StringBuilder();
    List<Writer3Thread> threadList = new ArrayList<>();
    long startTime = System.currentTimeMillis();
    Thread[] threads = new Thread[500];


    for (int i = 0; i < threads.length; i++) {
      Writer3Thread thread = new Writer3Thread();
      threads[i] = thread;
      threadList.add(thread);
      threads[i].start();
    }

    for (Thread thread : threads) {
      thread.join();
    }

    for (Writer3Thread thread : threadList) {
      for (String data : thread.getLocalData()) {
        fileBuilder.append(data);
      }
    }

    try (BufferedWriter writer = new BufferedWriter(new FileWriter("test_3.txt", true))) {
      writer.write(fileBuilder.toString());
    } catch (IOException e) {
      e.printStackTrace();
    }

    long endTime = System.currentTimeMillis();
    System.out.println("Time taken (approach 3): " + (endTime - startTime) + " ms");
  }

}

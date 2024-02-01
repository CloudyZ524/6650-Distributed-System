public class MultiThreadClient {
  final static private int NUMTHREADS = 100;

  public static void main(String[] args) throws InterruptedException {

    long startTime = System.currentTimeMillis();

    Thread[] threads = new Thread[NUMTHREADS];

    for (int i = 0; i < NUMTHREADS; i++) {
      threads[i] = new Thread(new Client());
      threads[i].start();
      };

    // Wait for all threads to complete
    for (Thread thread: threads) {
      thread.join();
    }

    long endTime = System.currentTimeMillis();
    System.out.println("Time taken for multithreaded clients: " + (endTime - startTime) + " ms");
  }
}

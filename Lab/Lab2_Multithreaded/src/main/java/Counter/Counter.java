package Counter;

public class Counter {
  // Shared counter
  private static int counter = 0;

  public static synchronized void incrementCounter() {
    counter++;
  }

  // Synchronized accessor for the counter
  public static synchronized int getCounter() {
    return counter;
  }

  public static void main(String[] args) throws InterruptedException {
    int numThreads = 1000;

    if (args.length > 0) {
      try {
        numThreads = Integer.parseInt(args[0]);
      } catch (NumberFormatException e) {
        System.err.println("Argument" + args[0] + " must be an integer.");
      }
    }

    // Take a timestamp before starting the threads
    long startTime = System.currentTimeMillis();

    // Array to hold all the threads
    Thread[] threads = new Thread[numThreads];

    // Initialize and start the threads
    for (int i = 0; i < numThreads; i++) {
      threads[i] = new Thread(new CounterThread());
      threads[i].start();
    }

    // Wait for all threads to finish
    for (int i = 0; i < numThreads; i++) {
      threads[i].join();
    }

    // Take a timestamp after all threads have completed
    long endTime = System.currentTimeMillis();

    System.out.println("Counter.Counter value: " + getCounter());
    System.out.println("Duration (ms): " + (endTime - startTime));
  }
}
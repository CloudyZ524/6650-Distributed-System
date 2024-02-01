package Counter;

public class CollectionsThread {
  public static void main(String[] args) throws InterruptedException {
    int numElements = 100000;

    Thread arrayThread = new Thread(new ArrayThread(numElements));
    arrayThread.start();

    Thread vectorThread = new Thread(new VectorThread(numElements));
    vectorThread.start();

    Thread hashtableThread = new Thread(new HashTableThread(numElements));
    hashtableThread.start();

    Thread hashmapThread = new Thread(new HashMapThread(numElements));
    hashmapThread.start();

    Thread concurrentHashmapThread = new Thread(new ConcurrentHashMapThread(numElements));
    concurrentHashmapThread.start();

  }
}

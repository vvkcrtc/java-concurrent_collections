import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;


public class Main {
    static final int COUNT_TEXTS = 10_000;
    static final int TEXT_LENGTH = 100_000;
    static String letters = "abc";
    static int countParsedThreads = letters.length();

    static int countCharsOfStr(String str, char symbol) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == symbol) {
                count++;
            }
        }
        return count;
    }

    static class CountChars implements Callable<Integer> {
        int index;

        public CountChars(int index) {
            this.index = index;
        }

        public Integer call() throws Exception {
            int count = 0;
            for (int i = 0; i < COUNT_TEXTS; i++) {
                try {
                    String tmpStr = texts[index].take();
                    count += countCharsOfStr(tmpStr, letters.charAt(index));
                    //  Thread.sleep(150);
                } catch (InterruptedException e) {
                }
            }
            return count;
        }

    }

    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }

    static BlockingQueue<String>[] texts = new BlockingQueue[countParsedThreads];

    public static void main(String[] args) throws ExecutionException, InterruptedException {


        for (int i = 0; i < countParsedThreads; i++) {
            texts[i] = new ArrayBlockingQueue<>(100);
        }

        new Thread(() -> {
            for (int i = 0; i < COUNT_TEXTS; i++) {
                try {
                    String tmpText = generateText(letters, TEXT_LENGTH);
                    for (int j = 0; j < countParsedThreads; j++) {
                        texts[j].put(tmpText);
                    }
                    // Thread.sleep(100);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }).start();


        List<Future> threads = new ArrayList<>();
        ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        for (int i = 0; i < countParsedThreads; i++) {
            Callable callable = new CountChars(i);
            threads.add(threadPool.submit(callable));
        }

        for (int t = 0; t < threads.size(); t++) {
            Future task = threads.get(t);
            Integer count = (Integer) task.get();
            System.out.println("Поток " + (t + 1) + " символов " + letters.charAt(t) + " = " + count);
        }

        threadPool.shutdown();


    }
}

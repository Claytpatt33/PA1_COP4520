import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class PrimeFinder {
    private static final int MAX = 100_000_000;
    private static final boolean[] isPrime = new boolean[MAX + 1];
    private static final int THREADS = 8;

    public static void main(String[] args) throws Exception {
        Arrays.fill(isPrime, true);
        isPrime[0] = isPrime[1] = false;
        long startTime = System.currentTimeMillis();
        
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        int segmentSize = MAX / THREADS;
        
        for (int i = 0; i < THREADS; i++) {
            int start = i * segmentSize;
            int end = (i + 1) * segmentSize;
            if (i == THREADS - 1) {
                end = MAX;
            }
            executor.execute(new SieveSegment(start, end));
        }
        
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);
        
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        List<Integer> primeList = new ArrayList<>();
        for (int i = 2; i <= MAX; i++) {
            if (isPrime[i]) {
                primeList.add(i);
            }
        }
        
        PrintWriter writer = new PrintWriter("primes.txt", "UTF-8");
        writer.println(executionTime + " " + primeList.size() + " " + primeList.stream().mapToLong(Integer::longValue).sum());
        
        primeList.stream().sorted(Collections.reverseOrder()).limit(10).sorted().forEach(writer::println);
        writer.close();
    }

    static class SieveSegment implements Runnable {
        private final int start, end;

        SieveSegment(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            int sqrtLimit = (int) Math.sqrt(MAX);
            for (int i = 2; i <= sqrtLimit; i++) {
                if (isPrime[i]) {
                    int startMultiple = (start + i - 1) / i * i;
                    for (int j = Math.max(startMultiple, i * i); j <= end; j += i) {
                        isPrime[j] = false;
                    }
                }
            }
        }
    }
}
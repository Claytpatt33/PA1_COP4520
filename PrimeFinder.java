import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;

public class PrimeFinder {
    private static final int MAX = 100_000_000;
    private static final boolean[] isPrime = new boolean[MAX + 1];
    private static final int THREADS = 8;
    private static final PriorityQueue<Integer> topTenPrimes = new PriorityQueue<>();

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

        long sumOfPrimes = 0;
        int totalPrimesFound = 0;
        for (int i = 2; i <= MAX; i++) {
            if (isPrime[i]) {
                sumOfPrimes += i;
                totalPrimesFound++;
                topTenPrimes.add(i);
                if (topTenPrimes.size() > 10) {
                    topTenPrimes.poll();
                }
            }
        }
        
        PrintWriter writer = new PrintWriter("primes.txt", "UTF-8");
        writer.println("execution time: " + executionTime + "ms");
        writer.println("total number of primes found: " + totalPrimesFound);
        writer.println("sum of all primes found: " + sumOfPrimes);
        writer.print("top ten maximum primes, from lowest to highest: ");
        
        int[] topPrimes = new int[topTenPrimes.size()];
        for (int i = topPrimes.length - 1; i >= 0; i--) {
            topPrimes[i] = topTenPrimes.poll();
        }
        
        for (int i = 0; i < topPrimes.length; i++) {
            writer.print(topPrimes[i]);
            if (i < topPrimes.length - 1) {
                writer.print(", ");
            }
        }
        writer.println();
        writer.close();
    }

    static class SieveSegment implements Runnable {
    private final int start, end;
    private static final int sqrtLimit = (int) Math.sqrt(MAX);
    private static final ArrayList<Integer> smallPrimes = new ArrayList<>();

    static {
        // Precompute the small primes
        boolean[] isSmallPrime = new boolean[sqrtLimit + 1];
        Arrays.fill(isSmallPrime, true);
        for (int i = 2; i * i <= sqrtLimit; i++) {
            if (isSmallPrime[i]) {
                smallPrimes.add(i);
                for (int j = i * i; j <= sqrtLimit; j += i) {
                    isSmallPrime[j] = false;
                }
            }
        }
    }

    SieveSegment(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public void run() {
        Arrays.fill(isPrime, start, end, true);
        for (int p : smallPrimes) {
            int firstMultiple = Math.max(p * p, (start + p - 1) / p * p);
            for (int j = firstMultiple; j <= end; j += p) {
                isPrime[j] = false;
            }
        }
    }
}
}
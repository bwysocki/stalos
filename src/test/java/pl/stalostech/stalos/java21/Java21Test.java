package pl.stalostech.stalos.java21;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.SequencedCollection;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

// tests java 21 features that are NOT in preview
public class Java21Test {

    private static final int RECORD_COUNT = 50_000;
    //private static final ScopedValue<String> requestUUID = ScopedValue.newInstance();

    @Test
    public void test_patternMatchingForSwitch() {
        assertThat(patternMatchingForSwitch(5)).isEqualTo("This is 5");
    }

    @Test
    public void test_sequencedCollections() {
        assertThat(sequencedCollection()).hasSameElementsAs(List.of(1, 2, 3));
    }

    @Test
    public void test_record() {
        Person mark = new Person("Mark", "Boss", 12);
        assertThat(deconstructRecord(mark)).isEqualTo("Mark");
        assertThat(patternMatchingForRecord(mark)).isEqualTo(12);
    }

    @Test
    public void test_gc() {
        // stack -> local primitives / references to objects / each thread own stack / NO GC (LIFO) - zmienne usuwane gdy metoda konczy dzialanie
        // heap -> objects / statics / tables / GC works here
        // GC - removes from heap not references objects (referencje ze stack/CPU registry)
        assertThat(gc()).isEqualTo("g1 young generation");
    }

    @Test
    void test_virtualThreads_CpuIntensiveOperation() {
        long standardThreadTime = measureExecutionTime(false, this::cpuIntensiveTask);
        long virtualThreadTime = measureExecutionTime(true, this::cpuIntensiveTask);

        System.out.printf("CPU Intensive - Standard Threads: %d ms, Virtual Threads: %d ms%n",
                standardThreadTime, virtualThreadTime);
    }

    @Test
    void test_virtualThreads_IoIntensiveOperation() throws IOException {
        Path tempDir = Files.createTempDirectory("ioTest");
        long standardThreadTime = measureExecutionTime(false, () -> ioTask(tempDir));
        long virtualThreadTime = measureExecutionTime(true, () -> ioTask(tempDir));

        System.out.printf("I/O Intensive - Standard Threads: %d ms, Virtual Threads: %d ms%n",
                standardThreadTime, virtualThreadTime);

    }

    /*@Test
    void test_scopeValues() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);
        Runnable handleRequest = () -> {
            String uuid = UUID.randomUUID().toString();
            ScopedValue.where(requestUUID, uuid).run(() -> {
                processRequest();
                latch.countDown();
            });
        };
        Thread.startVirtualThread(handleRequest);
        Thread.startVirtualThread(handleRequest);
        Thread.startVirtualThread(handleRequest);

        latch.await();
    }*/

    /*
        Serial GC : -XX:+UseSerialGC (dobry dla aplikacji jednowatkowych - długie zatrzymania aplikacji podczas zbierania pamięci)
        Parallel GC : -XX:+UseParallelGC (wykorzystuje wiele wątków do zarządzania pamięcią - moze miec dluzsze STOP THE WORLD)
        G1 GC : (domyslny od java 9) - podzielona pamięć na regiony (Eden, Survivor, Old), zorientowany na równoważenie wydajności i czasu opóźnień.
        ZGC -XX:+UseZGC: - Dzieli pamięć na 'memory pages' - 2MB, 32MB, 1GB - nie ma regionow(sa od 21 - young / old), a kazdy obiekt na `colored pointer` - sledzi cykl zycia obiektu.
        ZGC - most objects die young (jesli przezyje idzie do old)
     */
    private String gc() {
        String gcName = ManagementFactory.getGarbageCollectorMXBeans()
                .stream()
                .map(gc -> gc.getName().toLowerCase())
                .findFirst()
                .orElse("");

        return gcName;
    }

    private int patternMatchingForRecord(Object o) {
        return switch (o) {
            case Person(String fName, String lName, int age) -> age;
            default -> 0;
        };
    }

    private Object deconstructRecord(Object o) {
        if (o instanceof Person(String fname, String lname, int age)) {
            return fname;
        }
        return "";
    }

    private SequencedCollection<Integer> sequencedCollection() {
        SequencedCollection<Integer> list = new LinkedList<>();
        list.add(2);
        list.addFirst(3);
        list.addLast(1);
        return list.reversed();
    }

    private String patternMatchingForSwitch(Object o) {
        return switch (o) {
            case String s -> s.toString();
            case Integer i -> "This is %s".formatted(i);
            default -> "unknown";
        };
    }

    record Person(String fname, String lname, int age) {
    }

    private long measureExecutionTime(boolean useVirtualThreads, Runnable task) {
        ThreadFactory threadFactory = useVirtualThreads
                ? Thread.ofVirtual().factory()
                : Thread.ofPlatform().factory();

        var executor = Executors.newThreadPerTaskExecutor(threadFactory);

        long startTime = System.currentTimeMillis();

        IntStream.range(0, RECORD_COUNT).forEach(i -> executor.submit(task));

        executor.shutdown();
        while (!executor.isTerminated()) {
            Thread.yield(); // Ensure we wait for all tasks to complete
        }

        return System.currentTimeMillis() - startTime;
    }

    private void cpuIntensiveTask() {
        long result = 0;
        for (int i = 0; i < 1_000_000; i++) {
            result += Math.sqrt(i);
        }
    }

    private void ioTask(Path tempDir) {
        try {
            Path tempFile = Files.createTempFile(tempDir, "test", ".txt");
            Files.writeString(tempFile, "This is a test for virtual threads vs standard threads.");
            Files.readString(tempFile);
            Files.delete(tempFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*private static void processRequest() {
        System.out.println("Processing request with UUID: " + requestUUID.get());
        performSubtask();
    }

    // Przykładowa metoda wywoływana w ramach tego samego żądania
    private static void performSubtask() {
        System.out.println("Subtask for UUID: " + requestUUID.get());
        final String parentUUID = requestUUID.get();
        new Thread(() -> {
            // System.out.println("Kid Thread UUID: " + requestUUID.get()); // null pointer!
            ScopedValue.runWhere(requestUUID, parentUUID, () -> {
                System.out.println("Kid Thread UUID: " + requestUUID.get());
            });
        }).start();
    }*/

}

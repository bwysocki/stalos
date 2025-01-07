package pl.stalostech.stalos.jmh;

import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime) // Mierzenie średniego czasu wykonania
@OutputTimeUnit(TimeUnit.MILLISECONDS) // Wyniki w milisekundach
@State(Scope.Thread) // Stan współdzielony w obrębie wątku
public class JmhTest {

    /*
    Benchmark                       Mode  Cnt  Score   Error  Units
    JmhTest.sumUsingForLoop         avgt    5  1.177 ± 0.253  ms/op
    JmhTest.sumUsingParallelStream  avgt    5  0.756 ± 0.650  ms/op
    JmhTest.sumUsingStream          avgt    5  1.292 ± 0.311  ms/op
     */

    private List<Integer> numbers;

    @Test
    public void runJmhBenchmarks() throws Exception {
        org.openjdk.jmh.Main.main(new String[]{
                "JmhTest", // Nazwa klasy benchmarku
                "-wi", "3",    // Liczba iteracji rozgrzewki
                "-i", "5",     // Liczba iteracji rzeczywistego pomiaru
                "-f", "1"      // Liczba forków
        });
    }

    @Setup(Level.Iteration) // Przygotowanie danych przed każdą iteracją benchmarku
    public void setup() {
        numbers = new ArrayList<>();
        for (int i = 0; i < 1_000_000; i++) {
            numbers.add(i);
        }
    }

    @Benchmark
    public int sumUsingForLoop() {
        int sum = 0;
        for (int num : numbers) {
            sum += num;
        }
        return sum;
    }

    @Benchmark
    public int sumUsingStream() {
        return numbers.stream().mapToInt(Integer::intValue).sum();
    }

    @Benchmark
    public int sumUsingParallelStream() {
        return numbers.parallelStream().mapToInt(Integer::intValue).sum();
    }

}

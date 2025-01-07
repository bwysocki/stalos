package pl.stalostech.stalos.jvm;

import org.junit.jupiter.api.Test;

/*
    x.java/y.groovy/z.scala -[javac]-> x.class(bytecode) -> [JVM]
    JVM - can read line by line - but its slow -> to fix it: JIT was developed
    JIT -> checks what code / loop / method is executed most frequently and compiles it to native machine code
    JIT -> kod dziala szybciej im dluzej dziala apka :)
    JIT -> domyslne C1(1-3) i C2(4) kompilatory (zastepowane przez GRAL JIT) robia 4 levele kompilacji

 */
public class JITCompilerTest {

    @Test
    void testPrimeNrs() {
        // -XX:+PrintCompilation
        // -XX:+UnlockDiagnosticVMOptions / -XX:+LogCompilation
        // s - synchronized, n - native, % - native running in code cash (most optimal way
        for (int i = 0; i < 50000; i++) {
            showNext(i);
        }
    }

    private boolean isPrimie(int number) {
        for (int i = 2; i < number; i++) {
            if (number % i == 0) {
                return false;
            }
        }
        return true;
    }

    private int getNextPrimie(int number) {
        int next = number+1;
        while (!isPrimie(next)) {
            next += 1;
        }
        return next;
    }

    private void showNext(int i) {
        getNextPrimie(i);
    }


}

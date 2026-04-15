package org.mangorage.test;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorSpecies;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.StructuredTaskScope;

public final class Test {

    public static void main(String[] args) throws Exception {

        System.out.println("=== WAIT FOR ALL ===");
        waitForAllExample();

        System.out.println("\n=== FAIL FAST ===");
        failFastExample();

        System.out.println("\n=== RACE (FIRST RESULT WINS) ===");
        raceExample();
    }

    // ------------------------------------------------------------
    // 1. WAIT FOR ALL TASKS
    // ------------------------------------------------------------
    static void waitForAllExample() throws Exception {

        try (var scope = StructuredTaskScope.open()) {

            var userTask = scope.fork(() -> fetchUser());
            var orderTask = scope.fork(() -> fetchOrder());

            scope.join(); // wait for both

            String user = userTask.get();
            String order = orderTask.get();

            System.out.println("User: " + user);
            System.out.println("Order: " + order);
        }
    }

    // ------------------------------------------------------------
    // 2. FAIL FAST (cancel others if one fails)
    // ------------------------------------------------------------
    static void failFastExample() throws Exception {

        try (var scope = StructuredTaskScope.open(
                StructuredTaskScope.Joiner.awaitAllSuccessfulOrThrow()
        )) {

            var goodTask = scope.fork(() -> fetchUser());

            var badTask = scope.fork(() -> {
                Thread.sleep(200);
                throw new RuntimeException("Database exploded");
            });

            scope.join(); // throws immediately on failure

            // may not reach here if one fails
            System.out.println(goodTask.get());
            System.out.println(badTask.get());
        } catch (Exception e) {
            System.out.println("Failed fast: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------
    // 3. RACE (first successful result wins)
    // ------------------------------------------------------------
    static void raceExample() throws Exception {

        try (var scope = StructuredTaskScope.open(
                StructuredTaskScope.Joiner.<String>anySuccessfulOrThrow()
        )) {

            scope.fork(() -> slowService("A", 500));
            scope.fork(() -> slowService("B", 200));
            scope.fork(() -> slowService("C", 350));

            String result = scope.join(); // first successful wins

            System.out.println("Winner: " + result);
        }
    }

    // ------------------------------------------------------------
    // MOCK METHODS
    // ------------------------------------------------------------

    static String fetchUser() throws InterruptedException {
        Thread.sleep(300);
        return "User{id=1, name=Andy}";
    }

    static String fetchOrder() throws InterruptedException {
        Thread.sleep(400);
        return "Order{total=42.99}";
    }

    static String slowService(String name, int delayMs) throws InterruptedException {
        Thread.sleep(delayMs);
        return "Response from service " + name;
    }
}

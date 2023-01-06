package com.broker.service.locker;

import org.junit.jupiter.api.Test;
import org.springframework.data.util.Pair;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.nullValue;

class LocalLockerTest {
    private static final int THREAD_COUNT = 100;
    private final LocalLocker testedInstance = new LocalLocker();

    @Test
    void testSimpleCase() {
        CompletableFuture<Object> tradeFuture = new CompletableFuture<>();
        UUID tradeId = UUID.fromString("153fe39b-ee35-46e0-9211-ee72bde71bd4");

        testedInstance.addTrade(tradeId, tradeFuture);

        assertThat(testedInstance.getSinglePermit(UUID.fromString("f9d9d209-348e-43dd-98ea-01059d7dbede")), is(nullValue()));
        assertThat(testedInstance.getSinglePermit(tradeId), is(tradeFuture));
        assertThat(testedInstance.getSinglePermit(tradeId), is(nullValue()));
    }

    @Test
    void testMultiThreadCase() throws InterruptedException {
        Map<Integer, Pair<UUID, Future<?>>> sourceData = createData();
        Queue<Pair<UUID, Future<?>>> extractedData = executeGetSinglePermit(sourceData);
        verifyExtractedData(sourceData, extractedData);
    }

    private static void verifyExtractedData(Map<Integer, Pair<UUID, Future<?>>> data, Queue<Pair<UUID, Future<?>>> extractedData) {
        Map<UUID, ? extends Future<?>> dataMap = data.values().stream().collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));

        assertThat(extractedData.size(), is(THREAD_COUNT));

        for (Pair<UUID, Future<?>> extractedElement : extractedData) {
            Future<?> originalFuture = dataMap.remove(extractedElement.getFirst());
            Future<?> extractedFuture = extractedElement.getSecond();

            assertThat(originalFuture, is(extractedFuture));
        }
    }

    private Queue<Pair<UUID, Future<?>>> executeGetSinglePermit(Map<Integer, Pair<UUID, Future<?>>> data) throws InterruptedException {
        Queue<Pair<UUID, Future<?>>> queue = new ConcurrentLinkedQueue<>();

        Collection<Callable<Object>> threads = new ArrayList<>();

        for (int index = 0; index < THREAD_COUNT * 5; index++) {
            Integer mapIndex = index % THREAD_COUNT;

            threads.add( () -> {
                UUID uuid = data.get(mapIndex).getFirst();
                Future<?> singlePermit = testedInstance.getSinglePermit(uuid);
                if (null != singlePermit) {
                    queue.add(Pair.of(uuid, singlePermit));
                }
                return null;
            });
        }

        execute(threads);

        return queue;
    }

    private Map<Integer, Pair<UUID, Future<?>>> createData() throws InterruptedException {
        Collection<Callable<Object>> threads = new ArrayList<>();

        Map<Integer, Pair<UUID, Future<?>>> data = new HashMap<>();

        for (int index = 0; index < THREAD_COUNT; index++) {
            UUID tradeId = UUID.randomUUID();
            Future<Object> future = new CompletableFuture<>();
            data.put(index, Pair.of(tradeId, future));

            threads.add(() -> {
                testedInstance.addTrade(tradeId, future);
                return null;
            });
        }

        execute(threads);

        return data;
    }

    private static void execute(Collection<Callable<Object>> threads) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Future<Object>> futures = executorService.invokeAll(threads);

        executorService.shutdown();

        futures.stream().forEach(f -> {
            try {
                f.get();
            } catch (InterruptedException | ExecutionException e) {

            }
        });
    }
}
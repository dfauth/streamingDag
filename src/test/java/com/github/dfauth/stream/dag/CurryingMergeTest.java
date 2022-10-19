package com.github.dfauth.stream.dag;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

import static com.github.dfauth.stream.dag.CurryUtils.biFunctionTransformer;
import static com.github.dfauth.stream.dag.CurryUtils.curryingMerge;
import static com.github.dfauth.trycatch.TryCatch.tryCatch;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Slf4j
public class CurryingMergeTest {

    @Test
    public void testIt() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<Integer> fut = new CompletableFuture<>();
        Subscriber<Integer> s = CompletableFutureSubscriber.subscribingFuture(fut);
        biFunctionTransformer(Integer::sum).apply(Mono.just(1),Mono.just(2))
                .subscribe(s);
        assertEquals(3, (int)fut.get(10, TimeUnit.SECONDS));
    }

    @Test
    public void testItAgain() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<List<Integer>> fut = new CompletableFuture<>();
        List<Integer> l = new ArrayList<>();
        Subscriber<Integer> s = CompletableFutureSubscriber.subscribingList(fut, l);
        PublishingQueue<Integer> q1 = new PublishingQueue<>();
        PublishingQueue<Integer> q2 = new PublishingQueue<>();
        biFunctionTransformer(Integer::sum).apply(q1,q2).subscribe(s);
        q1.offer(1);
        assertEquals(Collections.emptyList(), l);
        q2.offer(2);
        assertEquals(List.of(3), l);
        q1.offer(2);
        assertEquals(List.of(3,4), l);
        q2.offer(1);
        assertEquals(List.of(3,4,3), l);
        q1.stop();
        q2.stop();
        assertEquals(List.of(3,4,3), fut.get(10, TimeUnit.SECONDS));
    }

    @Test
    public void testItAgainUsingCombineLatest() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<List<Integer>> fut = new CompletableFuture<>();
        List<Integer> l = new ArrayList<>();
        Subscriber<Integer> s = CompletableFutureSubscriber.subscribingList(fut, l);
        PublishingQueue<Integer> q1 = new PublishingQueue<>();
        PublishingQueue<Integer> q2 = new PublishingQueue<>();
        Flux.combineLatest(q1,q2,Integer::sum).subscribe(s);
        q1.offer(1);
        assertEquals(Collections.emptyList(), l);
        q2.offer(2);
        assertEquals(List.of(3), l);
        q1.offer(2);
        assertEquals(List.of(3,4), l);
        q2.offer(1);
        assertEquals(List.of(3,4,3), l);
        q1.stop();
        q2.stop();
        assertEquals(List.of(3,4,3), fut.get(10, TimeUnit.SECONDS));
    }

    @Test
    public void testCurryingMerge() throws InterruptedException, TimeoutException, ExecutionException {

        PublishingQueue<Integer> nodeA = new PublishingQueue<>();
        PublishingQueue<Integer> nodeB = new PublishingQueue<>();

        Queue<Integer> q = new ArrayBlockingQueue<>(10);

        CompletableFuture<Queue<Integer>> f = new CompletableFuture<>();

        Subscriber<Integer> testingSubscriber = CompletableFutureSubscriber.subscribingQueue(f, q);

        biFunctionTransformer(Integer::sum).apply(nodeA, nodeB).subscribe(testingSubscriber);

        assertTrue(q.size() == 0);

        int a=1, b=2;
        log.info("stream created");
        nodeA.offer(a);
        log.info("a updated");
        pause();
        assertTrue(q.size() == 0);

        nodeB.offer(b);
        log.info("b updated");
        pause();
        assertEquals(3, Optional.ofNullable(q.poll()).orElseThrow(() -> new RuntimeException("Oops")).intValue());

        // update a
        a = 2;
        nodeA.offer(a);
        log.info("a updated");
        pause();
        assertEquals(4, Optional.ofNullable(q.poll()).orElseThrow(() -> new RuntimeException("Oops")).intValue());

        // update b
        b = 3;
        nodeB.offer(b);
        log.info("b updated");
        pause();
        assertEquals(5, Optional.ofNullable(q.poll()).orElseThrow(() -> new RuntimeException("Oops")).intValue());

        assertTrue(q.size() == 0);

        nodeA.stop();
        nodeB.stop();

        Queue<Integer> result = f.get(1, TimeUnit.SECONDS);
        assertEquals(result.size(), 0);
        assertTrue(q.size() == 0);
    }


    @Test
    public void testCurryingNode1() throws InterruptedException, TimeoutException, ExecutionException {
        Function<Integer, Function<Double, Float>> sum = a -> b -> a.floatValue() + b.floatValue();

        PublishingQueue<Integer> nodeA = new PublishingQueue<>();
        PublishingQueue<Double> nodeB = new PublishingQueue<>();

        Queue<Float> q = new ArrayBlockingQueue<>(10);

        CompletableFuture<Queue<Float>> f = new CompletableFuture<>();

        Subscriber<Float> testingSubscriber = CompletableFutureSubscriber.subscribingQueue(f, q);

        biFunctionTransformer(sum).apply(nodeA, nodeB).subscribe(testingSubscriber);

        int a=1;
        double b=2;
        log.info("stream created");
        nodeA.offer(a);
        log.info("a updated");
        pause();
        assertTrue(q.size() == 0);
        nodeB.offer(b);
        log.info("b updated");
        pause();
        assertEquals(3, q.poll().intValue());

        // update a
        a = 2;
        nodeA.offer(a);
        log.info("a updated");
        assertEquals(4, q.poll().intValue());

        // update b
        b = 3;
        nodeB.offer(b);
        log.info("b updated");
        assertEquals(5, q.poll().intValue());

        assertTrue(q.size() == 0);

        nodeA.stop();
        nodeB.stop();

        Queue<Float> result = f.get(1, TimeUnit.SECONDS);
        assertEquals(result.size(), 0);
        assertTrue(q.size() == 0);
    }

    @Test
    public void testCurryingNode2() throws InterruptedException, TimeoutException, ExecutionException {
//        Function<Integer, Function<Integer, Function<Integer, Integer>>> sum = a -> b -> c -> a + b + c;
        Function<Integer, Function<Double, Function<Float, Float>>> sum = a -> b -> c -> a.floatValue() + b.floatValue() + c;

        PublishingQueue<Integer> nodeA = new PublishingQueue<>();
        PublishingQueue<Double> nodeB = new PublishingQueue<>();
        PublishingQueue<Float> nodeC = new PublishingQueue<>();

        Queue<Float> q = new ArrayBlockingQueue<>(10);

        CompletableFuture<Queue<Float>> f = new CompletableFuture<>();

        Subscriber<Float> testingSubscriber = CompletableFutureSubscriber.subscribingQueue(f, q);

        ((Publisher<Float>)curryingMerge(sum, nodeA, nodeB, nodeC)).subscribe(testingSubscriber);

        int a=1;
        double b=2;
        float c=3;
        log.info("stream created");
        nodeA.offer(a);
        log.info("a updated");
        pause();
        assertTrue(q.size() == 0);
        nodeB.offer(b);
        log.info("b updated");
        pause();
        assertTrue(q.size() == 0);
        nodeC.offer(c);
        log.info("c updated");
        assertEquals(6, q.poll().intValue());

        // update a
        a = 2;
        nodeA.offer(a);
        log.info("a updated");
        assertEquals(7, q.poll().intValue());

        // update b
        b = 3;
        nodeB.offer(b);
        log.info("b updated");
        assertEquals(8, q.poll().intValue());

        // update c
        c = 4;
        nodeC.offer(c);
        log.info("c updated");
        assertEquals(9, q.poll().intValue());
        assertTrue(q.size() == 0);

        nodeA.stop();
        nodeB.stop();
        nodeC.stop();

        Queue<Float> result = f.get(1, TimeUnit.SECONDS);
        assertEquals(result.size(), 0);
        assertTrue(q.size() == 0);
    }

    @Test
    public void testCurryingNode3() throws InterruptedException, TimeoutException, ExecutionException {
        Function<Integer, Function<Double, Function<Float, Function<BigDecimal, Integer>>>> sum = a -> b -> c -> d -> d
                .add(BigDecimal
                        .valueOf(a))
                .add(BigDecimal
                        .valueOf(b))
                .add(BigDecimal
                        .valueOf(c))
                .intValue();

        PublishingQueue<Integer> nodeA = new PublishingQueue<>();
        PublishingQueue<Double> nodeB = new PublishingQueue<>();
        PublishingQueue<Float> nodeC = new PublishingQueue<>();
        PublishingQueue<BigDecimal> nodeD = new PublishingQueue<>();

        Queue<Integer> q = new ArrayBlockingQueue<>(10);

        CompletableFuture<Queue<Integer>> f = new CompletableFuture<>();

        Subscriber<Integer> testingSubscriber = CompletableFutureSubscriber.subscribingQueue(f, q);

        ((Publisher<Integer>)curryingMerge(sum, nodeA, nodeB, nodeC, nodeD)).subscribe(testingSubscriber);

        int a=1;
        double b=2;
        float c=3;
        BigDecimal d = BigDecimal.valueOf(4);
        log.info("stream created");
        nodeA.offer(a);
        log.info("a updated");
        pause();
        assertTrue(q.size() == 0);
        nodeB.offer(b);
        log.info("b updated");
        pause();
        assertTrue(q.size() == 0);
        nodeC.offer(c);
        log.info("c updated");
        assertTrue(q.size() == 0);
        nodeD.offer(d);
        log.info("d updated");
        assertEquals(10, q.poll().intValue());

        // update a
        a = 2;
        nodeA.offer(a);
        log.info("a updated");
        assertEquals(11, q.poll().intValue());

        // update b
        b = 3;
        nodeB.offer(b);
        log.info("b updated");
        assertEquals(12, q.poll().intValue());

        // update c
        c = 4;
        nodeC.offer(c);
        log.info("c updated");
        assertEquals(13, q.poll().intValue());
        assertTrue(q.size() == 0);

        // update d
        d = BigDecimal.valueOf(5);
        nodeD.offer(d);
        log.info("d updated");
        assertEquals(14, q.poll().intValue());
        assertTrue(q.size() == 0);

        // update c again
        nodeC.offer(7f);
        assertEquals(17, q.poll().intValue());
        assertTrue(q.size() == 0);

        nodeA.stop();
        nodeB.stop();
        nodeC.stop();
        nodeD.stop();

        Queue<Integer> result = f.get(1, TimeUnit.SECONDS);
        assertEquals(result.size(), 0);
        assertTrue(q.size() == 0);
    }

    public static void pause() {
        log.info("pausing");
        tryCatch(() -> Thread.sleep(200));
    }
}

package com.github.dfauth.stream.dag;

import org.junit.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Function;

import static com.github.dfauth.stream.dag.CurryUtils.biFunctionTransformer;
import static com.github.dfauth.stream.dag.CurryUtils.curryingMerge;
import static com.github.dfauth.stream.dag.Utils.subscribingFuture;
import static com.github.dfauth.stream.dag.Utils.subscribingList;
import static com.github.dfauth.trycatch.TryCatch.tryCatch;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CurryingMergeTest {

    private static final Logger logger = LoggerFactory.getLogger(CurryingMergeTest.class);

    @Test
    public void testIt() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<Integer> fut = new CompletableFuture<>();
        Subscriber<Integer> s = subscribingFuture(fut);
        biFunctionTransformer(Integer::sum).apply(Mono.just(1),Mono.just(2))
                .subscribe(s);
        assertEquals(3, (int)fut.get(10, TimeUnit.SECONDS));
    }

    @Test
    public void testItAgain() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<List<Integer>> fut = new CompletableFuture<>();
        List<Integer> l = new ArrayList<>();
        Subscriber<Integer> s = subscribingList(fut, l);
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
    public void testCurryingMerge() throws InterruptedException, TimeoutException, ExecutionException {

        Node<Integer, Integer> nodeA = Node.identity();
        Node<Integer, Integer> nodeB = Node.identity();

        BlockingQueue<Integer> q = new ArrayBlockingQueue<>(10);

        TestingSubscriber<Integer> testingSubscriber = new TestingSubscriber<>(r -> {
            q.offer(r);
            logger.info("r is " + r);
        });

        biFunctionTransformer(Integer::sum).apply(nodeA, nodeB).subscribe(testingSubscriber);

        assertTrue(q.size() == 0);

        int a=1, b=2;
        logger.info("stream created");
        nodeA.update(a);
        logger.info("a updated");
        pause();
        assertTrue(q.size() == 0);

        nodeB.update(b);
        logger.info("b updated");
        pause();
        assertEquals(3, Optional.ofNullable(q.poll(2, TimeUnit.SECONDS)).orElseThrow(() -> new RuntimeException("Oops")).intValue());

        // update a
        a = 2;
        nodeA.update(a);
        logger.info("a updated");
        pause();
        assertEquals(4, Optional.ofNullable(q.poll(2, TimeUnit.SECONDS)).orElseThrow(() -> new RuntimeException("Oops")).intValue());

        // update b
        b = 3;
        nodeB.update(b);
        logger.info("b updated");
        pause();
        assertEquals(5, Optional.ofNullable(q.poll(2, TimeUnit.SECONDS)).orElseThrow(() -> new RuntimeException("Oops")).intValue());

        assertTrue(q.size() == 0);

        nodeA.stop();
        nodeB.stop();

        List<Integer> result = testingSubscriber.toCompletableFuture().get(1, TimeUnit.SECONDS);
        assertEquals(result.size(), 3); // four events
        assertTrue(q.size() == 0);
    }


    @Test
    public void testCurryingNode1() throws InterruptedException, TimeoutException, ExecutionException {
        Function<Integer, Function<Double, Float>> sum = a -> b -> a.floatValue() + b.floatValue();

        BlockingQueue<Float> q = new ArrayBlockingQueue<>(10);

        TestingSubscriber<Float> testingSubscriber = new TestingSubscriber<>(r -> {
            q.offer(r);
            logger.info("r is " + r);
        });

        Node<Integer, Integer> nodeA = Node.identity();
        Node<Double, Double> nodeB = Node.identity();

        ((Publisher<Float>)curryingMerge(sum, nodeA, nodeB)).subscribe(testingSubscriber);

        int a=1;
        double b=2;
        logger.info("stream created");
        nodeA.update(a);
        logger.info("a updated");
        pause();
        assertTrue(q.size() == 0);
        nodeB.update(b);
        logger.info("b updated");
        pause();
        assertEquals(3, q.poll(2, TimeUnit.SECONDS).intValue());

        // update a
        a = 2;
        nodeA.update(a);
        logger.info("a updated");
        assertEquals(4, q.poll(2, TimeUnit.SECONDS).intValue());

        // update b
        b = 3;
        nodeB.update(b);
        logger.info("b updated");
        assertEquals(5, q.poll(2, TimeUnit.SECONDS).intValue());

        assertTrue(q.size() == 0);

        nodeA.stop();
        nodeB.stop();

        List<Float> result = testingSubscriber.toCompletableFuture().get(1, TimeUnit.SECONDS);
        assertEquals(result.size(), 3); // three events
        assertTrue(q.size() == 0);
    }

    @Test
    public void testCurryingNode2() throws InterruptedException, TimeoutException, ExecutionException {
//        Function<Integer, Function<Integer, Function<Integer, Integer>>> sum = a -> b -> c -> a + b + c;
        Function<Integer, Function<Double, Function<Float, Float>>> sum = a -> b -> c -> a.floatValue() + b.floatValue() + c;

        BlockingQueue<Float> q = new ArrayBlockingQueue<>(10);

        TestingSubscriber<Float> testingSubscriber = new TestingSubscriber<>(r -> {
            q.offer(r);
            logger.info("r is " + r);
        });

        Node<Integer, Integer> nodeA = Node.identity();
        Node<Double, Double> nodeB = Node.identity();
        Node<Float, Float> nodeC = Node.identity();

        ((Publisher<Float>)curryingMerge(sum, nodeA, nodeB, nodeC)).subscribe(testingSubscriber);

        int a=1;
        double b=2;
        float c=3;
        logger.info("stream created");
        nodeA.update(a);
        logger.info("a updated");
        pause();
        assertTrue(q.size() == 0);
        nodeB.update(b);
        logger.info("b updated");
        pause();
        assertTrue(q.size() == 0);
        nodeC.update(c);
        logger.info("c updated");
        assertEquals(6, q.poll(2, TimeUnit.SECONDS).intValue());

        // update a
        a = 2;
        nodeA.update(a);
        logger.info("a updated");
        assertEquals(7, q.poll(2, TimeUnit.SECONDS).intValue());

        // update b
        b = 3;
        nodeB.update(b);
        logger.info("b updated");
        assertEquals(8, q.poll(2, TimeUnit.SECONDS).intValue());

        // update c
        c = 4;
        nodeC.update(c);
        logger.info("c updated");
        assertEquals(9, q.poll(2, TimeUnit.SECONDS).intValue());
        assertTrue(q.size() == 0);

        nodeA.stop();
        nodeB.stop();
        nodeC.stop();

        List<Float> result = testingSubscriber.toCompletableFuture().get(1, TimeUnit.SECONDS);
        assertEquals(result.size(), 4); // four events
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

        BlockingQueue<Integer> q = new ArrayBlockingQueue<>(10);

        TestingSubscriber<Integer> testingSubscriber = new TestingSubscriber<>(r -> {
            q.offer(r);
            logger.info("r is " + r);
        });

        Node<Integer, Integer> nodeA = Node.identity();
        Node<Double, Double> nodeB = Node.identity();
        Node<Float, Float> nodeC = Node.identity();
        Node<BigDecimal, BigDecimal> nodeD = Node.identity();

        ((Publisher<Integer>)curryingMerge(sum, nodeA, nodeB, nodeC, nodeD)).subscribe(testingSubscriber);

        int a=1;
        double b=2;
        float c=3;
        BigDecimal d = BigDecimal.valueOf(4);
        logger.info("stream created");
        nodeA.update(a);
        logger.info("a updated");
        pause();
        assertTrue(q.size() == 0);
        nodeB.update(b);
        logger.info("b updated");
        pause();
        assertTrue(q.size() == 0);
        nodeC.update(c);
        logger.info("c updated");
        assertTrue(q.size() == 0);
        nodeD.update(d);
        logger.info("d updated");
        assertEquals(10, q.poll(2, TimeUnit.SECONDS).intValue());

        // update a
        a = 2;
        nodeA.update(a);
        logger.info("a updated");
        assertEquals(11, q.poll(2, TimeUnit.SECONDS).intValue());

        // update b
        b = 3;
        nodeB.update(b);
        logger.info("b updated");
        assertEquals(12, q.poll(2, TimeUnit.SECONDS).intValue());

        // update c
        c = 4;
        nodeC.update(c);
        logger.info("c updated");
        assertEquals(13, q.poll(2, TimeUnit.SECONDS).intValue());
        assertTrue(q.size() == 0);

        // update d
        d = BigDecimal.valueOf(5);
        nodeD.update(d);
        logger.info("d updated");
        assertEquals(14, q.poll(2, TimeUnit.SECONDS).intValue());
        assertTrue(q.size() == 0);

        nodeA.stop();
        nodeB.stop();
        nodeC.stop();
        nodeD.stop();

        List<Integer> result = testingSubscriber.toCompletableFuture().get(1, TimeUnit.SECONDS);
        assertEquals(result.size(), 5); // five events
        assertTrue(q.size() == 0);
    }

    public static void pause() {
        logger.info("pausing");
        tryCatch(() -> Thread.sleep(200));
    }
}

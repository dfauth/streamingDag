package com.github.dfauth.stream.dag;

import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.function.BinaryOperator;

import static com.github.dfauth.stream.dag.CurryingMergeTest.pause;
import static com.github.dfauth.stream.dag.CompletableFutureSubscriber.subscribingQueue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CurryingTransformationTest {

    private static final Logger logger = LoggerFactory.getLogger(CurryingTransformationTest.class);

    @Test
    public void testCurryingTransformation() throws InterruptedException, TimeoutException, ExecutionException {
        BinaryOperator<Integer> sum = Integer::sum;

        PublishingQueue<Integer> nodeA = new PublishingQueue<>();
        PublishingQueue<Integer> nodeB = new PublishingQueue<>();

        Queue<Integer> q = new ArrayBlockingQueue<>(10);
        CompletableFuture<Queue<Integer>> f = new CompletableFuture<>();

        Subscriber<Integer> testingSubscriber = subscribingQueue(f, q);

        CurryUtils.combineLatest(sum).apply(nodeA).apply(nodeB).subscribe(testingSubscriber);

        assertTrue(q.size() == 0);

        int a=1, b=2;
        logger.info("stream created");
        nodeA.offer(a);
        logger.info("a updated");
        pause();
        assertTrue(q.size() == 0);

        nodeB.offer(b);
        logger.info("b updated");
        pause();
        assertEquals(3, Optional.ofNullable(q.poll()).orElseThrow(() -> new RuntimeException("Oops")).intValue());

        // update a
        a = 2;
        nodeA.offer(a);
        logger.info("a updated");
        pause();
        assertTrue(q.size() == 0); // not expecting any update

        // update b
        b = 3;
        nodeB.offer(b);
        logger.info("b updated");
        pause();
        assertEquals(5, Optional.ofNullable(q.poll()).orElseThrow(() -> new RuntimeException("Oops")).intValue());

        assertTrue(q.size() == 0);

        nodeA.stop();
        nodeB.stop();

        Queue<Integer> result = f.get(1, TimeUnit.SECONDS);
        assertEquals(0, result.size()); // two events ony expected
        assertTrue(q.size() == 0);
    }
}

package com.github.dfauth.stream.dag;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.BinaryOperator;

import static com.github.dfauth.stream.dag.CurryUtils.curryingTransformation;
import static com.github.dfauth.stream.dag.CurryingMergeTest.pause;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CurryingTransformationTest {

    private static final Logger logger = LoggerFactory.getLogger(CurryingTransformationTest.class);

    @Test
    public void testCurryingTransformation() throws InterruptedException, TimeoutException, ExecutionException {
        BinaryOperator<Integer> sum = (a, b) -> a + b;

        Node<Integer, Integer> nodeA = Node.identity();
        Node<Integer, Integer> nodeB = Node.identity();

        BlockingQueue<Integer> q = new ArrayBlockingQueue<>(10);

        TestingSubscriber<Integer> testingSubscriber = new TestingSubscriber<>(r -> {
            q.offer(r);
            logger.info("r is " + r);
        });

        curryingTransformation(nodeA, sum, nodeB).subscribe(testingSubscriber);

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
        assertTrue(q.size() == 0); // not expecting any update

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
        assertEquals(2, result.size()); // two events ony expected
        assertTrue(q.size() == 0);
    }
}

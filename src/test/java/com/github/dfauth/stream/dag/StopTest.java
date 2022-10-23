package com.github.dfauth.stream.dag;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.reactivestreams.Publisher;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;

import static com.github.dfauth.stream.dag.CachingTransformer.stream;
import static org.junit.Assert.*;

@Slf4j
public class StopTest {

    private static final BiFunction<Integer,Integer, Integer> add = Integer::sum;

    @Test
    public void testOneSidedCachedSideStops() {
        PublishingQueue<Integer> q1 = new PublishingQueue<>();
        PublishingQueue<Integer> q2 = new PublishingQueue<>();

        OneSidedCachingTransformer<Integer, Integer, Integer> t = new OneSidedCachingTransformer<>(add);
        TestSubscriber<Integer> subscriber = new TestSubscriber<>();
        t.apply(q1, q2).subscribe(subscriber);
        q1.offer(1);
        assertTrue(subscriber.isEmpty());
        q2.offer(1);
        assertFalse(subscriber.isEmpty());
        assertEquals(2, (int)subscriber.get());
        q1.offer(2);
        assertFalse(subscriber.isEmpty());
        assertEquals(2, (int)subscriber.get());
        q2.offer(2);
        assertFalse(subscriber.isEmpty());
        assertEquals(4, (int)subscriber.get());
        q1.stop();
        assertTrue(subscriber.monitor().isDone());
        assertTrue(q1.isStopped());
        assertTrue(q2.isStopped());
    }

    @Test
    public void testOneSidedUnCachedSideStops() {
        PublishingQueue<Integer> q1 = new PublishingQueue<>();
        PublishingQueue<Integer> q2 = new PublishingQueue<>();

        OneSidedCachingTransformer<Integer, Integer, Integer> t = new OneSidedCachingTransformer<>(add);
        TestSubscriber<Integer> subscriber = new TestSubscriber<>();
        t.apply(q1, q2).subscribe(subscriber);
        q1.offer(1);
        assertTrue(subscriber.isEmpty());
        q2.offer(1);
        assertFalse(subscriber.isEmpty());
        assertEquals(2, (int)subscriber.get());
        q1.offer(2);
        assertFalse(subscriber.isEmpty());
        assertEquals(2, (int)subscriber.get());
        q2.offer(2);
        assertFalse(subscriber.isEmpty());
        assertEquals(4, (int)subscriber.get());
        q2.stop();
        assertTrue(subscriber.monitor().isDone());
        assertTrue(q1.isStopped());
        assertTrue(q2.isStopped());
    }

    @Test
    public void testOneSidedSubscriberStops() {
        PublishingQueue<Integer> q1 = new PublishingQueue<>();
        PublishingQueue<Integer> q2 = new PublishingQueue<>();

        OneSidedCachingTransformer<Integer, Integer, Integer> t = new OneSidedCachingTransformer<>(add);
        TestSubscriber<Integer> subscriber = new TestSubscriber<>();
        t.apply(q1, q2).subscribe(subscriber);
        q1.offer(1);
        assertTrue(subscriber.isEmpty());
        q2.offer(1);
        assertFalse(subscriber.isEmpty());
        assertEquals(2, (int)subscriber.get());
        q1.offer(2);
        assertFalse(subscriber.isEmpty());
        assertEquals(2, (int)subscriber.get());
        q2.offer(2);
        assertFalse(subscriber.isEmpty());
        assertEquals(4, (int)subscriber.get());
        subscriber.stop();
        assertTrue(subscriber.monitor().isDone());
        assertTrue(q1.isStopped());
        assertTrue(q2.isStopped());
    }

    @Test
    public void testDoubleSidedLeftStopping() {
        PublishingQueue<Integer> q1 = new PublishingQueue<>();
        PublishingQueue<Integer> q2 = new PublishingQueue<>();

        CachingTransformer<Integer, Integer, Integer> t = stream(add);
        Publisher<Integer> result = t.apply(q1, q2);
        TestSubscriber<Integer> subscriber = new TestSubscriber<>();
        result.subscribe(subscriber);
        q1.offer(1);
        assertTrue(subscriber.isEmpty());
        q2.offer(1);
        assertFalse(subscriber.isEmpty());
        assertEquals(2, (int)subscriber.get());
        q1.offer(2);
        assertFalse(subscriber.isEmpty());
        assertEquals(3, (int)subscriber.get());
        q1.stop();
        assertTrue(subscriber.monitor().isDone());
        assertTrue(q1.isStopped());
        assertTrue(q2.isStopped());
    }

    @Test
    public void testDoubleSidedRightStopping() {
        PublishingQueue<Integer> q1 = new PublishingQueue<>();
        PublishingQueue<Integer> q2 = new PublishingQueue<>();

        CachingTransformer<Integer, Integer, Integer> t = stream(add);
        Publisher<Integer> result = t.apply(q1, q2);
        TestSubscriber<Integer> subscriber = new TestSubscriber<>();
        result.subscribe(subscriber);
        q1.offer(1);
        assertTrue(subscriber.isEmpty());
        q2.offer(1);
        assertFalse(subscriber.isEmpty());
        assertEquals(2, (int)subscriber.get());
        q1.offer(2);
        assertFalse(subscriber.isEmpty());
        assertEquals(3, (int)subscriber.get());
//        q1.stop();
        q2.stop();
        assertTrue(subscriber.monitor().isDone());
        assertTrue(q1.isStopped());
        assertTrue(q2.isStopped());
    }

    @Test
    public void testDoubleSidedSubscriberStopping() throws ExecutionException, InterruptedException, TimeoutException {
        PublishingQueue<Integer> q1 = new PublishingQueue<>();
        PublishingQueue<Integer> q2 = new PublishingQueue<>();

        CachingTransformer<Integer, Integer, Integer> t = stream(add);
        Publisher<Integer> result = t.apply(q1, q2);
        TestSubscriber<Integer> subscriber = new TestSubscriber<>();
        result.subscribe(subscriber);
        q1.offer(1);
        assertTrue(subscriber.isEmpty());
        q2.offer(1);
        assertFalse(subscriber.isEmpty());
        assertEquals(2, (int)subscriber.get());
        q1.offer(2);
        assertFalse(subscriber.isEmpty());
        assertEquals(3, (int)subscriber.get());
        subscriber.stop();
        assertTrue(subscriber.monitor().get(1000, TimeUnit.MILLISECONDS).isSuccess());
        assertTrue(q1.isStopped());
        assertTrue(q2.isStopped());
    }
}

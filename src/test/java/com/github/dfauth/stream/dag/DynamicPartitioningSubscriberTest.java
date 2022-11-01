package com.github.dfauth.stream.dag;

import com.github.dfauth.partial.Tuple2;
import org.junit.Test;
import org.reactivestreams.Subscription;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DynamicPartitioningSubscriberTest {

    @Test
    public void testIt() {
        Map<String, TestSubscriber<Tuple2<String,Integer>>> subscribers = new HashMap<>();
        DynamicPartitioningPublisher<Tuple2<String,Integer>,String> p = new DynamicPartitioningPublisher<>(Tuple2::_1, k -> subscribers.computeIfAbsent(k, _k -> new TestSubscriber<>()));

        PublishingQueue<Tuple2<String,Integer>> q = new PublishingQueue<>();
        q.subscribe(p);

        q.offer(Tuple2.of(Key.A.name(),1));
        assertFalse(subscribers.get(Key.A.name()).isEmpty());
        assertTrue(Optional.ofNullable(subscribers.get(Key.B.name())).isEmpty());
        assertTrue(Optional.ofNullable(subscribers.get(Key.C.name())).isEmpty());
        assertTrue(Optional.ofNullable(subscribers.get(Key.D.name())).isEmpty());
        assertTrue(Optional.ofNullable(subscribers.get(Key.E.name())).isEmpty());
        assertTrue(Optional.ofNullable(subscribers.get(Key.F.name())).isEmpty());

        q.offer(Tuple2.of(Key.B.name(),2));
        assertFalse(subscribers.get(Key.A.name()).isEmpty());
        assertFalse(subscribers.get(Key.B.name()).isEmpty());
        assertTrue(Optional.ofNullable(subscribers.get(Key.C.name())).isEmpty());
        assertTrue(Optional.ofNullable(subscribers.get(Key.D.name())).isEmpty());
        assertTrue(Optional.ofNullable(subscribers.get(Key.E.name())).isEmpty());
        assertTrue(Optional.ofNullable(subscribers.get(Key.F.name())).isEmpty());

        q.offer(Tuple2.of(Key.B.name(),3));
        assertFalse(subscribers.get(Key.A.name()).isEmpty());
        assertFalse(subscribers.get(Key.B.name()).isEmpty());
        assertTrue(Optional.ofNullable(subscribers.get(Key.C.name())).isEmpty());
        assertTrue(Optional.ofNullable(subscribers.get(Key.D.name())).isEmpty());
        assertTrue(Optional.ofNullable(subscribers.get(Key.E.name())).isEmpty());
        assertTrue(Optional.ofNullable(subscribers.get(Key.F.name())).isEmpty());

        q.offer(Tuple2.of(Key.C.name(),4));
        assertFalse(subscribers.get(Key.A.name()).isEmpty());
        assertFalse(subscribers.get(Key.B.name()).isEmpty());
        assertFalse(subscribers.get(Key.C.name()).isEmpty());
        assertTrue(Optional.ofNullable(subscribers.get(Key.D.name())).isEmpty());
        assertTrue(Optional.ofNullable(subscribers.get(Key.E.name())).isEmpty());
        assertTrue(Optional.ofNullable(subscribers.get(Key.F.name())).isEmpty());

        q.offer(Tuple2.of(Key.A.name(),5));
        assertFalse(subscribers.get(Key.A.name()).isEmpty());
        assertFalse(subscribers.get(Key.B.name()).isEmpty());
        assertFalse(subscribers.get(Key.C.name()).isEmpty());
        assertTrue(Optional.ofNullable(subscribers.get(Key.D.name())).isEmpty());
        assertTrue(Optional.ofNullable(subscribers.get(Key.E.name())).isEmpty());
        assertTrue(Optional.ofNullable(subscribers.get(Key.F.name())).isEmpty());
    }

    static enum Key {
        A, B, C, D, E, F;

        public static BiFunction<Tuple2<String,Integer>,Integer,Integer> f = (t, l) -> Key.valueOf(t._1()).ordinal();
    }

    static class PartitioningSubscriberImpl<T> implements PartitioningSubscriber<T> {

        private final int partition;
        private final Consumer<T> consumer;
        private Subscription subscription;

        public PartitioningSubscriberImpl(int partition, Consumer<T> consumer) {
            this.partition = partition;
            this.consumer = consumer;
        }

        @Override
        public int partition() {
            return partition;
        }

        @Override
        public void onSubscribe(Subscription subscription) {
            this.subscription = subscription;
            subscription.request(Integer.MAX_VALUE);
        }

        @Override
        public void onNext(T t) {
            consumer.accept(t);
        }

        @Override
        public void onError(Throwable throwable) {
            subscription.cancel();
        }

        @Override
        public void onComplete() {
            subscription.cancel();
        }
    }
}

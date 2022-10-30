package com.github.dfauth.stream.dag;

import com.github.dfauth.partial.Tuple2;
import org.junit.Test;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;

public class PartitioningSubscriberTest {

    @Test
    public void testIt() {
        List<Tuple2<String,Integer>> outA = new ArrayList<>();
        List<Tuple2<String,Integer>> outB = new ArrayList<>();
        List<Tuple2<String,Integer>> outC = new ArrayList<>();
        List<Tuple2<String,Integer>> outD = new ArrayList<>();
        List<Tuple2<String,Integer>> outE = new ArrayList<>();
        List<Tuple2<String,Integer>> outF = new ArrayList<>();
        PartitioningPublisher<Tuple2<String,Integer>> p = new PartitioningPublisher<>(Key.f, Key.values().length);
        p.subscribe(new PartitioningSubscriberImpl<>(Key.A.ordinal(), outA::add));
        p.subscribe(new PartitioningSubscriberImpl<>(Key.B.ordinal(), outB::add));
        p.subscribe(new PartitioningSubscriberImpl<>(Key.C.ordinal(), outC::add));
        p.subscribe(new PartitioningSubscriberImpl<>(Key.D.ordinal(), outD::add));
        p.subscribe(new PartitioningSubscriberImpl<>(Key.E.ordinal(), outE::add));
        p.subscribe(new PartitioningSubscriberImpl<>(Key.F.ordinal(), outF::add));

        PublishingQueue<Tuple2<String,Integer>> q = new PublishingQueue<>();
        q.subscribe(p);

        q.offer(Tuple2.of(Key.A.name(),1));
        assertEquals(1, outA.size());
        assertEquals(0, outB.size());
        assertEquals(0, outC.size());
        assertEquals(0, outD.size());
        assertEquals(0, outE.size());
        assertEquals(0, outF.size());

        q.offer(Tuple2.of(Key.B.name(),1));
        assertEquals(1, outA.size());
        assertEquals(1, outB.size());
        assertEquals(0, outC.size());
        assertEquals(0, outD.size());
        assertEquals(0, outE.size());
        assertEquals(0, outF.size());

        q.offer(Tuple2.of(Key.B.name(),1));
        assertEquals(1, outA.size());
        assertEquals(2, outB.size());
        assertEquals(0, outC.size());
        assertEquals(0, outD.size());
        assertEquals(0, outE.size());
        assertEquals(0, outF.size());

        q.offer(Tuple2.of(Key.C.name(),1));
        assertEquals(1, outA.size());
        assertEquals(2, outB.size());
        assertEquals(1, outC.size());
        assertEquals(0, outD.size());
        assertEquals(0, outE.size());
        assertEquals(0, outF.size());

        q.offer(Tuple2.of(Key.A.name(),1));
        assertEquals(2, outA.size());
        assertEquals(2, outB.size());
        assertEquals(1, outC.size());
        assertEquals(0, outD.size());
        assertEquals(0, outE.size());
        assertEquals(0, outF.size());
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

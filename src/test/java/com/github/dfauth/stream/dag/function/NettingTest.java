package com.github.dfauth.stream.dag.function;

import com.github.dfauth.stream.dag.CachingTransformer;
import com.github.dfauth.stream.dag.PublishingQueue;
import org.junit.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.dfauth.stream.dag.NonCompletingPublisher.supply;
import static com.github.dfauth.stream.dag.function.Optionals.eitherOrReduce;
import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;

public class NettingTest {


    @Test
    public void testIt() throws InterruptedException {
        {
            BigDecimal ri = new BigDecimal("1.1");

            BiFunction<Position, BigDecimal, RiskImpact> f0 = NettingTest::riskFromPosition;
            Function<Position,RiskImpact> f1 = NettingTest::net;
            BiFunction<RiskImpact, BigDecimal, RiskImpact> f2 = NettingTest::risk;
            Function<Position, RiskImpact> f = f1.andThen(Function2.function2(f2).flip().unwind().apply(ri));

            Function<Publisher<Position>, Publisher<RiskImpact>> g1 = CachingTransformer.compose(f1);
            BiFunction<Publisher<RiskImpact>, Publisher<BigDecimal>, Publisher<RiskImpact>> g2 = CachingTransformer.compose(f2);
            PublishingQueue<Position> q = new PublishingQueue<>();
            Publisher<RiskImpact> p = g2.apply(g1.apply(q),supply(ri));
            Thingy<RiskImpact> out = new Thingy<>();
            Flux.from(p).subscribe(out);
            q.offer(new Position("x", new BigDecimal(1), new BigDecimal(2)));
            sleep(1000);
            assertEquals(new RiskImpact("x", new BigDecimal("-1.1")), out.get());
            assertEquals(1, out.count());
            q.offer(new Position("x", new BigDecimal(0), new BigDecimal(1)));
            assertEquals(new RiskImpact("x", new BigDecimal("-1.1")), out.get());
            assertEquals(1, out.count());
        }
    }

    public static RiskImpact net(Position p) {
        return p.net();
    }
    public static RiskImpact risk(RiskImpact ri, BigDecimal riskCoefficient) {
        return ri.multiply(riskCoefficient);
    }
    public static RiskImpact riskFromPosition(Position p, BigDecimal riskCoefficient) {
        return p.net().multiply(riskCoefficient);
    }

    static class Thingy<T> implements Consumer<T> {

        private final AtomicReference<T> ref = new AtomicReference<>();
        private int cnt;

        @Override
        public void accept(T t) {
            ref.set(t);
            cnt++;
        }

        public T get() {
            return ref.get();
        }

        public int count() {
            return cnt;
        }
    }

    @lombok.Builder
    @lombok.Data
    @lombok.EqualsAndHashCode
    static class Position {
        private String id;
        private BigDecimal longPos;
        private BigDecimal shortPos;

        public RiskImpact net() {
            return new RiskImpact(id, eitherOrReduce(longPos,shortPos,BigDecimal::subtract));
        }
    }
    @lombok.Builder
    @lombok.Data
    @lombok.EqualsAndHashCode
    static class RiskImpact {
        private String id;
        private BigDecimal impact;

        public RiskImpact multiply(BigDecimal riskCoefficient) {
            return new RiskImpact(id, eitherOrReduce(impact, riskCoefficient, BigDecimal::multiply));
        }
    }
}

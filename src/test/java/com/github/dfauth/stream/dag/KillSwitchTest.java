package com.github.dfauth.stream.dag;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.github.dfauth.stream.dag.Async.async;
import static com.github.dfauth.stream.dag.KillSwitch.killSwitch;
import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;

@Slf4j
public class KillSwitchTest {

    @Test
    public void testIt() {
        List<Integer> out = new ArrayList<>();
        Flux<Integer> src = Flux.just(1);
        KillSwitch<Integer> k = killSwitch(src);
        Flux.from(k).subscribe(out::add);
        assertEquals(List.of(1),out);
    }

    @Test
    public void testIt1() throws IOException, InterruptedException {
        Monitor.VoidMonitor m = new Monitor.VoidMonitor();
        List<Integer> out1 = create(m);
        List<Integer> out2 = create(m);

        Thread.sleep(10000);
        m.complete();
        int size1 = out1.size();
        int size2 = out2.size();
        Thread.sleep(3000);
        assertEquals(size1, out1.size());
        assertEquals(size2, out2.size());
    }

    private List<Integer> create(Monitor.VoidMonitor m) {
        List<Integer> out = new ArrayList<>();
        PublishingQueue<Integer> q = new PublishingQueue<>();
        Flux<Integer> src = Flux.from(q);
        KillSwitch<Integer> k = killSwitch(src, () -> m);
        Flux.from(k).subscribe(i -> {
            out.add(i);
            assertEquals((int)i, out.size()-1);
            log.info("out: {}",out);
        });
        boolean running = true;
        async(() -> {
            int i = 0;
            long d = 10l + (long) (Math.random()*990l);
            log.info("sleeptime: {}",d);
            while(running) {
                q.offer(i++);
                sleep(d);
            }
        });
        return out;
    }
}

package com.github.dfauth.stream.dag;

import org.reactivestreams.Subscriber;

public interface PartitioningSubscriber<T> extends Subscriber<T> {

    int partition();
}

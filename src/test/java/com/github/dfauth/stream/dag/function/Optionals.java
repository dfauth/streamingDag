package com.github.dfauth.stream.dag.function;

import java.util.Optional;
import java.util.function.BinaryOperator;

public class Optionals {

    public static <T> T eitherOrReduce(T t1, T t2, BinaryOperator<T> f) {
        return Optional.ofNullable(t1)
                .map(_t -> Optional.ofNullable(t2)
                        .map(_r -> f.apply(_t,_r)).orElse(t1))
                .orElse(t2);
    }
}

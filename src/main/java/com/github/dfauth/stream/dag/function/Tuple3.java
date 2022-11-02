package com.github.dfauth.stream.dag.function;

public interface Tuple3<A,B,C> {

    A _1();
    B _2();
    C _3();

    static <A,B,C> Tuple3<A,B,C> tuple3(A a, B b, C c) {
        return new Tuple3<>(){
            @Override
            public A _1() {
                return a;
            }

            @Override
            public B _2() {
                return b;
            }

            @Override
            public C _3() {
                return c;
            }
        };
    }

}

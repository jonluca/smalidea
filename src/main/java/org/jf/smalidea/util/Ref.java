package org.jf.smalidea.util;

import java.util.Objects;

public class Ref <T>{

    private T value;

    public void set(T value){
        this.value = value;
    }

    public T get() {
        return value;
    }

    public Ref() {
    }

    public Ref(T value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ref<?> ref = (Ref<?>) o;
        return Objects.equals(value, ref.value);
    }

    @Override
    public int hashCode() {

        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "Ref{" +
                "value=" + value +
                '}';
    }
}

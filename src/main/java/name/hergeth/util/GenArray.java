package name.hergeth.util;

import java.util.Arrays;
import java.util.Collection;

/*
    Generic Array ..................
 */
public class GenArray<E extends Collection> {

    private final Object[] arr;
    public final int length;

    // constructor
    public GenArray(int length) {
        // Creates a new Object array of specified length
        arr = new Object[length];
        this.length = 1;
    }

    public GenArray(int lengthx, int lengthy) {
        // Creates a new Object array of specified length
        arr = new Object[lengthx * lengthy];
        this.length = lengthy;
    }

    // Function to get Object present at index i in the array
    public E get(int i) {
        @SuppressWarnings("unchecked") final E e = (E) arr[i];
        return e;
    }

    // Function to get Object present at index i in the array
    public E get(int i, int j) {
        @SuppressWarnings("unchecked") final E e = (E) arr[i * length + j];
        return e;
    }

    // Function to set a value e at index i in the array
    public void set(int i, E e) {
        arr[i] = e;
    }

    // Function to set a value e at index i in the array
    public void set(int i, int j, E e) {
        arr[i * length + j] = e;
    }

    @Override
    public String toString() {
        return Arrays.toString(arr);
    }

    public void add(int i, int j, Object o){
        get(i, j).add(o);
    }
}

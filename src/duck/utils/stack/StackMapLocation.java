package duck.utils.stack;

import battlecode.common.MapLocation;

public class StackMapLocation {
    final int capacity;
    final MapLocation[] stack;
    int size;

    public StackMapLocation(int capacity) {
        this.capacity = capacity;
        this.stack = new MapLocation[capacity];
        this.size = 0;
    }

    public void push(MapLocation value) {
        stack[size++] = value;
    }

    public MapLocation pop() {
        return stack[--size];
    }

    public MapLocation peek() {
        return stack[size - 1];
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        size = 0;
    }

    public int size() {
        return size;
    }
}

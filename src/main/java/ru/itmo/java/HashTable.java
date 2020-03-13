package ru.itmo.java;

import java.util.Objects;

public final class HashTable {
    private static final int DEFAULT_CAPACITY = 10;
    private static final float DEFAULT_LOAD_FACTOR = .5f;
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 5;

    private Entry[] elements;
    private boolean[] deletedElements;
    private int capacity;
    private int size;
    private final float loadFactor;
    private int threshold;

    public HashTable() {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public HashTable(int capacity) {
        this(capacity, DEFAULT_LOAD_FACTOR);
    }

    public HashTable(float loadFactor) {
        this(DEFAULT_CAPACITY, loadFactor);
    }

    public HashTable(int capacity, float loadFactor) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity cannot be negative number.");
        }
        if (loadFactor <= 0f || loadFactor > 1f) {
            throw new IllegalArgumentException("Load factor must be in range from 0 excluding to 1 including");
        }
        this.elements = new Entry[capacity];
        this.deletedElements = new boolean[capacity];
        this.capacity = capacity;
        this.size = 0;
        this.loadFactor = loadFactor;
        this.threshold = (int) (capacity * loadFactor);
    }

    public Object put(Object key, Object value) {
        if (isRehashRequired()) {
            rehash();
        }

        int index = getIndexByKey(key);
        var element = elements[index];
        elements[index] = new Entry(key, value);
        deletedElements[index] = false;
        if (element == null) {
            size++;
        }
        return element == null ? null : element.getValue();
    }

    public Object get(Object key) {
        int index = getIndexByKey(key);
        return elements[index] == null ? null : elements[index].getValue();
    }

    public Object remove(Object key) {
        int index = getIndexByKey(key);
        var element = elements[index];
        elements[index] = null;
        if (element != null) {
            size--;
            deletedElements[index] = true;
        }
        return element == null ? null : element.getValue();
    }

    public boolean containsKey(Object key) {
        int index = (Objects.hashCode(key) % capacity + capacity) % capacity;
        int iterations = 0;
        while (iterations < capacity) {
            if (!deletedElements[index] && elements[index] == null) {
                return false;
            }
            if (elements[index] != null && Objects.equals(key, elements[index].key)) {
                return true;
            }
            index = (index + 1) % capacity;
            iterations++;
        }
        return false;
    }

    public int size() {
        return size;
    }

    private int getIndexByKey(Object key) {
        int index = (Objects.hashCode(key) % capacity + capacity) % capacity;
        if (containsKey(key)) {
            while (!(elements[index] != null && Objects.equals(key, elements[index].key))) {
                index = (index + 1) % capacity;
            }
        } else {
            while (elements[index] != null) {
                index = (index + 1) % capacity;
            }
        }
        return index;
    }

    private boolean isRehashRequired() {
        return size >= threshold;
    }

    private void rehash() {
        if (capacity == MAX_ARRAY_SIZE) {
            if (size == capacity) {
                throw new IllegalStateException("Table is full");
            }
            return;
        }
        capacity = 2 * capacity + 1;
        size = 0;
        if (capacity < 0) {
            capacity = MAX_ARRAY_SIZE;
        }

        threshold = (int) (capacity * loadFactor);
        var oldElements = elements;
        elements = new Entry[capacity];
        deletedElements = new boolean[capacity];
        for (var element : oldElements) {
            if (element != null) {
                put(element.getKey(), element.getValue());
            }
        }
    }

    private static class Entry {
        private Object key;
        private Object value;

        Entry(Object key, Object value) {
            this.key = key;
            this.value = value;
        }

        public Object getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry entry = (Entry) o;
            return Objects.equals(key, entry.key) &&
                    Objects.equals(value, entry.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, value);
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "key=" + key +
                    ", value=" + value +
                    '}';
        }
    }

}

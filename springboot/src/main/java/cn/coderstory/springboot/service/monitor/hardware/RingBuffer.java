package cn.coderstory.springboot.service.monitor.hardware;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 泛型环形缓冲区。
 * <p>
 * 固定容量的环形缓冲区，支持添加元素和获取当前全部元素的快照。
 * 使用 {@link ReentrantReadWriteLock} 保证线程安全：
 * add() 方法获取写锁，snapshot() 方法获取读锁，支持多读单写并发访问。
 * <p>
 * 当缓冲区满时，新元素会覆盖最旧的元素。snapshot() 按插入顺序返回元素列表。
 *
 * @param <T> 缓冲区元素类型
 */
@Data
public class RingBuffer<T> {

    private final T[] buffer;
    private final int capacity;
    private int head = 0;
    private int count = 0;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * 创建指定容量的环形缓冲区。
     *
     * @param capacity 缓冲区容量（必须为正整数）
     */
    @SuppressWarnings("unchecked")
    public RingBuffer(int capacity) {
        this.capacity = capacity;
        this.buffer = (T[]) new Object[capacity];
    }

    /**
     * 向缓冲区添加元素。
     * <p>
     * 如果缓冲区已满，新元素会覆盖最旧的元素。
     * 此方法使用写锁保证线程安全。
     *
     * @param item 要添加的元素
     */
    public void add(T item) {
        lock.writeLock().lock();
        try {
            buffer[head] = item;
            head = (head + 1) % capacity;
            if (count < capacity) {
                count++;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 获取缓冲区中所有元素的快照。
     * <p>
     * 按插入顺序返回元素列表（从最旧到最新）。
     * 此方法使用读锁，允许多个线程同时读取。
     *
     * @return 按插入顺序排列的元素列表
     */
    public List<T> snapshot() {
        lock.readLock().lock();
        try {
            List<T> result = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                int idx = (head - count + i + capacity) % capacity;
                result.add(buffer[idx]);
            }
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 返回当前缓冲区中的元素数量。
     *
     * @return 元素数量
     */
    public int size() {
        lock.readLock().lock();
        try {
            return count;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 返回缓冲区容量。
     *
     * @return 容量
     */
    public int capacity() {
        return capacity;
    }
}

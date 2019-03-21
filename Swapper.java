package swapper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Swapper<E> {
    private Set<E> set;
    private final ReentrantLock lock;
    private final Condition containsRemoved;

    public Swapper() {
        set = new HashSet();
        lock = new ReentrantLock();
        containsRemoved = lock.newCondition();
    }

    public Collection<E> getValueSet() {
        return set;
    }

    private void swapAtomic(Collection<E> removed, Collection<E> added) {
        set.removeAll(removed);
        set.addAll(added);
    }

    public void swap(Collection<E> removed, Collection<E> added) throws InterruptedException {
        lock.lock();
        try {
            while (!set.containsAll(removed)) {
                containsRemoved.await();
            }
            swapAtomic(removed, added);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } finally {
            containsRemoved.signalAll();
            lock.unlock();
        }
    }
}
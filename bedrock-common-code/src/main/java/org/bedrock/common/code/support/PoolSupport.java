/* Copyright (c) 2008-2023, Nathan Sweet
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * - Neither the name of Esoteric Software nor the names of its contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */

package org.bedrock.common.code.support;

import lombok.Getter;

import java.lang.ref.SoftReference;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 一个可以重复使用的对象池，以避免分配。该池可选地具有线程安全性
 */
public class PoolSupport<T> {

	private final Queue<T> freeObjects;

	private final Supplier<T> createSupplier;

	public static <T> PoolSuppersBuilder<T> builder(Supplier<T> createSupplier) {
		return new PoolSuppersBuilder<>(createSupplier);
	}

	public static class PoolSuppersBuilder<T> {

		/**
		 * 阻塞队列
		 */
		private boolean threadSafe = false;

		/**
		 * 软引用
		 */
		private boolean softReferences = true;

		/**
		 * 队列最大容量
		 */
		private int maximumCapacity = Integer.MAX_VALUE;

		private final Supplier<T> createSupplier;

		public PoolSuppersBuilder(Supplier<T> createSupplier) {
			this.createSupplier = createSupplier;
		}

		public PoolSuppersBuilder<T> threadSafe(boolean threadSafe) {
			this.threadSafe = threadSafe;
			return this;
		}

		public PoolSuppersBuilder<T> softReferences(boolean softReferences) {
			this.softReferences = softReferences;
			return this;
		}

		public PoolSuppersBuilder<T> maximumCapacity(int maximumCapacity) {
			this.maximumCapacity = maximumCapacity;
			return this;
		}

		public PoolSupport<T> build() {
			return new PoolSupport<>(threadSafe, softReferences, maximumCapacity, createSupplier);
		}
	}

	/**
	 * -- GETTER --
	 * The all-time highest number of free objects. This can help determine if a pool's maximum capacity is set appropriately. It
	 * can be reset any time with
	 * .
	 * <p>
	 * If using soft references, this number may include objects that have been garbage collected.
	 */
	@Getter
	private int peak;

	/**
	 * @param maximumCapacity The maximum number of free objects to store in this pool. Objects are not created until
	 *                        {@link #obtain()} is called and no free objects are available.
	 */
	private PoolSupport(boolean threadSafe, boolean softReferences, final int maximumCapacity, Supplier<T> createSupplier) {
		this.createSupplier = createSupplier;
		Queue<T> queue;
		if (threadSafe)
			queue = new LinkedBlockingQueue<>(maximumCapacity) {
				@Override
				public boolean add(T o) {
					return super.offer(o);
				}
			};
		else if (softReferences) {
			queue = new LinkedList<>() { // More efficient clean() than ArrayDeque.
				public boolean add(T object) {
					if (size() >= maximumCapacity) return false;
					super.add(object);
					return true;
				}
			};
		} else {
			queue = new ArrayDeque<>() {
				public boolean offer(T object) {
					if (size() >= maximumCapacity) return false;
					super.offer(object);
					return true;
				}
			};
		}
		freeObjects = softReferences ? new SoftReferenceQueue<>(((Queue<SoftReference<T>>) queue)) : queue;
	}

	/**
	 * Returns an object from this pool. The object may be new or reused (previously {@link #free(Object)
	 * freed}).
	 */
	public T obtain() {
		T object = freeObjects.poll();
		return object != null ? object : createSupplier.get();
	}

	public <V> V run(Function<T, V> callable) {
		T t = obtain();
		try {
			return callable.apply(t);
		} finally {
			if (t != null) {
				free(t);
			}
		}
	}

	/**
	 * Puts the specified object in the pool, making it eligible to be returned by {@link #obtain()}. If the pool already contains
	 * the maximum number of free objects, the specified object is reset but not added to the pool.
	 * <p>
	 * If using soft references and the pool contains the maximum number of free objects, the first soft reference whose object has
	 * been garbage collected is discarded to make room.
	 */
	public void free(T object) {
		if (object == null) throw new IllegalArgumentException("object cannot be null.");
		if (!freeObjects.offer(object) && freeObjects instanceof SoftReferenceQueue) {
			((SoftReferenceQueue<T>) freeObjects).cleanOne();
			freeObjects.offer(object);
		}
		peak = Math.max(peak, freeObjects.size());
	}

	/**
	 * Removes all free objects from this pool.
	 */
	public void clear() {
		freeObjects.clear();
	}

	/**
	 * If using soft references, all soft references whose objects have been garbage collected are removed from the pool. This can
	 * be useful to reduce the number of objects in the pool before calling {@link #getFree()} or when the pool has no maximum
	 * capacity. It is not necessary to call {@link #clean()} before calling {@link #free(Object)}, which will try to remove an
	 * empty reference if the maximum capacity has been reached.
	 */
	public void clean() {
		if (freeObjects instanceof SoftReferenceQueue) ((SoftReferenceQueue<T>) freeObjects).clean();
	}

	/**
	 * The number of objects available to be obtained.
	 * <p>
	 * If using soft references, this number may include objects that have been garbage collected. {@link #clean()} may be used
	 * first to remove empty soft references.
	 */
	public int getFree() {
		return freeObjects.size();
	}

	public void resetPeak() {
		peak = 0;
	}

	/**
	 * Wraps queue values with {@link SoftReference} for {@link PoolSupport}.
	 *
	 * @author Martin Grotzke
	 */
	static class SoftReferenceQueue<T> implements Queue<T> {

		private final Queue<SoftReference<T>> delegate;

		public SoftReferenceQueue(Queue<SoftReference<T>> delegate) {
			this.delegate = delegate;
		}

		public T poll() {
			while (true) {
				SoftReference<T> reference = delegate.poll();
				if (reference == null) return null;
				T object = reference.get();
				if (object != null) return object;
			}
		}

		public boolean offer(T e) {
			return delegate.add(new SoftReference<>(e));
		}

		public int size() {
			return delegate.size();
		}

		public void clear() {
			delegate.clear();
		}

		void cleanOne() {
			for (Iterator<SoftReference<T>> iter = delegate.iterator(); iter.hasNext(); ) {
				if (iter.next().get() == null) {
					iter.remove();
					break;
				}
			}
		}

		void clean() {
			delegate.removeIf(o -> o.get() == null);
		}

		public boolean add(T e) {
			return false;
		}

		public boolean isEmpty() {
			return false;
		}

		public boolean contains(Object o) {
			return false;
		}

		public Iterator<T> iterator() {
			return null;
		}

		public T remove() {
			return null;
		}

		public Object[] toArray() {
			return null;
		}

		public T element() {
			return null;
		}

		public T peek() {
			return null;
		}

		public <E> E[] toArray(E[] a) {
			return null;
		}

		public boolean remove(Object o) {
			return false;
		}

		public boolean containsAll(Collection c) {
			return false;
		}

		public boolean addAll(Collection<? extends T> c) {
			return false;
		}

		public boolean removeAll(Collection c) {
			return false;
		}

		public boolean retainAll(Collection c) {
			return false;
		}
	}
}

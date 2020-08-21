/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.common.utils;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * More supplier utils
 */
public class MoreSupplierUtils {

    private MoreSupplierUtils() {
        throw new UnsupportedOperationException();
    }

    private static final ThrowableConsumer<?, RuntimeException> DEFAULT_THROWABLE_CONSUMER = i -> {
    };

    private static final Consumer<?> DEFAULT_CONSUMER = i -> {
    };

    /**
     * create an lazy closeable supplier to lazy init variable with nothing-doing closeConsumer
     * <p></p>
     *
     * @param delegate the supplier delegate
     * @param <T> the element of supplier will supply
     * @return a lazy closeable supplier
     */
    @SuppressWarnings("unchecked")
    public static <T> LazyCloseableSupplier<T> lazy(Supplier<T> delegate) {
        return lazyCloseable(delegate, (Consumer<T>) DEFAULT_CONSUMER);
    }

    /**
     * create an lazy closeable supplier to lazy init variable
     * <p></p>
     *
     * @param delegate the supplier delegate
     * @param closeConsumer the supplier delegate close consumer function
     * @param <T> the element of supplier will supply
     * @return a lazy closeable supplier
     */
    @SuppressWarnings("unchecked")
    public static <T> LazyCloseableSupplier<T> lazyCloseable(Supplier<T> delegate, Consumer<T> closeConsumer) {
        return delegate instanceof LazyCloseableSupplier
                ? (LazyCloseableSupplier<T>) delegate : new LazyCloseableSupplier<>(delegate::get, closeConsumer::accept);
    }

    /**
     * create an lazy closeable, throwable supplier to lazy init variable with nothing-doing closeConsumer
     * <p></p>
     *
     * @param delegate the supplier delegate
     * @param <T> the element of supplier will supply
     * @return a lazy closeable supplier
     */
    @SuppressWarnings("unchecked")
    public static <T, X extends Throwable> LazyCloseableThrowableSupplier<T, X, RuntimeException> lazyThrowable(
            ThrowableSupplier<T, X> delegate) {
        return lazyCloseableThrowable(Preconditions.checkNotNull(delegate), (ThrowableConsumer<T, RuntimeException>) DEFAULT_THROWABLE_CONSUMER);
    }

    /**
     * create an lazy closeable, throwable supplier to lazy init variable
     * <p></p>
     *
     * @param delegate the supplier delegate
     * @param closeConsumer the supplier delegate close consumer function
     * @param <T> the element of supplier will supply
     * @return a lazy closeable supplier
     */
    public static <T, X extends Throwable, Y extends Throwable> LazyCloseableThrowableSupplier<T, X, Y> lazyCloseableThrowable(
            ThrowableSupplier<T, X> delegate, ThrowableConsumer<T, Y> closeConsumer) {
        return delegate instanceof LazyCloseableThrowableSupplier
                ? (LazyCloseableThrowableSupplier<T, X, Y>) delegate : new LazyCloseableThrowableSupplier<>(
                Preconditions.checkNotNull(delegate), Preconditions.checkNotNull(closeConsumer));
    }

    /**
     * supplier can provide lazy init-able, closeable, throwable design pattern util
     */
    public static class LazyCloseableThrowableSupplier<T, X extends Throwable, Y extends Throwable>
            implements ThrowableSupplier<T, X> {

        private volatile boolean initialized;

        private T value;

        private final ThrowableSupplier<T, X> delegate;

        private final ThrowableConsumer<T, Y> closeConsumer;

        public LazyCloseableThrowableSupplier(ThrowableSupplier<T, X> delegate, ThrowableConsumer<T, Y> closeConsumer) {
            this.delegate = Preconditions.checkNotNull(delegate);
            this.closeConsumer = Preconditions.checkNotNull(closeConsumer);
        }

        @Override
        public T get() throws X {
            if (!this.initialized) {
                synchronized (this) {
                    if (!this.initialized) {
                        this.value = this.delegate.get();
                        this.initialized = true;
                        return this.value;
                    }
                }
            }
            return this.value;
        }

        public void close() throws Y {
            if (this.initialized) {
                synchronized (this) {
                    if (this.initialized) {
                        this.closeConsumer.accept(this.value);
                        this.value = null;
                        this.initialized = false;
                    }
                }
            }
        }

        public boolean isInitialized() {
            return initialized;
        }
    }

    /**
     * supplier can provide lazy init-able and closeable design pattern util
     */
    public static class LazyCloseableSupplier<T> extends LazyCloseableThrowableSupplier<T, RuntimeException, RuntimeException> {

        public LazyCloseableSupplier(
                ThrowableSupplier<T, RuntimeException> delegate,
                ThrowableConsumer<T, RuntimeException> closeConsumer) {
            super(delegate, closeConsumer);
        }
    }
}

package loom.generators.mvysny;

import java.util.Iterator;

public class Coroutine {

    public static final class Yielder<E> {
        private ContinuationInvoker continuationInvoker = null;
        /**
         * This will temporarily hold the item passed to {@link #yield(E)}.
         */
        private E item;

        /**
         * Generate an item. The item is immediately returned via {@link Iterator#next()}.
         *
         * @param item the item to be returned by the iterator, may be null.
         */
        public void yield(E item) {
            this.item = item;
            continuationInvoker.suspend();
        }

        public E getItem() {
            return item;
        }

        public void resetItem() {
            item = null;
        }

        public void setContinuationInvoker(ContinuationInvoker continuationInvoker) {
            this.continuationInvoker = continuationInvoker;
        }
    }

}

/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.api;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.concepts.Registration;

/**
 * A chain of transactions. Transactions in a chain need to be committed in sequence and each transaction should see
 * the effects of previous committed transactions as they occurred. A chain makes no guarantees of atomicity across
 * the chained transactions - the transactions are committed as soon as possible in the order that they were committed.
 * This behaviour is different from the default AsyncDataBroker, where a transaction is always created from the current
 * global state, not taking into account any transactions previously committed by the calling thread. Due to
 * the asynchronous nature of transaction submission this can lead to surprising results. If a thread executes
 * the following sequence sufficiently quickly:
 *
 * <code>
 * AsyncWriteTransaction t1 = broker.newWriteOnlyTransaction();
 * t1.put(id, data);
 * t1.commit();
 *
 * AsyncReadTransaction t2 = broker.newReadOnlyTransaction();
 * Optional&lt;?&gt; maybeData = t2.read(id).get();
 * </code>
 * it may happen, that it sees maybeData.isPresent() == false, simply because t1 has not completed the processes
 * of being applied and t2 is actually allocated from the previous state. This is obviously bad for users who create
 * incremental state in the datastore and actually read what they write in subsequent transactions. Using
 * a TransactionChain instead of a broker solves this particular problem, and leads to expected behavior: t2 will always
 * see the data written in t1 present.
 */
@Beta
public interface TransactionChain extends Registration, TransactionFactory {
    /**
     * Create a new read only transaction which will continue the chain.
     *
     * <p>
     * The previous write transaction has to be either SUBMITTED ({@link WriteTransaction#commit commit} was invoked)
     * or CANCELLED ({@link #close close} was invoked).
     *
     * <p>
     * The returned read-only transaction presents an isolated view of the data if the previous write transaction was
     * successful - in other words, this read-only transaction will see the state changes made by the previous write
     * transaction in the chain. However, state which was introduced by other transactions outside this transaction
     * chain after creation of the previous transaction is not visible.
     *
     * @return New transaction in the chain.
     * @throws IllegalStateException if the previous transaction was not SUBMITTED or CANCELLED.
     * @throws TransactionChainClosedException if the chain has been closed.
     */
    @Override
    ReadTransaction newReadOnlyTransaction();

    /**
     * Create a new write-only transaction which will continue the chain.
     *
     * <p>
     * The previous write transaction has to be either SUBMITTED ({@link WriteTransaction#commit commit} was invoked)
     * or CANCELLED ({@link #close close} was invoked)
     *
     * <p>
     * The returned write-only transaction presents an isolated view of the data if the previous write transaction was
     * successful - in other words, this write-only transaction will see the state changes made by the previous write
     * transaction in the chain. However, state which was introduced by other transactions outside this transaction
     * chain after creation of the previous transaction is not visible
     *
     * <p>
     * Committing this write-only transaction using {@link WriteTransaction#commit commit} will commit the state
     * changes in this transaction to be visible to any subsequent transaction in this chain and also to any transaction
     * outside this chain.
     *
     * @return New transaction in the chain.
     * @throws IllegalStateException if the previous transaction was not SUBMITTED or CANCELLED.
     * @throws TransactionChainClosedException if the chain has been closed.
     */
    @Override
    WriteTransaction newWriteOnlyTransaction();
}

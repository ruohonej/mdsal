/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.data.tree;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.MoreExecutors;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeProducerException;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.test.BindingBrokerTestFactory;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.test.BindingTestContext;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCursorAwareTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducer;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducerBusyException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducerException;

public class BindingDOMDataTreeProducerAdapterTest {

    private BindingDOMDataTreeProducerAdapter bindingDOMDataTreeProducerAdapter;
    private BindingToNormalizedNodeCodec codec;

    @Mock
    private DOMDataTreeProducer delegate;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        final BindingBrokerTestFactory testFactory = new BindingBrokerTestFactory();
        testFactory.setExecutor(MoreExecutors.newDirectExecutorService());
        final BindingTestContext testContext = testFactory.getTestContext();
        testContext.start();
        codec = testContext.getCodec();
        bindingDOMDataTreeProducerAdapter =
                (BindingDOMDataTreeProducerAdapter) BindingDOMDataTreeProducerAdapter.create(delegate, codec);
    }

    @Test
    public void createTransactionTest() throws Exception {
        doReturn(mock(DOMDataTreeCursorAwareTransaction.class)).when(delegate).createTransaction(true);
        assertNotNull(bindingDOMDataTreeProducerAdapter.createTransaction(true));
        verify(delegate).createTransaction(true);
    }

    @Test
    public void createTest() throws Exception {
        assertNotNull(BindingDOMDataTreeProducerAdapter.create(delegate, codec));
    }

    @Test
    public void createProducerTest() throws Exception {
        doReturn(mock(DOMDataTreeProducer.class)).when(delegate).createProducer(any());
        assertNotNull(bindingDOMDataTreeProducerAdapter.createProducer(ImmutableSet.of()));
        verify(delegate).createProducer(any());
    }

    @Test
    public void closeTest() throws Exception {
        reset(delegate);
        bindingDOMDataTreeProducerAdapter.close();
        verify(delegate).close();
    }

    @Test(expected = DataTreeProducerException.class)
    public void closeTestWithException1() throws Exception {
        doThrow(new DOMDataTreeProducerBusyException("test")).when(delegate).close();
        bindingDOMDataTreeProducerAdapter.close();
    }

    @Test(expected = DataTreeProducerException.class)
    public void closeTestWithException2() throws Exception {
        doThrow(new DOMDataTreeProducerException("test")).when(delegate).close();
        bindingDOMDataTreeProducerAdapter.close();
    }
}
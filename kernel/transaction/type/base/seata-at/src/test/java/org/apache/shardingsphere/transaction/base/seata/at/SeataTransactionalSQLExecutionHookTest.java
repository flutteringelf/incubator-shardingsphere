/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.transaction.base.seata.at;

import io.seata.core.context.RootContext;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class SeataTransactionalSQLExecutionHookTest {
    
    private final SeataTransactionalSQLExecutionHook executionHook = new SeataTransactionalSQLExecutionHook();
    
    @Mock
    private DataSourceMetaData dataSourceMetaData;
    
    @After
    public void tearDown() {
        RootContext.unbind();
    }
    
    @Test
    public void assertTrunkThreadExecute() {
        RootContext.bind("xid");
        executionHook.start("ds", "SELECT 1", Collections.emptyList(), dataSourceMetaData, true);
        assertThat(SeataXIDContext.get(), is(RootContext.getXID()));
        executionHook.finishSuccess();
        assertTrue(RootContext.inGlobalTransaction());
    }
    
    @Test
    public void assertChildThreadExecute() {
        executionHook.start("ds", "SELECT 1", Collections.emptyList(), dataSourceMetaData, false);
        assertTrue(RootContext.inGlobalTransaction());
        executionHook.finishSuccess();
        assertFalse(RootContext.inGlobalTransaction());
    }
    
    @Test
    public void assertChildThreadExecuteFailed() {
        executionHook.start("ds", "SELECT 1", Collections.emptyList(), dataSourceMetaData, false);
        assertTrue(RootContext.inGlobalTransaction());
        executionHook.finishFailure(new RuntimeException(""));
        assertFalse(RootContext.inGlobalTransaction());
    }
}

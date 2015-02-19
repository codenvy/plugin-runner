/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.ext.runner.client.tabs.history;

import com.codenvy.ide.ext.runner.client.tabs.history.runner.RunnerWidget;
import com.google.gwtmockito.GwtMockitoTestRunner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;

/**
 * @author Andrienko Alexander
 */
@RunWith(GwtMockitoTestRunner.class)
public class HistoryViewImplTest {
    @Mock
    private RunnerWidget runnerWidget;

    @InjectMocks
    private HistoryViewImpl historyView;

    @Test
    public void shouldAddRunner() {
        historyView.addRunner(runnerWidget);

        verify(historyView.runnersPanel).add(runnerWidget);
    }
}
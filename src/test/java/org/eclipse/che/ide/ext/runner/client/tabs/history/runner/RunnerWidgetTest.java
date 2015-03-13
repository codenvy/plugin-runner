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
package org.eclipse.che.ide.ext.runner.client.tabs.history.runner;

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.ide.ext.runner.client.RunnerResources;
import org.eclipse.che.ide.ext.runner.client.models.Runner;
import org.eclipse.che.ide.ext.runner.client.selection.SelectionManager;
import org.eclipse.che.ide.ext.runner.client.tabs.common.item.ItemWidget;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.vectomatic.dom.svg.ui.SVGImage;
import org.vectomatic.dom.svg.ui.SVGResource;

import static org.eclipse.che.ide.ext.runner.client.tabs.properties.panel.common.RAM.MB_512;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Andrienko Alexander
 */
@RunWith(GwtMockitoTestRunner.class)
public class RunnerWidgetTest {
    private static final String TEXT = "text";

    @Mock
    private ItemWidget       itemWidget;
    @Mock
    private RunnerResources  resources;
    @Mock
    private SelectionManager selectionManager;

    @Mock
    private Runner                    runner;
    @Mock
    private RunnerResources.RunnerCss css;

    private RunnerWidget runnerWidget;

    @Before
    public void setUp() {
        SVGResource svgResource = mock(SVGResource.class, RETURNS_DEEP_STUBS);

        when(resources.runnerInProgress()).thenReturn(svgResource);
        when(resources.runnerInQueue()).thenReturn(svgResource);
        when(resources.runnerFailed()).thenReturn(svgResource);
        when(resources.runnerTimeout()).thenReturn(svgResource);
        when(resources.runnerDone()).thenReturn(svgResource);
        when(resources.runnerDone()).thenReturn(svgResource);

        runnerWidget = new RunnerWidget(itemWidget, resources, selectionManager);

        when(resources.runnerCss()).thenReturn(css);
        when(runner.getTitle()).thenReturn(TEXT);
        when(runner.getRAM()).thenReturn(MB_512.getValue());
        when(runner.getCreationTime()).thenReturn(TEXT);
    }

    @Test
    public void shouldVerifyConstructor() {
        ArgumentCaptor<ItemWidget.ActionDelegate> actionDelegateCaptor =
                ArgumentCaptor.forClass(ItemWidget.ActionDelegate.class);

        verify(resources).runnerInProgress();
        verify(resources).runnerInQueue();
        verify(resources).runnerFailed();
        verify(resources).runnerTimeout();
        verify(resources, times(2)).runnerDone();

        verify(itemWidget).setDelegate(actionDelegateCaptor.capture());
        ItemWidget.ActionDelegate actionDelegate = actionDelegateCaptor.getValue();
        actionDelegate.onWidgetClicked();

        verify(selectionManager).setRunner(any(Runner.class));
    }

    @Test
    public void shouldSelect() {
        runnerWidget.select();

        verify(itemWidget).select();
    }

    @Test
    public void shouldUnSelect() {
        runnerWidget.unSelect();

        verify(itemWidget).unSelect();
    }

    @Test
    public void shouldUpdateRunnerWithStatusInProgress() {
        when(runner.getStatus()).thenReturn(Runner.Status.IN_PROGRESS);
        when(css.blueColor()).thenReturn(TEXT);

        runnerWidget.update(runner);

        verify(css).blueColor();

        shouldUpdateItemWidgetParameter();
    }

    @Test
    public void shouldUpdateRunnerWithStatusQueue() {
        when(runner.getStatus()).thenReturn(Runner.Status.IN_QUEUE);
        when(css.blueColor()).thenReturn(TEXT);

        runnerWidget.update(runner);

        verify(css).yellowColor();

        shouldUpdateItemWidgetParameter();
    }

    @Test
    public void shouldUpdateRunnerWithStatusFailed() {
        when(runner.getStatus()).thenReturn(Runner.Status.FAILED);
        when(css.blueColor()).thenReturn(TEXT);

        runnerWidget.update(runner);

        verify(css).redColor();

        shouldUpdateItemWidgetParameter();
    }

    @Test
    public void shouldUpdateRunnerWithStatusTimeOut() {
        when(runner.getStatus()).thenReturn(Runner.Status.TIMEOUT);
        when(css.blueColor()).thenReturn(TEXT);

        runnerWidget.update(runner);

        verify(css).whiteColor();

        shouldUpdateItemWidgetParameter();
    }

    @Test
    public void shouldUpdateRunnerWithStatusStopped() {
        when(runner.getStatus()).thenReturn(Runner.Status.STOPPED);
        when(css.blueColor()).thenReturn(TEXT);

        runnerWidget.update(runner);

        verify(css).redColor();

        shouldUpdateItemWidgetParameter();
    }

    @Test
    public void shouldUpdateRunnerWithStatusDone() {
        when(runner.getStatus()).thenReturn(Runner.Status.DONE);
        when(css.blueColor()).thenReturn(TEXT);

        runnerWidget.update(runner);

        verify(css).greenColor();

        shouldUpdateItemWidgetParameter();
    }

    @Test
    public void shouldAsWidget() {
        runnerWidget.asWidget();

        verify(itemWidget).asWidget();
    }

    private void shouldUpdateItemWidgetParameter() {
        verify(itemWidget).setImage(any(SVGImage.class));
        verify(runner).getTitle();
        verify(itemWidget).setName(TEXT);
        verify(runner).getRAM();
        verify(itemWidget).setDescription(MB_512.toString());
        verify(runner).getCreationTime();
        verify(itemWidget).setStartTime(TEXT);
    }
}
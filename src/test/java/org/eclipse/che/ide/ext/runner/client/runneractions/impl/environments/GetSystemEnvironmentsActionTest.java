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
package org.eclipse.che.ide.ext.runner.client.runneractions.impl.environments;

import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.api.project.shared.dto.RunnerEnvironmentLeaf;
import org.eclipse.che.api.project.shared.dto.RunnerEnvironmentTree;
import org.eclipse.che.api.runner.gwt.client.RunnerServiceClient;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.ext.runner.client.RunnerLocalizationConstant;
import org.eclipse.che.ide.ext.runner.client.actions.ChooseRunnerAction;
import org.eclipse.che.ide.ext.runner.client.callbacks.AsyncCallbackBuilder;
import org.eclipse.che.ide.ext.runner.client.callbacks.FailureCallback;
import org.eclipse.che.ide.ext.runner.client.callbacks.SuccessCallback;
import org.eclipse.che.ide.ext.runner.client.models.Environment;
import org.eclipse.che.ide.ext.runner.client.tabs.templates.TemplatesContainer;
import org.eclipse.che.ide.ext.runner.client.util.GetEnvironmentsUtil;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import com.google.inject.Provider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.eclipse.che.ide.ext.runner.client.tabs.properties.panel.common.Scope.SYSTEM;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Alexander Andrienko
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class GetSystemEnvironmentsActionTest {
    private static final String MESSAGE = "some message";

    //constructor variables
    @Mock
    private Provider<TemplatesContainer>                          templatesContainerProvider;
    @Mock
    private RunnerServiceClient                                   runnerService;
    @Mock
    private NotificationManager                                   notificationManager;
    @Mock
    private Provider<AsyncCallbackBuilder<RunnerEnvironmentTree>> callbackBuilderProvider;
    @Mock
    private RunnerLocalizationConstant                            locale;
    @Mock
    private GetEnvironmentsUtil                                   environmentUtil;
    @Mock
    private ChooseRunnerAction                                    chooseRunnerAction;

    @Mock
    private Throwable reason;

    //callbacks for server
    @Mock
    private AsyncCallbackBuilder<RunnerEnvironmentTree>            asyncCallbackBuilder;
    @Mock
    private AsyncRequestCallback<RunnerEnvironmentTree>            asyncRequestCallback;
    //project variables
    @Mock
    private ProjectDescriptor                                      projectDescriptor;
    //captors
    @Captor
    private ArgumentCaptor<FailureCallback>                        failedCallBackCaptor;
    @Captor
    private ArgumentCaptor<SuccessCallback<RunnerEnvironmentTree>> successCallBackCaptor;
    //run variables
    @Mock
    private List<RunnerEnvironmentLeaf>                            leaves;
    @Mock
    private List<Environment>                                      environments;
    @Mock
    private TemplatesContainer                                     templatesContainer;
    @Mock
    private RunnerEnvironmentTree                                  tree;
    @Mock
    private AppContext                                             appContext;
    @Mock
    private CurrentProject                                         currentProject;

    @InjectMocks
    private GetSystemEnvironmentsAction action;

    @Before
    public void setUp() {
        action = new GetSystemEnvironmentsAction(runnerService,
                                                 notificationManager,
                                                 callbackBuilderProvider,
                                                 locale,
                                                 environmentUtil,
                                                 chooseRunnerAction,
                                                 appContext,
                                                 templatesContainerProvider);
        //preparing callbacks for server
        when(templatesContainerProvider.get()).thenReturn(templatesContainer);
        when(environmentUtil.getAllEnvironments(tree)).thenReturn(leaves);
        when(environmentUtil.getEnvironmentsByProjectType(tree, MESSAGE, SYSTEM)).thenReturn(environments);
        when(environmentUtil.getEnvironmentsFromNodes(leaves, SYSTEM)).thenReturn(environments);
        when(callbackBuilderProvider.get()).thenReturn(asyncCallbackBuilder).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.unmarshaller(RunnerEnvironmentTree.class)).thenReturn(asyncCallbackBuilder)
                                                                            .thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.failure(any(FailureCallback.class))).thenReturn(asyncCallbackBuilder).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.success(Matchers.<SuccessCallback<RunnerEnvironmentTree>>anyObject()))
                .thenReturn(asyncCallbackBuilder).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.build()).thenReturn(asyncRequestCallback).thenReturn(asyncRequestCallback);

        when(locale.customRunnerGetEnvironmentFailed()).thenReturn(MESSAGE);
    }

    @Test
    public void shouldSuccessPerformWhenEnvironmentTreeIsNull() {
        action.perform();

        verify(asyncCallbackBuilder).success(successCallBackCaptor.capture());
        SuccessCallback<RunnerEnvironmentTree> successCallback = successCallBackCaptor.getValue();
        successCallback.onSuccess(null);

        verify(runnerService).getRunners(asyncRequestCallback);
    }

    @Test
    public void shouldFailurePerformWhenEnvironmentTreeIsNull() {
        action.perform();

        verify(asyncCallbackBuilder).failure(failedCallBackCaptor.capture());
        FailureCallback failureCallback = failedCallBackCaptor.getValue();
        failureCallback.onFailure(reason);

        verify(notificationManager).showError(MESSAGE);
        verify(runnerService).getRunners(asyncRequestCallback);
    }

    @Test
    public void getSystemEnvironmentsActionShouldBePerformedWhenEnvironmentTreeIsNotNull() throws Exception {
        ProjectDescriptor descriptor = mock(ProjectDescriptor.class);
        when(appContext.getCurrentProject()).thenReturn(currentProject);
        when(currentProject.getProjectDescription()).thenReturn(descriptor);
        when(descriptor.getType()).thenReturn(MESSAGE);
        //launch perform first time for set environmentTree not null
        action.perform();

        verify(asyncCallbackBuilder).success(successCallBackCaptor.capture());
        SuccessCallback<RunnerEnvironmentTree> successCallback = successCallBackCaptor.getValue();
        successCallback.onSuccess(tree);

        reset(runnerService);

        action.perform();

        verify(runnerService, never()).getRunners(asyncRequestCallback);
        verify(appContext, times(2)).getCurrentProject();
        verify(environmentUtil, times(2)).getEnvironmentsByProjectType(tree, MESSAGE, SYSTEM);
        verify(templatesContainerProvider, times(2)).get();
        verify(environmentUtil, times(2)).getRunnerCategoryByProjectType(tree, MESSAGE);
        verify(chooseRunnerAction, times(2)).addSystemRunners(environments);
    }

    @Test
    public void shouldFailurePerformWhenEnvironmentTreeIsNotNull() {
        //launch perform first time for set environmentTree not null
        action.perform();

        verify(asyncCallbackBuilder).success(successCallBackCaptor.capture());
        SuccessCallback<RunnerEnvironmentTree> successCallback = successCallBackCaptor.getValue();
        successCallback.onSuccess(tree);

        action.perform();

        verify(asyncCallbackBuilder, times(2)).failure(failedCallBackCaptor.capture());
        FailureCallback failureCallback = failedCallBackCaptor.getValue();
        failureCallback.onFailure(reason);

        verify(notificationManager).showError(MESSAGE);
    }

}
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
package com.codenvy.ide.ext.runner.client.runneractions.impl;

import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.api.project.shared.dto.RunnerConfiguration;
import com.codenvy.api.project.shared.dto.RunnersDescriptor;
import com.codenvy.api.runner.dto.ResourcesDescriptor;
import com.codenvy.api.runner.gwt.client.RunnerServiceClient;
import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.app.CurrentProject;
import com.codenvy.ide.ext.runner.client.RunnerLocalizationConstant;
import com.codenvy.ide.ext.runner.client.callbacks.AsyncCallbackBuilder;
import com.codenvy.ide.ext.runner.client.callbacks.FailureCallback;
import com.codenvy.ide.ext.runner.client.callbacks.SuccessCallback;
import com.codenvy.ide.ext.runner.client.inject.factories.RunnerActionFactory;
import com.codenvy.ide.ext.runner.client.models.Runner;
import com.codenvy.ide.ext.runner.client.util.RunnerUtil;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.ui.dialogs.CancelCallback;
import com.codenvy.ide.ui.dialogs.ConfirmCallback;
import com.codenvy.ide.ui.dialogs.DialogFactory;
import com.codenvy.ide.ui.dialogs.confirm.ConfirmDialog;
import com.codenvy.ide.ui.dialogs.message.MessageDialog;
import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.inject.Provider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;

import java.util.HashMap;

import static com.codenvy.ide.ext.runner.client.models.Runner.Status.FAILED;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


/**
 * @author Alexander Andrienko
 */
//Todo CheckRamAndRunAction class is too difficult. This class should improve.
@RunWith(GwtMockitoTestRunner.class)
public class CheckRamAndRunActionTest {

    //text for warning or error messages
    private static final String RESOURCE                                = "some resources";
    private static final String ID_ENVIRONMENT                          = "testEnvironmentId";
    private static final String AVAILABLE_MEMORY_LESS_THEN_REQUIRED     = "not enough required memory";
    private static final String MESSAGES_TOTAL_LESS_REQUIRED_MEMORY     = "messages total less required memory";
    private static final String TOTAL_MEMORY_LESS_OVERRIDE_MEMORY       = "Total Memory Less than Override Memory";
    private static final String MESSAGES_AVAILABLE_LESS_OVERRIDE_MEMORY = "Available memory less override memory";

    //variables for constructor
    @Mock
    private RunnerServiceClient                                 service;
    @Mock
    private AppContext                                          appContext;
    @Mock
    private DialogFactory                                       dialogFactory;
    @Mock
    private Provider<AsyncCallbackBuilder<ResourcesDescriptor>> callbackBuilderProvider;
    @Mock
    private RunnerLocalizationConstant                          constant;
    @Mock
    private RunnerUtil                                          runnerUtil;
    @Mock
    private RunnerActionFactory                                 actionFactory;

    //callBacks for server
    @Mock
    private AsyncCallbackBuilder<ResourcesDescriptor> asyncCallbackBuilder;
    @Mock
    private AsyncRequestCallback<ResourcesDescriptor> asyncRequestCallback;
    //descriptors
    @Mock
    private ResourcesDescriptor                       resourcesDescriptor;
    @Mock
    private ProjectDescriptor                         projectDescriptor;
    @Mock
    private RunnersDescriptor                         runners;
    //configurations
    @Mock
    private RunnerConfiguration                       runnerConfiguration;
    @Mock
    private HashMap<String, RunnerConfiguration>      hashMap;
    //dialogs
    @Mock
    private ConfirmDialog                             confirmDialog;
    @Mock
    private MessageDialog                             messageDialog;
    //run processes
    @Mock
    private RunAction                                 runAction;
    @Mock
    private Runner                                    runner;
    //another mocks
    @Mock
    private CurrentProject                            project;
    @Mock
    private Throwable                                 reason;

    @Captor
    private ArgumentCaptor<FailureCallback>                      failedCallBackCaptor;
    @Captor
    private ArgumentCaptor<SuccessCallback<ResourcesDescriptor>> successCallBackCaptor;
    @Captor
    private ArgumentCaptor<ConfirmCallback>                      confirmCallbackArgumentCaptor;

    private CheckRamAndRunAction checkRamAndRunAction;

    @Before
    public void setUp() {
        when(actionFactory.createRun()).thenReturn(runAction);
        checkRamAndRunAction =
                new CheckRamAndRunAction(service, appContext, dialogFactory, callbackBuilderProvider, constant, runnerUtil, actionFactory);
    }

    @Test
    public void shouldNotPerformWhenCurrentProjectIsNull() {
        reset(runnerUtil, constant, service, runner, runAction);
        when(appContext.getCurrentProject()).thenReturn(null);

        checkRamAndRunAction.perform(runner);

        verify(appContext).getCurrentProject();
        verifyNoMoreInteractions(runnerUtil, constant, service, runner, runAction);
    }

    @Test
    public void shouldFailedPerform() {
        when(appContext.getCurrentProject()).thenReturn(project);
        when(constant.getResourcesFailed()).thenReturn(RESOURCE);
        //preparing mock for creation SuccessCallback
        when(callbackBuilderProvider.get()).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.unmarshaller(ResourcesDescriptor.class)).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.failure(any(FailureCallback.class))).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.success(Matchers.<SuccessCallback<ResourcesDescriptor>>anyObject())).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.build()).thenReturn(asyncRequestCallback);

        checkRamAndRunAction.perform(runner);

        verify(appContext).getCurrentProject();
        verify(service).getResources(asyncRequestCallback);

        verify(callbackBuilderProvider).get();
        verify(asyncCallbackBuilder).unmarshaller(ResourcesDescriptor.class);

        verify(asyncCallbackBuilder).failure(failedCallBackCaptor.capture());

        FailureCallback failureCallback = failedCallBackCaptor.getValue();
        failureCallback.onFailure(reason);

        verify(runnerUtil).showError(runner, RESOURCE, reason);
    }

    @Test
    public void shouldPerformWithDefaultRunnerConfiguration() {
        String defaultRunnerConfig = "defaultRunnerConfigTesT";
        when(appContext.getCurrentProject()).thenReturn(project);
        when(constant.getResourcesFailed()).thenReturn(RESOURCE);
        //preparing mock for creation SuccessCallback
        when(callbackBuilderProvider.get()).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.unmarshaller(ResourcesDescriptor.class)).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.failure(any(FailureCallback.class))).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.success(Matchers.<SuccessCallback<ResourcesDescriptor>>anyObject())).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.build()).thenReturn(asyncRequestCallback);
        //preparing runner and run configuration
        when(project.getProjectDescription()).thenReturn(projectDescriptor);
        when(projectDescriptor.getRunners()).thenReturn(runners);
        when(runner.getEnvironmentId()).thenReturn(ID_ENVIRONMENT);
        when(runners.getConfigs()).thenReturn(hashMap);
        when(hashMap.get(ID_ENVIRONMENT)).thenReturn(null);
        when(runners.getDefault()).thenReturn(defaultRunnerConfig);
        when(hashMap.get(defaultRunnerConfig)).thenReturn(runnerConfiguration);

        //total memory
        when(resourcesDescriptor.getTotalMemory()).thenReturn("1024");
        when(resourcesDescriptor.getUsedMemory()).thenReturn("256");
        //overrideMemory
        when(runner.getRAM()).thenReturn(256);
        //requiredMemory
        when(runnerConfiguration.getRam()).thenReturn(256);

        checkRamAndRunAction.perform(runner);

        verify(service).getResources(asyncRequestCallback);
        verify(callbackBuilderProvider).get();
        verify(asyncCallbackBuilder).unmarshaller(ResourcesDescriptor.class);

        verify(asyncCallbackBuilder).success(successCallBackCaptor.capture());

        SuccessCallback<ResourcesDescriptor> successCallback = successCallBackCaptor.getValue();
        successCallback.onSuccess(resourcesDescriptor);

        verify(resourcesDescriptor).getTotalMemory();
        verify(resourcesDescriptor).getUsedMemory();
        verify(project).getProjectDescription();
        verify(runners, times(2)).getConfigs();
        verify(hashMap).get(ID_ENVIRONMENT);
        verify(hashMap).get(defaultRunnerConfig);
        verify(runners).getDefault();

        verify(runner).getRAM();
        verify(runnerConfiguration).getRam();
        //success run
        verify(runner).setRAM(256);

        verify(runAction).perform(runner);
    }

    @Test
    public void shouldPerformWhenEnoughMemory() {
        when(appContext.getCurrentProject()).thenReturn(project);
        when(constant.getResourcesFailed()).thenReturn(RESOURCE);
        //preparing mock for creation SuccessCallback
        when(callbackBuilderProvider.get()).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.unmarshaller(ResourcesDescriptor.class)).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.failure(any(FailureCallback.class))).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.success(Matchers.<SuccessCallback<ResourcesDescriptor>>anyObject())).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.build()).thenReturn(asyncRequestCallback);
        //preparing runner and run configuration
        when(project.getProjectDescription()).thenReturn(projectDescriptor);
        when(projectDescriptor.getRunners()).thenReturn(runners);
        when(runner.getEnvironmentId()).thenReturn(ID_ENVIRONMENT);
        when(runners.getConfigs()).thenReturn(hashMap);
        when(hashMap.get(ID_ENVIRONMENT)).thenReturn(runnerConfiguration);

        //total memory
        when(resourcesDescriptor.getTotalMemory()).thenReturn("1024");
        when(resourcesDescriptor.getUsedMemory()).thenReturn("256");
        //overrideMemory
        when(runner.getRAM()).thenReturn(256);
        //requiredMemory
        when(runnerConfiguration.getRam()).thenReturn(256);

        checkRamAndRunAction.perform(runner);

        verify(service).getResources(asyncRequestCallback);
        verify(callbackBuilderProvider).get();
        verify(asyncCallbackBuilder).unmarshaller(ResourcesDescriptor.class);

        verify(asyncCallbackBuilder).success(successCallBackCaptor.capture());

        SuccessCallback<ResourcesDescriptor> successCallback = successCallBackCaptor.getValue();
        successCallback.onSuccess(resourcesDescriptor);

        verify(resourcesDescriptor).getTotalMemory();
        verify(resourcesDescriptor).getUsedMemory();
        verify(project).getProjectDescription();
        verify(runners).getConfigs();
        verify(hashMap).get(ID_ENVIRONMENT);
        verify(runners, never()).getDefault();

        verify(runner).getRAM();
        verify(runnerConfiguration).getRam();
        //success run
        verify(runner).setRAM(256);

        verify(runAction).perform(runner);
    }

    @Test
    public void shouldPerformWhenOverrideMemoryIsZero() {
        when(appContext.getCurrentProject()).thenReturn(project);
        when(constant.getResourcesFailed()).thenReturn(RESOURCE);
        //preparing mock for creation SuccessCallback
        when(callbackBuilderProvider.get()).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.unmarshaller(ResourcesDescriptor.class)).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.failure(any(FailureCallback.class))).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.success(Matchers.<SuccessCallback<ResourcesDescriptor>>anyObject())).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.build()).thenReturn(asyncRequestCallback);
        //preparing runner and run configuration
        when(project.getProjectDescription()).thenReturn(projectDescriptor);
        when(projectDescriptor.getRunners()).thenReturn(runners);
        when(runner.getEnvironmentId()).thenReturn(ID_ENVIRONMENT);
        when(runners.getConfigs()).thenReturn(hashMap);
        when(hashMap.get(ID_ENVIRONMENT)).thenReturn(runnerConfiguration);

        //total memory
        when(resourcesDescriptor.getTotalMemory()).thenReturn("1024");
        //used memory
        when(resourcesDescriptor.getUsedMemory()).thenReturn("256");
        //overrideMemory
        when(runner.getRAM()).thenReturn(0);
        //requiredMemory
        when(runnerConfiguration.getRam()).thenReturn(384);

        checkRamAndRunAction.perform(runner);

        verify(service).getResources(asyncRequestCallback);

        verify(asyncCallbackBuilder).success(successCallBackCaptor.capture());

        SuccessCallback<ResourcesDescriptor> successCallback = successCallBackCaptor.getValue();
        successCallback.onSuccess(resourcesDescriptor);

        verify(resourcesDescriptor).getTotalMemory();
        verify(resourcesDescriptor).getUsedMemory();
        verify(project).getProjectDescription();
        verify(runners).getConfigs();
        verify(hashMap).get(ID_ENVIRONMENT);
        verify(runners, never()).getDefault();

        verify(runner).getRAM();
        verify(runnerConfiguration).getRam();
        //success run
        verify(runner).setRAM(384);

        verify(runAction).perform(runner);
    }

    @Test
    public void shouldPerformWhenOverrideAndRequiredMemoriesAreZero() {
        when(appContext.getCurrentProject()).thenReturn(project);
        when(constant.getResourcesFailed()).thenReturn(RESOURCE);
        //preparing mock for creation SuccessCallback
        when(callbackBuilderProvider.get()).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.unmarshaller(ResourcesDescriptor.class)).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.failure(any(FailureCallback.class))).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.success(Matchers.<SuccessCallback<ResourcesDescriptor>>anyObject())).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.build()).thenReturn(asyncRequestCallback);
        //preparing runner and run configuration
        when(project.getProjectDescription()).thenReturn(projectDescriptor);
        when(projectDescriptor.getRunners()).thenReturn(runners);
        when(runner.getEnvironmentId()).thenReturn(ID_ENVIRONMENT);
        when(runners.getConfigs()).thenReturn(hashMap);
        when(hashMap.get(ID_ENVIRONMENT)).thenReturn(runnerConfiguration);

        //total memory
        when(resourcesDescriptor.getTotalMemory()).thenReturn("1024");
        //used memory
        when(resourcesDescriptor.getUsedMemory()).thenReturn("256");
        //overrideMemory
        when(runner.getRAM()).thenReturn(0);
        //requiredMemory
        when(runnerConfiguration.getRam()).thenReturn(0);

        checkRamAndRunAction.perform(runner);

        verify(service).getResources(asyncRequestCallback);

        verify(asyncCallbackBuilder).success(successCallBackCaptor.capture());

        SuccessCallback<ResourcesDescriptor> successCallback = successCallBackCaptor.getValue();
        successCallback.onSuccess(resourcesDescriptor);

        verify(resourcesDescriptor).getTotalMemory();
        verify(resourcesDescriptor).getUsedMemory();
        verify(project).getProjectDescription();
        verify(runners).getConfigs();
        verify(hashMap).get(ID_ENVIRONMENT);
        verify(runners, never()).getDefault();

        verify(runner).getRAM();
        verify(runnerConfiguration).getRam();

        verify(runAction).perform(runner);
    }

    @Test
    public void shouldShowMessageTotalLessRequiredMemory() {
        when(appContext.getCurrentProject()).thenReturn(project);
        when(constant.getResourcesFailed()).thenReturn(RESOURCE);
        //preparing mock for creation SuccessCallback
        when(callbackBuilderProvider.get()).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.unmarshaller(ResourcesDescriptor.class)).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.failure(any(FailureCallback.class))).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.success(Matchers.<SuccessCallback<ResourcesDescriptor>>anyObject())).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.build()).thenReturn(asyncRequestCallback);
        //preparing runner and run configuration
        when(project.getProjectDescription()).thenReturn(projectDescriptor);
        when(projectDescriptor.getRunners()).thenReturn(runners);
        when(runner.getEnvironmentId()).thenReturn(ID_ENVIRONMENT);
        when(runners.getConfigs()).thenReturn(hashMap);
        when(hashMap.get(ID_ENVIRONMENT)).thenReturn(runnerConfiguration);
        when(constant.messagesTotalLessRequiredMemory(512, 1024)).thenReturn(MESSAGES_TOTAL_LESS_REQUIRED_MEMORY);

        //total memory
        when(resourcesDescriptor.getTotalMemory()).thenReturn("512");
        //used memory
        when(resourcesDescriptor.getUsedMemory()).thenReturn("256");
        //overrideMemory
        when(runner.getRAM()).thenReturn(512);
        //requiredMemory
        when(runnerConfiguration.getRam()).thenReturn(1024);

        checkRamAndRunAction.perform(runner);

        verify(service).getResources(asyncRequestCallback);

        verify(asyncCallbackBuilder).success(successCallBackCaptor.capture());

        SuccessCallback<ResourcesDescriptor> successCallback = successCallBackCaptor.getValue();
        successCallback.onSuccess(resourcesDescriptor);

        verify(resourcesDescriptor).getTotalMemory();
        verify(resourcesDescriptor).getUsedMemory();
        verify(project).getProjectDescription();
        verify(runners).getConfigs();
        verify(hashMap).get(ID_ENVIRONMENT);
        verify(runners, never()).getDefault();

        verify(runner).getRAM();
        verify(runnerConfiguration).getRam();

        verify(constant).messagesTotalLessRequiredMemory(512, 1024);
        verify(runnerUtil).showWarning(MESSAGES_TOTAL_LESS_REQUIRED_MEMORY);

        verify(runner).setStatus(FAILED);
    }

    @Test
    public void shouldShowMessagesAvailableLessRequiredMemory() {
        when(appContext.getCurrentProject()).thenReturn(project);
        when(constant.getResourcesFailed()).thenReturn(RESOURCE);
        //preparing mock for creation SuccessCallback
        when(callbackBuilderProvider.get()).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.unmarshaller(ResourcesDescriptor.class)).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.failure(any(FailureCallback.class))).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.success(Matchers.<SuccessCallback<ResourcesDescriptor>>anyObject())).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.build()).thenReturn(asyncRequestCallback);
        //preparing runner and run configuration
        when(project.getProjectDescription()).thenReturn(projectDescriptor);
        when(projectDescriptor.getRunners()).thenReturn(runners);
        when(runner.getEnvironmentId()).thenReturn(ID_ENVIRONMENT);
        when(runners.getConfigs()).thenReturn(hashMap);
        when(hashMap.get(ID_ENVIRONMENT)).thenReturn(runnerConfiguration);
        when(constant.messagesAvailableLessRequiredMemory(512, 256, 384)).thenReturn(AVAILABLE_MEMORY_LESS_THEN_REQUIRED);

        //total memory
        when(resourcesDescriptor.getTotalMemory()).thenReturn("512");
        //used memory
        when(resourcesDescriptor.getUsedMemory()).thenReturn("256");
        //overrideMemory
        when(runner.getRAM()).thenReturn(512);
        //requiredMemory
        when(runnerConfiguration.getRam()).thenReturn(384);

        checkRamAndRunAction.perform(runner);

        verify(service).getResources(asyncRequestCallback);

        verify(asyncCallbackBuilder).success(successCallBackCaptor.capture());

        SuccessCallback<ResourcesDescriptor> successCallback =
                successCallBackCaptor.getValue();
        successCallback.onSuccess(resourcesDescriptor);

        verify(resourcesDescriptor).getTotalMemory();
        verify(resourcesDescriptor).getUsedMemory();
        verify(project).getProjectDescription();
        verify(runners).getConfigs();
        verify(hashMap).get(ID_ENVIRONMENT);
        verify(runners, never()).getDefault();

        verify(runner).getRAM();
        verify(runnerConfiguration).getRam();

        verify(constant).messagesAvailableLessRequiredMemory(512, 256, 384);
        verify(runnerUtil).showWarning(AVAILABLE_MEMORY_LESS_THEN_REQUIRED);

        verify(runner).setStatus(FAILED);
    }

    @Test
    public void shouldShowMessageTotalMemoryLessOverrideMemory() {
        when(appContext.getCurrentProject()).thenReturn(project);
        when(constant.getResourcesFailed()).thenReturn(RESOURCE);
        //preparing mock for creation SuccessCallback
        when(callbackBuilderProvider.get()).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.unmarshaller(ResourcesDescriptor.class)).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.failure(any(FailureCallback.class))).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.success(Matchers.<SuccessCallback<ResourcesDescriptor>>anyObject())).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.build()).thenReturn(asyncRequestCallback);
        //preparing runner and run configuration
        when(project.getProjectDescription()).thenReturn(projectDescriptor);
        when(projectDescriptor.getRunners()).thenReturn(runners);
        when(runner.getEnvironmentId()).thenReturn(ID_ENVIRONMENT);
        when(runners.getConfigs()).thenReturn(hashMap);
        when(hashMap.get(ID_ENVIRONMENT)).thenReturn(runnerConfiguration);
        when(constant.messagesTotalLessOverrideMemory(1024, 512)).thenReturn(TOTAL_MEMORY_LESS_OVERRIDE_MEMORY);

        //total memory
        when(resourcesDescriptor.getTotalMemory()).thenReturn("512");
        //used memory
        when(resourcesDescriptor.getUsedMemory()).thenReturn("256");
        //overrideMemory
        when(runner.getRAM()).thenReturn(1024);
        //requiredMemory
        when(runnerConfiguration.getRam()).thenReturn(128);

        checkRamAndRunAction.perform(runner);

        verify(service).getResources(asyncRequestCallback);

        verify(asyncCallbackBuilder).success(successCallBackCaptor.capture());

        SuccessCallback<ResourcesDescriptor> successCallback = successCallBackCaptor.getValue();
        successCallback.onSuccess(resourcesDescriptor);

        verify(resourcesDescriptor).getTotalMemory();
        verify(resourcesDescriptor).getUsedMemory();
        verify(project).getProjectDescription();
        verify(runners).getConfigs();
        verify(hashMap).get(ID_ENVIRONMENT);
        verify(runners, never()).getDefault();

        verify(runner).getRAM();
        verify(runnerConfiguration).getRam();

        verify(constant).messagesTotalLessOverrideMemory(1024, 512);
        verify(runnerUtil).showWarning(TOTAL_MEMORY_LESS_OVERRIDE_MEMORY);

        verify(runner).setStatus(FAILED);
    }

    @Test
    public void shouldShowMessageAvailableMemoryLessOverrideMemory() {
        when(appContext.getCurrentProject()).thenReturn(project);
        when(constant.getResourcesFailed()).thenReturn(RESOURCE);
        //preparing mock for creation SuccessCallback
        when(callbackBuilderProvider.get()).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.unmarshaller(ResourcesDescriptor.class)).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.failure(any(FailureCallback.class))).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.success(Matchers.<SuccessCallback<ResourcesDescriptor>>anyObject())).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.build()).thenReturn(asyncRequestCallback);
        //preparing runner and run configuration
        when(project.getProjectDescription()).thenReturn(projectDescriptor);
        when(projectDescriptor.getRunners()).thenReturn(runners);
        when(runner.getEnvironmentId()).thenReturn(ID_ENVIRONMENT);
        when(runners.getConfigs()).thenReturn(hashMap);
        when(hashMap.get(ID_ENVIRONMENT)).thenReturn(runnerConfiguration);
        when(constant.messagesAvailableLessOverrideMemory()).thenReturn(MESSAGES_AVAILABLE_LESS_OVERRIDE_MEMORY);

        //total memory
        when(resourcesDescriptor.getTotalMemory()).thenReturn("512");
        //used memory
        when(resourcesDescriptor.getUsedMemory()).thenReturn("256");
        //overrideMemory
        when(runner.getRAM()).thenReturn(384);
        //requiredMemory
        when(runnerConfiguration.getRam()).thenReturn(128);

        checkRamAndRunAction.perform(runner);

        verify(service).getResources(asyncRequestCallback);

        verify(asyncCallbackBuilder).success(successCallBackCaptor.capture());

        SuccessCallback<ResourcesDescriptor> successCallback =
                successCallBackCaptor.getValue();
        successCallback.onSuccess(resourcesDescriptor);

        verify(resourcesDescriptor).getTotalMemory();
        verify(resourcesDescriptor).getUsedMemory();
        verify(project).getProjectDescription();
        verify(runners).getConfigs();
        verify(hashMap).get(ID_ENVIRONMENT);
        verify(runners, never()).getDefault();

        verify(runner).getRAM();
        verify(runnerConfiguration).getRam();

        verify(constant).messagesAvailableLessOverrideMemory();
        verify(runnerUtil).showError(runner, MESSAGES_AVAILABLE_LESS_OVERRIDE_MEMORY, null);

        verify(runner).setStatus(FAILED);
    }

    @Test
    public void shouldShowMessageTotalMemoryLessRequiredMemory() {
        String titleWarning = "some warning";
        String messageOverrideMemory = "override memory message";
        String messagesOverrideLessRequiredMemory = "some message";

        when(appContext.getCurrentProject()).thenReturn(project);
        when(constant.getResourcesFailed()).thenReturn(RESOURCE);
        //preparing mock for creation SuccessCallback
        when(callbackBuilderProvider.get()).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.unmarshaller(ResourcesDescriptor.class)).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.failure(any(FailureCallback.class))).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.success(Matchers.<SuccessCallback<ResourcesDescriptor>>anyObject())).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.build()).thenReturn(asyncRequestCallback);
        //preparing runner and run configuration

        when(project.getProjectDescription()).thenReturn(projectDescriptor);
        when(projectDescriptor.getRunners()).thenReturn(runners);
        when(runner.getEnvironmentId()).thenReturn(ID_ENVIRONMENT);
        when(runners.getConfigs()).thenReturn(hashMap);
        when(hashMap.get(ID_ENVIRONMENT)).thenReturn(runnerConfiguration);

        when(constant.titlesWarning()).thenReturn(titleWarning);
        when(constant.messagesOverrideMemory()).thenReturn(messageOverrideMemory);
        when(constant.messagesOverrideLessRequiredMemory(128, 384)).
                                                                           thenReturn(messagesOverrideLessRequiredMemory);
        when(dialogFactory.createConfirmDialog(anyString(), anyString(), any(ConfirmCallback.class), isNull(CancelCallback.class)))
                .thenReturn(confirmDialog);
        when(dialogFactory.createMessageDialog(anyString(), anyString(), any(ConfirmCallback.class))).thenReturn(messageDialog);

        //total memory
        when(resourcesDescriptor.getTotalMemory()).thenReturn("512");
        //used memory
        when(resourcesDescriptor.getUsedMemory()).thenReturn("128");
        //overrideMemory
        when(runner.getRAM()).thenReturn(128);
        //requiredMemory
        when(runnerConfiguration.getRam()).thenReturn(384);

        checkRamAndRunAction.perform(runner);

        verify(service).getResources(asyncRequestCallback);

        verify(asyncCallbackBuilder).success(successCallBackCaptor.capture());

        SuccessCallback<ResourcesDescriptor> successCallback = successCallBackCaptor.getValue();
        successCallback.onSuccess(resourcesDescriptor);

        verify(resourcesDescriptor).getTotalMemory();
        verify(resourcesDescriptor).getUsedMemory();
        verify(project).getProjectDescription();
        verify(runners).getConfigs();
        verify(hashMap).get(ID_ENVIRONMENT);
        verify(runners, never()).getDefault();

        verify(runner).getRAM();
        verify(runnerConfiguration).getRam();

        verify(dialogFactory).createConfirmDialog(eq(titleWarning), eq(messageOverrideMemory), confirmCallbackArgumentCaptor.capture(),
                                                  isNull(CancelCallback.class));
        ConfirmCallback confirmCallback = confirmCallbackArgumentCaptor.getValue();
        confirmCallback.accepted();


        verify(constant).messagesOverrideMemory();

        verify(runner).setRAM(384);
        verify(runAction).perform(runner);

        verify(dialogFactory).createMessageDialog(eq(titleWarning),
                                                  eq(messagesOverrideLessRequiredMemory), confirmCallbackArgumentCaptor.capture());
        ConfirmCallback confirmCallback2 = confirmCallbackArgumentCaptor.getValue();
        confirmCallback2.accepted();

        verify(constant, times(2)).titlesWarning();
        verify(constant).messagesOverrideLessRequiredMemory(128, 384);

        verify(confirmDialog).show();
        verify(messageDialog).show();
    }

    @Test
    public void shouldShowMessageTotalMemoryLessRequiredMemoryButRunnerDescriptorIsNull() {
        String titleWarning = "some warning";
        String messageOverrideMemory = "override memory message";
        String messagesOverrideLessRequiredMemory = "some message";
        //preparing mock for creation SuccessCallback
        when(appContext.getCurrentProject()).thenReturn(project);
        when(constant.getResourcesFailed()).thenReturn(RESOURCE);

        when(callbackBuilderProvider.get()).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.unmarshaller(ResourcesDescriptor.class)).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.failure(any(FailureCallback.class))).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.success(Matchers.<SuccessCallback<ResourcesDescriptor>>anyObject())).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.build()).thenReturn(asyncRequestCallback);
        //preparing runner and run configuration
        when(project.getProjectDescription()).thenReturn(projectDescriptor);
        when(projectDescriptor.getRunners()).thenReturn(null);
        when(runner.getEnvironmentId()).thenReturn(ID_ENVIRONMENT);
        when(runners.getConfigs()).thenReturn(hashMap);
        when(hashMap.get(ID_ENVIRONMENT)).thenReturn(runnerConfiguration);
        //preparing constants
        when(constant.titlesWarning()).thenReturn(titleWarning);
        when(constant.messagesOverrideMemory()).thenReturn(messageOverrideMemory);
        when(constant.messagesOverrideLessRequiredMemory(128, 384)).thenReturn(messagesOverrideLessRequiredMemory);
        when(dialogFactory.createConfirmDialog(anyString(), anyString(), any(ConfirmCallback.class), isNull(CancelCallback.class)))
                .thenReturn(confirmDialog);
        when(dialogFactory.createMessageDialog(anyString(), anyString(), any(ConfirmCallback.class))).thenReturn(messageDialog);

        //total memory
        when(resourcesDescriptor.getTotalMemory()).thenReturn("512");
        //used memory
        when(resourcesDescriptor.getUsedMemory()).thenReturn("128");
        //overrideMemory
        when(runner.getRAM()).thenReturn(0);
        //required memory is 0

        checkRamAndRunAction.perform(runner);

        verify(service).getResources(asyncRequestCallback);

        verify(asyncCallbackBuilder).success(successCallBackCaptor.capture());

        SuccessCallback<ResourcesDescriptor> successCallback =
                successCallBackCaptor.getValue();
        successCallback.onSuccess(resourcesDescriptor);

        verify(resourcesDescriptor).getTotalMemory();
        verify(resourcesDescriptor).getUsedMemory();
        verify(project).getProjectDescription();
        verify(runners, never()).getDefault();

        verify(runner).getRAM();

        verify(runAction).perform(runner);
    }

    @Test
    public void shouldShowMessageTotalLessOverrideMemoryButRunnerDescriptorIsNull() {
        when(appContext.getCurrentProject()).thenReturn(project);
        when(constant.getResourcesFailed()).thenReturn(RESOURCE);
        //preparing mock for creation SuccessCallback
        when(callbackBuilderProvider.get()).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.unmarshaller(ResourcesDescriptor.class)).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.failure(any(FailureCallback.class))).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.success(Matchers.<SuccessCallback<ResourcesDescriptor>>anyObject())).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.build()).thenReturn(asyncRequestCallback);
        //preparing runner and run configuration
        when(project.getProjectDescription()).thenReturn(projectDescriptor);
        when(projectDescriptor.getRunners()).thenReturn(null);
        when(runner.getEnvironmentId()).thenReturn(ID_ENVIRONMENT);
        when(runners.getConfigs()).thenReturn(hashMap);
        when(hashMap.get(ID_ENVIRONMENT)).thenReturn(runnerConfiguration);
        when(constant.messagesTotalLessOverrideMemory(512, 384)).thenReturn(MESSAGES_TOTAL_LESS_REQUIRED_MEMORY);

        //total memory
        when(resourcesDescriptor.getTotalMemory()).thenReturn("384");
        //used memory
        when(resourcesDescriptor.getUsedMemory()).thenReturn("256");
        //overrideMemory
        when(runner.getRAM()).thenReturn(512);
        //requiredMemory 0

        checkRamAndRunAction.perform(runner);

        verify(service).getResources(asyncRequestCallback);

        verify(asyncCallbackBuilder).success(successCallBackCaptor.capture());

        SuccessCallback<ResourcesDescriptor> successCallback = successCallBackCaptor.getValue();
        successCallback.onSuccess(resourcesDescriptor);

        verify(resourcesDescriptor).getTotalMemory();
        verify(resourcesDescriptor).getUsedMemory();
        verify(project).getProjectDescription();
        verify(runners, never()).getDefault();

        verify(runner).getRAM();

        verify(constant).messagesTotalLessOverrideMemory(512, 384);
        verify(runnerUtil).showWarning(MESSAGES_TOTAL_LESS_REQUIRED_MEMORY);

        verify(runner).setStatus(FAILED);
    }

    @Test
    public void shouldShowMessagesAvailableLessOverrideMemoryButRunnerDescriptorIsNull() {
        when(appContext.getCurrentProject()).thenReturn(project);
        when(constant.getResourcesFailed()).thenReturn(RESOURCE);
        //preparing mock for creation SuccessCallback
        when(callbackBuilderProvider.get()).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.unmarshaller(ResourcesDescriptor.class)).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.failure(any(FailureCallback.class))).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.success(Matchers.<SuccessCallback<ResourcesDescriptor>>anyObject())).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.build()).thenReturn(asyncRequestCallback);
        //preparing runner and run configuration
        when(project.getProjectDescription()).thenReturn(projectDescriptor);
        when(projectDescriptor.getRunners()).thenReturn(null);
        when(runner.getEnvironmentId()).thenReturn(ID_ENVIRONMENT);
        when(runners.getConfigs()).thenReturn(hashMap);
        when(hashMap.get(ID_ENVIRONMENT)).thenReturn(runnerConfiguration);

        when(constant.messagesAvailableLessOverrideMemory()).thenReturn(MESSAGES_AVAILABLE_LESS_OVERRIDE_MEMORY);

        //total memory
        when(resourcesDescriptor.getTotalMemory()).thenReturn("1024");
        //used memory
        when(resourcesDescriptor.getUsedMemory()).thenReturn("756");
        //overrideMemory
        when(runner.getRAM()).thenReturn(512);
        //requiredMemory 0

        checkRamAndRunAction.perform(runner);

        verify(service).getResources(asyncRequestCallback);

        verify(asyncCallbackBuilder).success(successCallBackCaptor.capture());

        SuccessCallback<ResourcesDescriptor> successCallback = successCallBackCaptor.getValue();
        successCallback.onSuccess(resourcesDescriptor);

        verify(resourcesDescriptor).getTotalMemory();
        verify(resourcesDescriptor).getUsedMemory();
        verify(project).getProjectDescription();
        verify(runners, never()).getDefault();

        verify(runner).getRAM();

        verify(constant).messagesAvailableLessOverrideMemory();
        verify(runnerUtil).showError(runner, MESSAGES_AVAILABLE_LESS_OVERRIDE_MEMORY, null);

        verify(runner).setStatus(FAILED);
    }

    @Test
    public void shouldPerformWhenOverrideMemoryCorrectAndEnoughButRunnerDescriptorIsNull() {
        when(appContext.getCurrentProject()).thenReturn(project);
        when(constant.getResourcesFailed()).thenReturn(RESOURCE);
        //preparing mock for creation FailureCallback
        when(callbackBuilderProvider.get()).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.unmarshaller(ResourcesDescriptor.class)).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.failure(any(FailureCallback.class))).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.success(Matchers.<SuccessCallback<ResourcesDescriptor>>anyObject())).thenReturn(asyncCallbackBuilder);
        when(asyncCallbackBuilder.build()).thenReturn(asyncRequestCallback);
        //preparing runner and run configuration
        when(project.getProjectDescription()).thenReturn(projectDescriptor);
        when(projectDescriptor.getRunners()).thenReturn(null);
        when(runner.getEnvironmentId()).thenReturn(ID_ENVIRONMENT);
        when(runners.getConfigs()).thenReturn(hashMap);
        when(hashMap.get(ID_ENVIRONMENT)).thenReturn(runnerConfiguration);

        //total memory
        when(resourcesDescriptor.getTotalMemory()).thenReturn("1024");
        //used memory
        when(resourcesDescriptor.getUsedMemory()).thenReturn("756");
        //overrideMemory
        when(runner.getRAM()).thenReturn(256);
        //requiredMemory 0

        checkRamAndRunAction.perform(runner);

        verify(service).getResources(asyncRequestCallback);

        verify(asyncCallbackBuilder).success(successCallBackCaptor.capture());

        SuccessCallback<ResourcesDescriptor> successCallback = successCallBackCaptor.getValue();
        successCallback.onSuccess(resourcesDescriptor);

        verify(resourcesDescriptor).getTotalMemory();
        verify(resourcesDescriptor).getUsedMemory();
        verify(project).getProjectDescription();
        verify(runners, never()).getDefault();

        verify(runner).getRAM();

        runner.setRAM(256);

        verify(runAction).perform(runner);
    }
}

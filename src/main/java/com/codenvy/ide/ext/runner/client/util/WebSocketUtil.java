/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.ext.runner.client.util;

import com.codenvy.ide.websocket.rest.SubscriptionHandler;
import com.google.inject.ImplementedBy;

import javax.annotation.Nonnull;

/**
 * The utility class that simplify work flow of WebSocket.
 *
 * @author Andrey Plotnikov
 */
@ImplementedBy(WebSocketUtilImpl.class)
public interface WebSocketUtil {

    /**
     * Subscribe a given handler to WebSocket. It means new messages from this chanel will be analyzed.
     *
     * @param channel
     *         channel where handler has to be subscribed
     * @param handler
     *         handler that has to analyze messages from WebSocket
     */
    void subscribeHandler(@Nonnull String channel, @Nonnull SubscriptionHandler handler);

    /**
     * Unsubsribe a given handler from WebSocket. It means new messages from this chanel will be not analyzed.
     *
     * @param channel
     *         channel where handler is subscribed
     * @param handler
     *         handler that analyzes messages from WebSocket
     */
    void unSubscribeHandler(@Nonnull String channel, @Nonnull SubscriptionHandler handler);

}
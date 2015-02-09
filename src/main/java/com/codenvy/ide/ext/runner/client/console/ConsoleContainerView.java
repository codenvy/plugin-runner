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
package com.codenvy.ide.ext.runner.client.console;

import com.codenvy.ide.api.mvp.View;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;

import javax.annotation.Nonnull;

/**
 * @author Andrey Plotnikov
 */
@ImplementedBy(ConsoleContainerViewImpl.class)
public interface ConsoleContainerView extends View<ConsoleContainerView.ActionDelegate> {

    void showWidget(@Nonnull IsWidget console);

    void setVisible(boolean visible);

    interface ActionDelegate {
    }

}
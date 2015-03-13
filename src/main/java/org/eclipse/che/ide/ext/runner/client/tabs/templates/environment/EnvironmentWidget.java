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
package org.eclipse.che.ide.ext.runner.client.tabs.templates.environment;

import org.eclipse.che.ide.ext.runner.client.RunnerResources;
import org.eclipse.che.ide.ext.runner.client.models.Environment;
import org.eclipse.che.ide.ext.runner.client.selection.SelectionManager;
import org.eclipse.che.ide.ext.runner.client.tabs.common.item.ItemWidget;
import org.eclipse.che.ide.ext.runner.client.tabs.common.item.RunnerItems;
import org.eclipse.che.ide.ext.runner.client.tabs.properties.panel.common.Scope;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.vectomatic.dom.svg.ui.SVGImage;

import javax.annotation.Nonnull;

import static org.eclipse.che.ide.ext.runner.client.tabs.properties.panel.common.Scope.SYSTEM;

/**
 * The class contains methods which allow change view representation of runner.
 *
 * @author Dmitry Shnurenko
 */
public class EnvironmentWidget implements RunnerItems<Environment> {

    public static final String DEFAULT_DESCRIPTION = "DEFAULT";

    private final ItemWidget itemWidget;
    private final SVGImage   projectScope;
    private final SVGImage   systemScope;

    private Scope       environmentScope;
    private Environment environment;

    @Inject
    public EnvironmentWidget(final ItemWidget itemWidget, RunnerResources resources, final SelectionManager selectionManager) {
        this.itemWidget = itemWidget;

        projectScope = new SVGImage(resources.scopeProject());
        systemScope = new SVGImage(resources.scopeSystem());

        projectScope.addClassNameBaseVal(resources.runnerCss().environmentSvg());
        systemScope.addClassNameBaseVal(resources.runnerCss().environmentSvg());

        itemWidget.setDelegate(new ItemWidget.ActionDelegate() {
            @Override
            public void onWidgetClicked() {
                selectionManager.setEnvironment(environment);
            }
        });

        this.environmentScope = SYSTEM;
    }

    /**
     * Sets special environment scope.
     *
     * @param environmentScope
     *         scope which need set
     */
    public void setScope(@Nonnull Scope environmentScope) {
        this.environmentScope = environmentScope;
    }

    /** {@inheritDoc} */
    @Override
    public void select() {
        itemWidget.select();
    }

    /** {@inheritDoc} */
    @Override
    public void unSelect() {
        itemWidget.unSelect();
    }

    /** {@inheritDoc} */
    @Override
    public void update(@Nonnull Environment environment) {
        this.environment = environment;

        itemWidget.setName(environment.getName());

        String description = environment.getDescription();

        itemWidget.setDescription(description == null ? DEFAULT_DESCRIPTION : description);

        setImage();
    }

    private void setImage() {
        switch (environmentScope) {
            case PROJECT:
                itemWidget.setImage(projectScope);
                break;
            case SYSTEM:
                itemWidget.setImage(systemScope);
                break;
            default:
        }
    }

    /** {@inheritDoc} */
    @Override
    public Widget asWidget() {
        return itemWidget.asWidget();
    }

}
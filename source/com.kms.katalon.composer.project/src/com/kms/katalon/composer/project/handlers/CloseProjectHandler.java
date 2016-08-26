package com.kms.katalon.composer.project.handlers;

import static com.kms.katalon.composer.components.impl.util.EntityPartUtil.getOpenedEntityIds;
import static com.kms.katalon.composer.components.log.LoggerSingleton.logError;
import static org.eclipse.ui.PlatformUI.getPreferenceStore;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.internal.workbench.ApplicationPartServiceImpl;
import org.eclipse.e4.ui.internal.workbench.PartServiceImpl;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import com.kms.katalon.composer.project.constants.StringConstants;
import com.kms.katalon.constants.EventConstants;
import com.kms.katalon.constants.PreferenceConstants;
import com.kms.katalon.controller.ProjectController;
import com.kms.katalon.entity.project.ProjectEntity;

@SuppressWarnings("restriction")
public class CloseProjectHandler {
    @Inject
    private IEclipseContext context;
    
    @Inject
    private IEventBroker eventBroker;

    @Inject
    private EPartService partService;

    @Inject
    private EModelService modelService;

    @Inject
    private MApplication application;

    @PostConstruct
    private void registerEventHandler() {
        eventBroker.subscribe(EventConstants.PROJECT_CLOSE, new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                if (partService instanceof ApplicationPartServiceImpl) {
                    partService = context.getActive(PartServiceImpl.class);
                }
                closeCurrentProject(partService, modelService, application, eventBroker);
            }

        });
    }

    public static boolean closeCurrentProject(EPartService partService, EModelService modelService,
            MApplication application, IEventBroker eventBroker) {
        final ProjectEntity project = ProjectController.getInstance().getCurrentProject();
        if (project != null) {
            if (partService.saveAll(true)) {
                saveOpenedEntitiesState(partService, project);

                // Find and close all opened editor parts which is managed by PartService
                for (MPart p : partService.getParts()) {
                    if (p.getElementId().startsWith("com.kms.katalon.composer.content.")
                            && p.getElementId().endsWith(")")
                            || "org.eclipse.e4.ui.compatibility.editor".equals(p.getElementId())) {
                        partService.hidePart(p, true);
                    }
                }

                try {
                    if (project != null) {
                        ProjectController.getInstance().closeProject(project.getId(), null);
                        eventBroker.send(EventConstants.PROJECT_CLOSED, project.getId());
                    }
                } catch (Exception e) {
                    MessageDialog.openWarning(null, StringConstants.WARN_TITLE,
                            StringConstants.HAND_WARN_MSG_UNABLE_TO_CLOSE_CURRENT_PROJ);
                }
                return true;
            } else {
                return false;
            }
        }
        return true;

    }

    /**
     * Save all opened entities for next working session
     * 
     * @see com.kms.katalon.composer.explorer.parts.ExplorerPart#restoreOpenedEntitiesState()
     * @param partService
     * @param project
     */
    private static void saveOpenedEntitiesState(EPartService partService, ProjectEntity project) {
        if (!getPreferenceStore().getBoolean(PreferenceConstants.GENERAL_AUTO_RESTORE_PREVIOUS_SESSION)) {
            return;
        }
        try {
            List<String> openedEntityIds = getOpenedEntityIds(partService.getParts());
            ProjectController.getInstance().keepStateOfOpenedEntities(openedEntityIds);
        } catch (Exception e) {
            logError(e);
        }
    }
}

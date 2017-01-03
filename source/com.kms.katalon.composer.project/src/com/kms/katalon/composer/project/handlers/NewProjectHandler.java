package com.kms.katalon.composer.project.handlers;

import java.io.FileNotFoundException;

import javax.inject.Inject;
import javax.xml.bind.MarshalException;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.kms.katalon.composer.components.log.LoggerSingleton;
import com.kms.katalon.composer.project.constants.StringConstants;
import com.kms.katalon.composer.project.views.NewProjectDialog;
import com.kms.katalon.constants.EventConstants;
import com.kms.katalon.controller.ProjectController;
import com.kms.katalon.entity.dal.exception.FilePathTooLongException;
import com.kms.katalon.entity.project.ProjectEntity;
import com.kms.katalon.execution.launcher.manager.LauncherManager;

@SuppressWarnings("restriction")
public class NewProjectHandler {
    public static final String TEMPL_CUSTOM_KW_PKG_REL_PATH = "Keywords/com/example";
    
    @Inject
    private IEventBroker eventBroker;

    @Execute
    public void execute(Shell shell) {
        try {
            NewProjectDialog dialog = new NewProjectDialog(shell);
            if (dialog.open() != Dialog.OK) {
                return;
            }
            String projectName = dialog.getProjectName();
            String projectLocation = dialog.getProjectLocation();
            String projectDescription = dialog.getProjectDescription();
            ProjectEntity newProject = createNewProject(projectName, projectLocation, projectDescription);
            if (newProject == null) {
                return;
            }
            eventBroker.send(EventConstants.PROJECT_CREATED, newProject);

            // Open created project
            eventBroker.send(EventConstants.PROJECT_OPEN, newProject.getId());

            LauncherManager.refresh();
            eventBroker.post(EventConstants.JOB_REFRESH, null);
            eventBroker.post(EventConstants.CONSOLE_LOG_REFRESH, null);
        } catch (FilePathTooLongException ex) {
            MessageDialog.openError(Display.getCurrent().getActiveShell(), StringConstants.ERROR_TITLE, ex.getMessage());
        } catch (Exception ex) {            
            LoggerSingleton.getInstance().getLogger().error(ex);
            MessageDialog.openError(shell, StringConstants.ERROR_TITLE,
                    StringConstants.HAND_ERROR_MSG_UNABLE_TO_CREATE_NEW_PROJ);
        }
    }

    public static ProjectEntity createNewProject(String projectName, String projectLocation,
            String projectDescription) throws Exception {
        try {
            return ProjectController.getInstance().addNewProject(projectName, projectDescription, projectLocation);
        } catch (MarshalException ex) {
            if (!(ex.getLinkedException() instanceof FileNotFoundException)) {
                throw ex;
            }
            LoggerSingleton.getInstance().getLogger().error(ex);
            MessageDialog.openError(Display.getCurrent().getActiveShell(), StringConstants.ERROR_TITLE,
                    StringConstants.HAND_ERROR_MSG_NEW_PROJ_LOCATION_INVALID);
        }
        return null;
    }
}

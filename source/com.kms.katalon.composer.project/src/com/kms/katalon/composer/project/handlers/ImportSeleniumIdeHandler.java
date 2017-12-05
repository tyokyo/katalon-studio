package com.kms.katalon.composer.project.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import com.kms.katalon.composer.components.impl.tree.FolderTreeEntity;
import com.kms.katalon.composer.components.impl.tree.TestSuiteTreeEntity;
import com.kms.katalon.composer.components.log.LoggerSingleton;
import com.kms.katalon.composer.components.tree.ITreeEntity;
import com.kms.katalon.composer.project.constants.StringConstants;
import com.kms.katalon.composer.util.groovy.GroovyGuiUtil;
import com.kms.katalon.constants.EventConstants;
import com.kms.katalon.constants.IdConstants;
import com.kms.katalon.controller.ObjectRepositoryController;
import com.kms.katalon.controller.ProjectController;
import com.kms.katalon.controller.TestCaseController;
import com.kms.katalon.controller.TestSuiteController;
import com.kms.katalon.entity.folder.FolderEntity;
import com.kms.katalon.entity.folder.FolderEntity.FolderType;
import com.kms.katalon.entity.link.TestSuiteTestCaseLink;
import com.kms.katalon.entity.repository.WebElementEntity;
import com.kms.katalon.entity.testcase.TestCaseEntity;
import com.kms.katalon.entity.testsuite.TestSuiteEntity;
import com.kms.katalon.execution.launcher.manager.LauncherManager;
import com.kms.katalon.selenium.ide.ImportSeleniumIdeService;
import com.kms.katalon.selenium.ide.format.SeleniumIdeFormatter;
import com.kms.katalon.selenium.ide.model.Command;
import com.kms.katalon.selenium.ide.model.TestCase;
import com.kms.katalon.selenium.ide.model.TestSuite;

public class ImportSeleniumIdeHandler {
	
	@Inject
    private ESelectionService selectionService;
	
	@Inject
    private IEventBroker eventBroker;

	private FolderTreeEntity testSuiteTreeRoot;
	
	private FolderTreeEntity testCaseTreeRoot;
	
	private FolderTreeEntity objectRepositoryTreeRoot;
	
	@CanExecute
	public boolean canExecute() {
	    return (ProjectController.getInstance().getCurrentProject() != null)
                && !LauncherManager.getInstance().isAnyLauncherRunning();
	}
	
	@Execute
	public void execute() {
		try {
			Shell shell = Display.getCurrent().getActiveShell();
			FileDialog fileDialog = new FileDialog(shell, SWT.SYSTEM_MODAL);
			fileDialog.setText(StringConstants.HAND_IMPORT_SELENIUM_IDE);
			fileDialog.setFilterPath(Platform.getLocation().toString());
			String selectedFile = fileDialog.open();
			if (selectedFile != null && selectedFile.length() > 0) {
				File testSuiteFile = new File(selectedFile);
				if (testSuiteFile != null && testSuiteFile.exists()) {
					TestSuite testSuite = ImportSeleniumIdeService.getInstance().parseTestSuite(testSuiteFile);
					createTestSuite(testSuite);
				}
			}
		} catch (Exception e) {
			LoggerSingleton.logError(e);
		}
	}
	
	private void createTestSuite(TestSuite testSuite) throws Exception {
		Object[] selectedObjects = (Object[]) selectionService.getSelection(IdConstants.EXPLORER_PART_ID);
        ITreeEntity testSuiteParentTreeEntity = findParentTreeEntity(FolderType.TESTSUITE, selectedObjects);
        if (testSuiteParentTreeEntity == null) {
            if (testSuiteTreeRoot == null) {
                return;
            }
            testSuiteParentTreeEntity = testSuiteTreeRoot;
        }

        if (testSuiteParentTreeEntity == null) {
            return;
        }
        
        FolderEntity testSuiteParentFolderEntity = (FolderEntity) testSuiteParentTreeEntity.getObject();
        
		TestSuiteController tsController = TestSuiteController.getInstance();
		TestSuiteEntity testSuiteEntity = tsController.newTestSuiteWithoutSave(testSuiteParentFolderEntity, testSuite.getName());
		testSuiteEntity = tsController.saveNewTestSuite(testSuiteEntity);
        
        createTestCases(testSuiteEntity, testSuite.getTestCases());
        
        eventBroker.send(EventConstants.EXPLORER_REFRESH_TREE_ENTITY, testSuiteParentTreeEntity);
        eventBroker.post(EventConstants.EXPLORER_SET_SELECTED_ITEM, new TestSuiteTreeEntity(testSuiteEntity, testSuiteParentTreeEntity));
        eventBroker.post(EventConstants.TEST_SUITE_OPEN, testSuiteEntity);
	}
	
	public static ITreeEntity findParentTreeEntity(FolderType folderType, Object[] selectedObjects) throws Exception {
        if (selectedObjects != null) {
            for (Object entity : selectedObjects) {
                if (entity instanceof ITreeEntity) {
                    Object entityObject = ((ITreeEntity) entity).getObject();
                    if (entityObject instanceof FolderEntity) {
                        FolderEntity folder = (FolderEntity) entityObject;
                        if (folder.getFolderType() == folderType) {
                            return (ITreeEntity) entity;
                        }
                    } else if (entityObject instanceof TestCaseEntity) {
                        return ((ITreeEntity) entity).getParent();
                    }
                }
            }
        }
        return null;
    }

	private void createTestCases(TestSuiteEntity testSuiteEntity, List<TestCase> testCases) throws Exception {
		Object[] selectedObjects = (Object[]) selectionService.getSelection(IdConstants.EXPLORER_PART_ID);
        ITreeEntity parentTreeEntity = findParentTreeEntity(FolderType.TESTCASE, selectedObjects);
        if (parentTreeEntity == null) {
            parentTreeEntity = testCaseTreeRoot;
        }
        FolderEntity parentFolderEntity = (FolderEntity) parentTreeEntity.getObject();
        TestCaseController tcController = TestCaseController.getInstance();
        
        if (!testCases.isEmpty()) {
        	List<TestSuiteTestCaseLink> testSuiteTestCaseLinks = new ArrayList<>();
        	
        	for (TestCase testCase : testCases) {
        		TestCaseEntity testCaseEntity = tcController.newTestCaseWithoutSave(parentFolderEntity, testCase.getName());
        		testCaseEntity = tcController.saveNewTestCase(testCaseEntity);
        		eventBroker.send(EventConstants.TESTCASE_OPEN, testCaseEntity);
        		
        		String scriptContent = SeleniumIdeFormatter.getInstance().format(testCase).toString();
        		GroovyGuiUtil.addContentToTestCase(testCaseEntity, scriptContent);
        		
        		TestSuiteTestCaseLink testSuiteTestCaseLink = new TestSuiteTestCaseLink();
        		testSuiteTestCaseLink.setTestCaseId(testCaseEntity.getIdForDisplay());
        		testSuiteTestCaseLinks.add(testSuiteTestCaseLink);
        		
//        		createTestObjects(testCase.getCommands());
        	}
        	
        	TestSuiteController tsController = TestSuiteController.getInstance();
        	testSuiteEntity.setTestSuiteTestCaseLinks(testSuiteTestCaseLinks);
        	tsController.updateTestSuite(testSuiteEntity);
        	eventBroker.send(EventConstants.TEST_SUITE_OPEN, testSuiteEntity);
        }
	}
	
	private void createTestObjects(List<Command> commands) throws Exception {
		Object[] selectedObjects = (Object[]) selectionService.getSelection(IdConstants.EXPLORER_PART_ID);
        ITreeEntity parentTreeEntity = findParentTreeEntity(FolderType.WEBELEMENT, selectedObjects);
        if (parentTreeEntity == null) {
            parentTreeEntity = objectRepositoryTreeRoot;
        }
        FolderEntity parentFolderEntity = (FolderEntity) parentTreeEntity.getObject();
        ObjectRepositoryController toController = ObjectRepositoryController.getInstance();
        
        if (!commands.isEmpty()) {
        	for (Command command: commands) {
        		if (StringUtils.isNotBlank(command.getTarget())) {
	        		WebElementEntity webElement = toController.newTestObjectWithoutSave(parentFolderEntity, command.getTarget());
	        		webElement = toController.saveNewTestObject(webElement);
        		}
        	}
        }        
	}
	
	@Inject
    @Optional
    private void catchTestSuiteFolderTreeEntitiesRoot(
            @UIEventTopic(EventConstants.EXPLORER_RELOAD_INPUT) List<Object> treeEntities) {
        try {
            for (Object o : treeEntities) {
                Object entityObject = ((ITreeEntity) o).getObject();
                if (entityObject instanceof FolderEntity) {
                    FolderEntity folder = (FolderEntity) entityObject;
                    if (folder.getFolderType() == FolderType.TESTSUITE) {
                        testSuiteTreeRoot = (FolderTreeEntity) o;
                        return;
                    }
                }
            }
        } catch (Exception e) {
            LoggerSingleton.logError(e);
        }
    }
	
	@Inject
    @Optional
    private void catchTestCaseTreeEntitiesRoot(
            @UIEventTopic(EventConstants.EXPLORER_RELOAD_INPUT) List<Object> treeEntities) {
        try {
            testCaseTreeRoot = findTestCaseTreeRoot(treeEntities);
        } catch (Exception e) {
            LoggerSingleton.logError(e);
        }
    }
	
	@Inject
	@Optional
	private void catchObjectTreeEntitiesRoot(
			@UIEventTopic(EventConstants.EXPLORER_RELOAD_INPUT) List<Object> treeEntities) {
		try {
			for (Object o : treeEntities) {
				Object entityObject = ((ITreeEntity) o).getObject();
				if (entityObject instanceof FolderEntity) {
					FolderEntity folder = (FolderEntity) entityObject;
					if (folder.getFolderType() == FolderType.WEBELEMENT) {
						objectRepositoryTreeRoot = (FolderTreeEntity) o;
						return;
					}
				}
			}
		} catch (Exception e) {
			LoggerSingleton.logError(e);
		}
	}
	
	private static FolderTreeEntity findTestCaseTreeRoot(List<Object> treeEntities) throws Exception {
        for (Object o : treeEntities) {
            Object entityObject = ((ITreeEntity) o).getObject();
            if (!(entityObject instanceof FolderEntity)) {
                return null;
            }
            FolderEntity folder = (FolderEntity) entityObject;
            if (folder.getFolderType() == FolderType.TESTCASE) {
                return (FolderTreeEntity) o;
            }
        }
        return null;
    }

}

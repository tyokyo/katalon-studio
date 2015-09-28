package com.kms.katalon.composer.testsuite.listeners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolItem;

import com.kms.katalon.composer.components.impl.dialogs.TreeEntitySelectionDialog;
import com.kms.katalon.composer.components.impl.tree.FolderTreeEntity;
import com.kms.katalon.composer.components.impl.tree.TestDataTreeEntity;
import com.kms.katalon.composer.components.impl.util.TreeEntityUtil;
import com.kms.katalon.composer.components.log.LoggerSingleton;
import com.kms.katalon.composer.explorer.providers.EntityLabelProvider;
import com.kms.katalon.composer.explorer.providers.EntityProvider;
import com.kms.katalon.composer.explorer.providers.EntityViewerFilter;
import com.kms.katalon.composer.testsuite.constants.StringConstants;
import com.kms.katalon.composer.testsuite.constants.ToolItemConstants;
import com.kms.katalon.composer.testsuite.parts.TestSuitePartDataBindingView;
import com.kms.katalon.controller.FolderController;
import com.kms.katalon.controller.ProjectController;
import com.kms.katalon.controller.TestCaseController;
import com.kms.katalon.controller.TestDataController;
import com.kms.katalon.core.testdata.TestData;
import com.kms.katalon.core.testdata.TestDataFactory;
import com.kms.katalon.entity.folder.FolderEntity;
import com.kms.katalon.entity.link.TestCaseTestDataLink;
import com.kms.katalon.entity.link.TestSuiteTestCaseLink;
import com.kms.katalon.entity.link.VariableLink;
import com.kms.katalon.entity.link.VariableLink.VariableType;
import com.kms.katalon.entity.project.ProjectEntity;
import com.kms.katalon.entity.testcase.TestCaseEntity;
import com.kms.katalon.entity.testdata.DataFileEntity;
import com.kms.katalon.entity.variable.VariableEntity;

public class TestDataToolItemListener extends SelectionAdapter {

    private TableViewer tableViewer;
    private TestSuitePartDataBindingView view;

    public TestDataToolItemListener(TableViewer treeViewer, TestSuitePartDataBindingView view) {
        super();
        this.tableViewer = treeViewer;
        this.view = view;
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        if (view.getSelectedTestCaseLink() == null) {
            MessageDialog.openInformation(null, StringConstants.INFORMATION,
                    StringConstants.LIS_INFO_SELECT_A_TEST_CASE);
            return;
        }

        if (e.getSource() == null) return;

        if (e.getSource() instanceof ToolItem) {
            toolItemSelected(e);
        } else if (e.getSource() instanceof MenuItem) {
            menuItemSelected(e);
        }
    }

    private void toolItemSelected(SelectionEvent e) {
        ToolItem toolItem = (ToolItem) e.getSource();

        if (toolItem.getText() == null) return;

        switch (toolItem.getToolTipText()) {
            case ToolItemConstants.ADD:
                if (e.detail == SWT.ARROW) {
                    createDropdownMenuAddItem(toolItem);
                } else {
                    performAddTestDataLink(ToolItemConstants.ADD_AFTER);
                }
                return;
            case ToolItemConstants.REMOVE:
                removeTestDataLink();
                return;
            case ToolItemConstants.UP:
                upTestDataLink();
                return;
            case ToolItemConstants.DOWN:
                downTestDataLink();
                return;
            case ToolItemConstants.MAP:
                mapTestDataLink();
                return;
            case ToolItemConstants.MAPALL:
                mapAllTestDataLink();
                return;
            default:
                return;
        }
    }

    private void menuItemSelected(SelectionEvent e) {
        MenuItem menuItem = (MenuItem) e.getSource();
        if (menuItem.getText() == null) return;
        switch (menuItem.getText()) {
            case ToolItemConstants.ADD_AFTER:
                performAddTestDataLink(ToolItemConstants.ADD_AFTER);
                return;
            case ToolItemConstants.ADD_BEFORE:
                performAddTestDataLink(ToolItemConstants.ADD_BEFORE);
                return;
            default:
                return;
        }
    }

    private void createDropdownMenuAddItem(ToolItem toolItemAdd) {
        Rectangle rect = toolItemAdd.getBounds();
        Point pt = toolItemAdd.getParent().toDisplay(new Point(rect.x, rect.y));

        Menu menu = new Menu(toolItemAdd.getParent().getShell());

        MenuItem mnAddBefore = new MenuItem(menu, SWT.NONE);
        mnAddBefore.setText(ToolItemConstants.ADD_BEFORE);
        mnAddBefore.addSelectionListener(this);

        MenuItem mnAddAfter = new MenuItem(menu, SWT.NONE);
        mnAddAfter.setText(ToolItemConstants.ADD_AFTER);
        mnAddAfter.addSelectionListener(this);

        menu.setLocation(pt.x, pt.y + rect.height);
        menu.setVisible(true);
    }

    private void performAddTestDataLink(String offset) {
        try {
            ProjectEntity currentProject = ProjectController.getInstance().getCurrentProject();
            if (currentProject == null) return;

            EntityProvider entityProvider = new EntityProvider();
            TreeEntitySelectionDialog dialog = new TreeEntitySelectionDialog(tableViewer.getTable()
                    .getShell(), new EntityLabelProvider(), new EntityProvider(),
                    new EntityViewerFilter(entityProvider));

            dialog.setAllowMultiple(true);
            dialog.setTitle(StringConstants.LIS_TITLE_TEST_DATA_BROWSER);

            FolderEntity rootFolder = FolderController.getInstance().getTestDataRoot(currentProject);
            dialog.setInput(TreeEntityUtil.getChildren(null, rootFolder));

            if (dialog.open() == Dialog.OK && (dialog.getResult() != null)) {
                List<DataFileEntity> dataFileEntities = new ArrayList<DataFileEntity>();
                for (Object childResult : dialog.getResult()) {
                    if (childResult instanceof TestDataTreeEntity) {
                        DataFileEntity testData = (DataFileEntity) ((TestDataTreeEntity) childResult).getObject();
                        if (testData == null) continue;
                        dataFileEntities.add(testData);
                    } else if (childResult instanceof FolderTreeEntity) {
                        dataFileEntities.addAll(getTestDatasFromFolderTree((FolderTreeEntity) childResult));
                    }
                }

                List<TestCaseTestDataLink> addedTestDataLinkTreeNodes = addTestDataToTreeView(dataFileEntities, offset);

                if (addedTestDataLinkTreeNodes.size() > 0) {
                    tableViewer.refresh();
                    tableViewer.setSelection(new StructuredSelection(addedTestDataLinkTreeNodes));
                    view.refreshVariableTable();
                    view.setDirty(true);
                }
            }

        } catch (Exception e) {
            LoggerSingleton.logError(e);
            MessageDialog.openWarning(Display.getCurrent().getActiveShell(), StringConstants.ERROR_TITLE,
                    StringConstants.LIS_ERROR_MSG_UNABLE_TO_ADD_TEST_DATA);
        }
    }

    private List<TestCaseTestDataLink> getTableItems() {
        return view.getSelectedTestCaseLink().getTestDataLinks();
    }

    private List<TestCaseTestDataLink> addTestDataToTreeView(List<DataFileEntity> testDataEntities, String offset)
            throws Exception {
        List<TestCaseTestDataLink> addedTestDataLinkTreeNodes = new ArrayList<TestCaseTestDataLink>();
        int selectedIndex = tableViewer.getTable().getSelectionIndex();

        for (int i = 0; i < testDataEntities.size(); i++) {
            DataFileEntity testData = testDataEntities.get(i);

            TestCaseTestDataLink newTestDataLink = createTestDataLink(testData);
            switch (offset) {
                case ToolItemConstants.ADD_AFTER: {
                    if (selectedIndex < 0) {
                        int itemCount = tableViewer.getTable().getItemCount();
                        getTableItems().add(itemCount, newTestDataLink);
                        selectedIndex = itemCount;
                    } else {
                        getTableItems().add(selectedIndex + 1, newTestDataLink);
                        selectedIndex++;
                    }
                    break;
                }
                case ToolItemConstants.ADD_BEFORE: {
                    if (selectedIndex <= 0) {
                        getTableItems().add(0, newTestDataLink);
                        selectedIndex = 1;
                    } else {
                        getTableItems().add(selectedIndex, newTestDataLink);
                        selectedIndex++;
                    }
                    break;
                }
            }
            addedTestDataLinkTreeNodes.add(newTestDataLink);
        }
        return addedTestDataLinkTreeNodes;
    }

    private TestCaseTestDataLink createTestDataLink(DataFileEntity testData) throws Exception {
        TestCaseTestDataLink testDataLink = new TestCaseTestDataLink();
        testDataLink.setTestDataId(TestDataController.getInstance().getIdForDisplay(testData));

        return testDataLink;
    }

    private void removeTestDataLink() {
        StructuredSelection selection = (StructuredSelection) tableViewer.getSelection();
        if (selection == null || selection.size() == 0) return;
        @SuppressWarnings("unchecked")
        Iterator<TestCaseTestDataLink> iterator = selection.toList().iterator();

        while (iterator.hasNext()) {
            TestCaseTestDataLink linkNode = iterator.next();

            for (VariableLink variableLink : view.getVariableLinks()) {

                if (variableLink.getType() == VariableType.DATA_COLUMN
                        && variableLink.getTestDataLinkId().equals(linkNode.getId())) {
                    variableLink.setTestDataLinkId("");
                    variableLink.setValue("");
                }
            }
        }

        getTableItems().removeAll(selection.toList());
        tableViewer.refresh();
        view.refreshVariableTable();
        view.setDirty(true);
    }

    @SuppressWarnings("unchecked")
    private void upTestDataLink() {
        IStructuredSelection selection = (StructuredSelection) tableViewer.getSelection();
        if (selection == null || selection.size() == 0) {
            return;
        }

        List<TestCaseTestDataLink> selectedLinks = selection.toList();

        sortListOfTestCaseTestDataLinks(getTableItems(), selectedLinks);

        boolean needToRefresh = false;
        for (TestCaseTestDataLink selectedLink : selectedLinks) {

            int selectedIndex = getTableItems().indexOf(selectedLink);
            if (selectedIndex > 0) {
                TestCaseTestDataLink linkBefore = (TestCaseTestDataLink) getTableItems().get(selectedIndex - 1);

                // Avoid swap 2 objects that are both selected
                if (selectedLinks.contains(linkBefore)) {
                    continue;
                }

                Collections.swap(getTableItems(), selectedIndex - 1, selectedIndex);
                needToRefresh = true;
            }
        }

        if (needToRefresh) {
            tableViewer.refresh();
            view.refreshVariableTable();
            view.setDirty(true);
        }
    }

    @SuppressWarnings("unchecked")
    private void downTestDataLink() {
        IStructuredSelection selection = (StructuredSelection) tableViewer.getSelection();
        if (selection == null || selection.size() == 0) {
            return;
        }

        List<TestCaseTestDataLink> selectedLinks = selection.toList();

        sortListOfTestCaseTestDataLinks(getTableItems(), selectedLinks);

        boolean needToRefresh = false;
        for (TestCaseTestDataLink selectedLink : selectedLinks) {

            int selectedIndex = getTableItems().indexOf(selectedLink);
            if (selectedIndex < getTableItems().size() - 1) {
                TestCaseTestDataLink linkAfter = (TestCaseTestDataLink) getTableItems().get(selectedIndex + 1);

                // Avoid swap 2 objects that are both selected
                if (selectedLinks.contains(linkAfter)) {
                    continue;
                }

                Collections.swap(getTableItems(), selectedIndex, selectedIndex + 1);
                needToRefresh = true;
            }
        }

        if (needToRefresh) {
            tableViewer.refresh();
            view.refreshVariableTable();
            view.setDirty(true);
        }
    }

    private void sortListOfTestCaseTestDataLinks(final List<TestCaseTestDataLink> data,
            List<TestCaseTestDataLink> testDataLinks) {
        Collections.sort(testDataLinks, new Comparator<TestCaseTestDataLink>() {

            @Override
            public int compare(TestCaseTestDataLink arg0, TestCaseTestDataLink arg1) {
                return (data.indexOf(arg0) > data.indexOf(arg1)) ? 1 : -1;
            }
        });

    }

    private void mapTestDataLink() {

    }

    private void mapAllTestDataLink() {
        Map<String, String[]> columnNameHashmap = new LinkedHashMap<String, String[]>();
        Map<String, TestCaseTestDataLink> dataLinkHashMap = new LinkedHashMap<String, TestCaseTestDataLink>();

        ProjectEntity projectEntity = ProjectController.getInstance().getCurrentProject();

        for (TestCaseTestDataLink dataLink : view.getSelectedTestCaseLink().getTestDataLinks()) {
            try {
                TestData testData = TestDataFactory.findTestDataForExternalBundleCaller(dataLink.getTestDataId(),
                        projectEntity.getFolderLocation());
                if (testData == null) {
                    continue;
                }

                String[] columnNames = testData.getColumnNames();
                if (columnNames != null) {
                    columnNameHashmap.put(dataLink.getId(), columnNames);
                    dataLinkHashMap.put(dataLink.getId(), dataLink);
                }
            } catch (Exception e) {
                // Ignore it because user might not set data source for test
                // data.
            }
        }

        try {
            TestSuiteTestCaseLink testCaseLink = view.getSelectedTestCaseLink();
            TestCaseEntity testCaseEntity = TestCaseController.getInstance().getTestCaseByDisplayId(
                    testCaseLink.getTestCaseId());

            for (VariableLink variableLink : view.getSelectedTestCaseLink().getVariableLinks()) {

                VariableEntity variable = TestCaseController.getInstance().getVariable(testCaseEntity,
                        variableLink.getVariableId());
                if (variable != null) {
                    for (Entry<String, String[]> entry : columnNameHashmap.entrySet()) {
                        boolean isFound = false;

                        for (String columnName : entry.getValue()) {
                            if (variable.getName().equals(columnName)) {
                                TestCaseTestDataLink dataLink = dataLinkHashMap.get(entry.getKey());

                                variableLink.setType(VariableType.DATA_COLUMN);
                                variableLink.setTestDataLinkId(dataLink.getId());
                                variableLink.setValue(variable.getName());

                                isFound = true;
                            }
                        }

                        if (isFound) {
                            break;
                        }
                    }
                }
            }
            view.refreshVariableTable();
            view.setDirty(true);

            MessageDialog.openInformation(null, "", StringConstants.LIS_INFO_MSG_DONE);
        } catch (Exception e) {
            LoggerSingleton.logError(e);
        }
    }

    private List<DataFileEntity> getTestDatasFromFolderTree(FolderTreeEntity folderTree) {
        List<DataFileEntity> lstTestData = new ArrayList<DataFileEntity>();
        try {
            for (Object child : folderTree.getChildren()) {
                if (child instanceof TestDataTreeEntity) {
                    DataFileEntity dataFile = (DataFileEntity) ((TestDataTreeEntity) child).getObject();
                    if (dataFile != null) {
                        lstTestData.add(dataFile);
                    }
                } else if (child instanceof FolderTreeEntity) {
                    lstTestData.addAll(getTestDatasFromFolderTree((FolderTreeEntity) child));
                }
            }
        } catch (Exception e) {
            LoggerSingleton.logError(e);
        }
        return lstTestData;
    }

}
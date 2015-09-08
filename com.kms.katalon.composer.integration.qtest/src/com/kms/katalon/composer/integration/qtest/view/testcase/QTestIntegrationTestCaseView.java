package com.kms.katalon.composer.integration.qtest.view.testcase;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;

import com.kms.katalon.composer.components.dialogs.MultiStatusErrorDialog;
import com.kms.katalon.composer.components.services.UISynchronizeService;
import com.kms.katalon.composer.integration.qtest.QTestIntegrationUtil;
import com.kms.katalon.composer.integration.qtest.handlers.QTestUploadHandler;
import com.kms.katalon.composer.integration.qtest.jobs.UploadTestCaseJob;
import com.kms.katalon.composer.integration.qtest.model.TestCaseRepo;
import com.kms.katalon.composer.testcase.parts.integration.AbstractTestCaseIntegrationView;
import com.kms.katalon.controller.ProjectController;
import com.kms.katalon.controller.TestCaseController;
import com.kms.katalon.entity.file.IntegratedFileEntity;
import com.kms.katalon.entity.integration.IntegratedEntity;
import com.kms.katalon.entity.project.ProjectEntity;
import com.kms.katalon.entity.testcase.TestCaseEntity;
import com.kms.katalon.integration.qtest.QTestIntegrationTestCaseManager;
import com.kms.katalon.integration.qtest.constants.QTestStringConstants;
import com.kms.katalon.integration.qtest.entity.QTestProject;
import com.kms.katalon.integration.qtest.entity.QTestTestCase;
import com.kms.katalon.integration.qtest.setting.QTestSettingStore;

public class QTestIntegrationTestCaseView extends AbstractTestCaseIntegrationView {

    public QTestIntegrationTestCaseView(TestCaseEntity testCaseEntity, MPart mpart) {
        super(testCaseEntity, mpart);
    }

    private Text txtID;
    private Text txtName;
    private Text txtParentID;
    private QTestTestCase qTestTestCase;

    private Button btnUpload;
    private Button btnDisintegrate;
    private Button btnNavigate;
    private Text txtPID;

    /**
     * @wbp.parser.entryPoint
     */
    public Composite createContainer(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout gl_container = new GridLayout(1, false);
        gl_container.marginWidth = 0;
        gl_container.marginHeight = 0;
        container.setLayout(gl_container);

        Composite compositeButton = new Composite(container, SWT.NONE);
        compositeButton.setLayout(new GridLayout(3, false));

        btnUpload = new Button(compositeButton, SWT.NONE);
        btnUpload.setToolTipText("Upload this test case to qTest");
        btnUpload.setText("Upload");

        btnDisintegrate = new Button(compositeButton, SWT.NONE);
        btnDisintegrate
                .setToolTipText("Delete the integrated test case on qTest server and also remove its information from the file system.");
        btnDisintegrate.setText("Disintegrate");

        btnNavigate = new Button(compositeButton, SWT.NONE);
        btnNavigate.setToolTipText("Navigate to the integrated test case page on qTest");
        btnNavigate.setText("Navigate");

        Composite compositeInfo = new Composite(container, SWT.NONE);
        compositeInfo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        compositeInfo.setLayout(new GridLayout(2, false));

        Label lblNewLabel = new Label(compositeInfo, SWT.NONE);
        lblNewLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
        lblNewLabel.setText("QTest ID");

        txtID = new Text(compositeInfo, SWT.BORDER | SWT.READ_ONLY);
        txtID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblQtestName = new Label(compositeInfo, SWT.NONE);
        lblQtestName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblQtestName.setText("QTest Name");

        txtName = new Text(compositeInfo, SWT.BORDER | SWT.READ_ONLY);
        txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblPID = new Label(compositeInfo, SWT.NONE);
        lblPID.setText("Alias");

        txtPID = new Text(compositeInfo, SWT.BORDER | SWT.READ_ONLY);
        txtPID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblParentId = new Label(compositeInfo, SWT.NONE);
        lblParentId.setText("Parent ID");

        txtParentID = new Text(compositeInfo, SWT.BORDER | SWT.READ_ONLY);
        txtParentID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        initialize();
        controlModifyListeners();

        return container;
    }

    private void controlModifyListeners() {
        btnUpload.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                performUploadTestCase();
            }
        });

        btnNavigate.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                navigateToQTestTestCase();
            }

        });

        btnDisintegrate.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                disIntegrateTestCaseWithQTest();
            }

        });

    }

    protected void disIntegrateTestCaseWithQTest() {
        try {
            if (MessageDialog.openConfirm(null, "Confirmation",
                    "Are you sure you want to disintegrate this test case with qTest?")) {
                testCaseEntity.getIntegratedEntities().remove(
                        testCaseEntity.getIntegratedEntity(QTestStringConstants.PRODUCT_NAME));
                reloadView();
                setDirty(true);
            }
        } catch (Exception e) {
            MultiStatusErrorDialog.showErrorDialog(e, "Unable to delete this test case on qTest.", e.getClass()
                    .getSimpleName());
        }

    }

    private void navigateToQTestTestCase() {
        try {
            ProjectEntity projectEntity = ProjectController.getInstance().getCurrentProject();
            QTestProject qTestProject = QTestIntegrationUtil.getTestCaseRepo(testCaseEntity, projectEntity)
                    .getQTestProject();
            URL url = QTestIntegrationTestCaseManager.navigatedUrlToQTestTestCase(qTestProject, qTestTestCase,
                    projectEntity.getFolderLocation());
            IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser();
            browser.openURL(url);
        } catch (Exception e) {
            MultiStatusErrorDialog.showErrorDialog(e, "Unable to open qTest navigated test case", e.getClass()
                    .getSimpleName());
        }
    }

    private void performUploadTestCase() {
        ProjectEntity projectEntity = ProjectController.getInstance().getCurrentProject();

        if (isDirty()) {
            MessageDialog.openInformation(null, "Information", "Please save your test case before uploading.");
            return;
        }

        String token = QTestSettingStore.getToken(projectEntity.getFolderLocation());

        if (token == null || token.isEmpty()) {
            MessageDialog.openWarning(null, "Warning",
                    "QTest's token is required. Please enter a valid token on qTest setting page or\n"
                            + "you can generate a new one by clicking on generate button.");
            return;
        }

        try {
            TestCaseRepo testCaseRepo = QTestIntegrationUtil.getTestCaseRepo(testCaseEntity, projectEntity);
            if (testCaseRepo == null) {
                MessageDialog.openWarning(null, "Warning",
                        "This test case isn't in any Test Case Repository. Please add a valid Test Case Repository"
                                + " in Test Case Repositories page.");
                return;
            }

            UploadTestCaseJob uploadJob = new UploadTestCaseJob("Upload test case", UISynchronizeService.getInstance()
                    .getSync());
            List<IntegratedFileEntity> uploadedEntities = new ArrayList<IntegratedFileEntity>();
            TestCaseEntity originalEntity = TestCaseController.getInstance().getTestCase(testCaseEntity.getId());
            uploadedEntities.add(originalEntity);
            QTestUploadHandler.addParentToUploadedEntities(originalEntity, uploadedEntities);

            uploadJob.setFileEntities(uploadedEntities);

            uploadJob.doTask();
        } catch (Exception ex) {
            MultiStatusErrorDialog.showErrorDialog(ex, "Unable to upload test case to qTest.", ex.getClass()
                    .getSimpleName());
        }
    }

    private void initialize() {
        if (testCaseEntity == null) {
            btnUpload.setEnabled(false);
            btnDisintegrate.setEnabled(false);
            btnNavigate.setEnabled(false);
            return;
        }

        reloadView();
    }

    private void reloadView() {
        IntegratedEntity integratedEntity = testCaseEntity.getIntegratedEntity(QTestStringConstants.PRODUCT_NAME);

        qTestTestCase = QTestIntegrationTestCaseManager.getQTestTestCaseByIntegratedEntity(integratedEntity);

        if (qTestTestCase == null) {
            btnUpload.setEnabled(true);
            btnDisintegrate.setEnabled(false);
            btnNavigate.setEnabled(false);
        } else {
            btnUpload.setEnabled(false);
            btnDisintegrate.setEnabled(true);
            btnNavigate.setEnabled(true);
        }

        if (qTestTestCase != null) {
            txtID.setText(String.valueOf(qTestTestCase.getId()));
            txtParentID.setText(String.valueOf(qTestTestCase.getParentId()));
            txtName.setText(String.valueOf(qTestTestCase.getName()));
            txtPID.setText(qTestTestCase.getPid());
        } else {
            txtID.setText("");
            txtParentID.setText("");
            txtName.setText("");
            txtPID.setText("");
        }
    }

    public TestCaseEntity getTestCase() {
        return testCaseEntity;
    }

    public void setTestCase(TestCaseEntity testCase) {
        this.testCaseEntity = testCase;
    }
}

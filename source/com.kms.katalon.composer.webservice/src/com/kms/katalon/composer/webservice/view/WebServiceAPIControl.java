package com.kms.katalon.composer.webservice.view;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.osgi.service.event.EventHandler;

import com.kms.katalon.composer.components.event.EventBrokerSingleton;
import com.kms.katalon.composer.components.log.LoggerSingleton;
import com.kms.katalon.composer.components.util.ColorUtil;
import com.kms.katalon.composer.webservice.constants.ComposerWebserviceMessageConstants;
import com.kms.katalon.composer.webservice.constants.ImageConstants;
import com.kms.katalon.composer.webservice.constants.StringConstants;
import com.kms.katalon.constants.EventConstants;
import com.kms.katalon.controller.ProjectController;
import com.kms.katalon.core.webservice.common.WebServiceMethod;
import com.kms.katalon.core.webservice.setting.WebServiceSettingStore;
import com.kms.katalon.entity.project.ProjectEntity;
import com.kms.katalon.entity.repository.DraftWebServiceRequestEntity;
import com.kms.katalon.entity.repository.WebServiceRequestEntity;

public class WebServiceAPIControl extends Composite implements EventHandler {

    //private static final int DEFAULT_HEIGHT = 20;

    private static final int DEFAULT_REQUEST_METHOD_SELECTION_INDEX = 0;

    private CCombo cbRequestMethod;

    private Text txtRequestURL;

    private ToolItem btnSend;

    private GridData layoutData;

    private boolean sendingState;
    
    private Menu menuSend;
    
    private MenuItem mniSendAndVerify;
    
    private ToolItem btnAddRequestToTestCase;
    
    private Menu menuAddRequestToTestCase;
    
    private MenuItem mniAddRequestToNewTestCase;
    
    private MenuItem mniAddRequestToExistingTestCase;
    
    private ToolItem btnSaveDraft;
    
    private WebServiceRequestEntity originalWsObject;
    
    private WebServiceSettingStore store;
    
    private IEventBroker eventBroker;
    
    public WebServiceAPIControl(Composite parent, WebServiceRequestEntity requestEntity) {
        super(parent, SWT.NONE);
        eventBroker = EventBrokerSingleton.getInstance().getEventBroker();
        originalWsObject = requestEntity;
        store = getWebServiceSettingStore();
        boolean isSOAP = isSOAP();
        boolean isDraft = isDraft();
        String url = isSOAP ? originalWsObject.getWsdlAddress() : originalWsObject.getRestUrl();
        createControl(url, isDraft);
        setInput(isSOAP);
        eventBroker.subscribe(EventConstants.UPDATE_WEBSERVICE_METHODS, this);
    }
    
    private boolean isSOAP() {
        return WebServiceRequestEntity.SOAP.equals(originalWsObject.getServiceType());
    }
    
    private boolean isDraft() {
        return originalWsObject instanceof DraftWebServiceRequestEntity;
    }

    private void createControl(String url, boolean isDraft) {
        GridLayout gridLayout = new GridLayout(3, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.verticalSpacing = 0;
        setLayout(gridLayout);
        setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        cbRequestMethod = new CCombo(this, SWT.BORDER);
        cbRequestMethod.setBackground(ColorUtil.getWhiteBackgroundColor());
        GridData gdRequestMethod = new GridData(SWT.FILL, SWT.CENTER, false, false);
        gdRequestMethod.widthHint = 100;
        gdRequestMethod.heightHint = 22;
        cbRequestMethod.setLayoutData(gdRequestMethod);
        cbRequestMethod.setEditable(true);

        txtRequestURL = new Text(this, SWT.BORDER);
        GridData gdRequestURL = new GridData(SWT.FILL, SWT.CENTER, true, true);
        gdRequestURL.heightHint = 20;
        txtRequestURL.setLayoutData(gdRequestURL);
        txtRequestURL.setMessage(StringConstants.PA_LBL_URL);

        if (!StringUtils.trim(url).isEmpty()) {
            txtRequestURL.setText(url);
        }
      
        ToolBar toolbar = new ToolBar(this, SWT.RIGHT | SWT.RIGHT);
        toolbar.setForeground(ColorUtil.getToolBarForegroundColor());
        btnSend = new ToolItem(toolbar, SWT.DROP_DOWN);
        setSendButtonState(false);
        
        
        menuSend = new Menu(btnSend.getParent().getShell());
        mniSendAndVerify = new MenuItem(menuSend, SWT.PUSH);
        mniSendAndVerify.setText(StringConstants.MENU_ITEM_TEST_REQUEST_AND_VERIFY);
        mniSendAndVerify.setID(0);
        
        btnSend.setData(menuSend);
        
        if (!isDraft) {
            btnAddRequestToTestCase = new ToolItem(toolbar, SWT.DROP_DOWN);
            btnAddRequestToTestCase.setImage(ImageConstants.WS_ADD_TO_TEST_CASE_24);
            
            menuAddRequestToTestCase = new Menu(btnAddRequestToTestCase.getParent().getShell());
            mniAddRequestToNewTestCase = new MenuItem(menuAddRequestToTestCase, SWT.PUSH);
            mniAddRequestToNewTestCase.setText(StringConstants.MENU_ITEM_ADD_REQUEST_TO_NEW_TEST_CASE);
            mniAddRequestToNewTestCase.setID(0);
            mniAddRequestToExistingTestCase = new MenuItem(menuAddRequestToTestCase, SWT.PUSH);
            mniAddRequestToExistingTestCase.setText(StringConstants.MENU_ITEM_ADD_REQUEST_TO_EXISTING_TEST_CASE);
            mniAddRequestToExistingTestCase.setID(1);
            
            btnAddRequestToTestCase.setData(menuAddRequestToTestCase);
        }
        
        if (isDraft) {
            btnSaveDraft = new ToolItem(toolbar, SWT.PUSH);
            btnSaveDraft.setImage(ImageConstants.IMG_24_SAVE);
            btnSaveDraft.setToolTipText(ComposerWebserviceMessageConstants.BTN_SAVE_DRAFT_REQUEST);
        }
        
        toolbar.setLayoutData(new GridData(SWT.CENTER, SWT.RIGHT, false, true));

        // gdBtnSend.widthHint = 100;
    }
    
    public void addRequestMethodFocusListener(FocusListener focusListener) {
        if (focusListener == null) {
            return;
        }
        cbRequestMethod.addFocusListener(focusListener);
    }
    
    public void addRequestMethodModifyListener(ModifyListener modifyListener) {
        if (modifyListener == null) {
            return;
        }
        cbRequestMethod.addModifyListener(modifyListener);
    }

    public void addRequestMethodSelectionListener(SelectionListener selectionListener) {
        if (selectionListener == null) {
            return;
        }
        cbRequestMethod.addSelectionListener(selectionListener);
    }

    public void addRequestURLModifyListener(ModifyListener modifyListener) {
        if (modifyListener == null) {
            return;
        }
        txtRequestURL.addModifyListener(modifyListener);
    }

    public void addSendSelectionListener(SelectionListener selectionListener) {
        if (selectionListener == null) {
            return;
        }
        btnSend.addSelectionListener(selectionListener);
    }
    
    public void addSendAndVerifySelectionListener(SelectionListener selectionListener) {
        if (selectionListener == null) {
            return;
        }
        mniSendAndVerify.addSelectionListener(selectionListener);
    }
    
    public void addAddRequestToTestCaseSelectionListener(SelectionListener selectionListener) {
        if (selectionListener == null) {
            return;
        }
        btnAddRequestToTestCase.addSelectionListener(selectionListener);
    }
    
    public void addAddRequestToNewTestCaseSelectionListener(SelectionListener selectionListener) {
        if (selectionListener == null) {
            return;
        }
        mniAddRequestToNewTestCase.addSelectionListener(selectionListener);
    }
    
    public void addAddRequestToExistingTestCaseSelectionListener(SelectionListener selectionListener) {
        if (selectionListener == null) {
            return;
        }
        mniAddRequestToExistingTestCase.addSelectionListener(selectionListener);
    }
    
    public void addSaveDraftSelectionListener(SelectionListener selectionListener) {
        if (selectionListener == null) {
            return;
        }
        btnSaveDraft.addSelectionListener(selectionListener);
    }

    private void setInput(boolean isSOAP) {
        cbRequestMethod.setItems(
                isSOAP ? WebServiceRequestEntity.SOAP_REQUEST_METHODS : getRestRequestMethods());
        cbRequestMethod.select(DEFAULT_REQUEST_METHOD_SELECTION_INDEX);
        if (!isSOAP) {
            cbRequestMethod.setEditable(true);
            cbRequestMethod.setText(originalWsObject.getRestRequestMethod()); // need to set here because we support
                                                                              // custom methods for REST request
        }
    }
    
    private String[] getRestRequestMethods() {
        List<WebServiceMethod> methods;
        try {
            methods = store.getWebServiceMethods();
        } catch (IOException e) {
            LoggerSingleton.logError(e);
            methods = store.getDefaultWebServiceMethods();
        }
        
        return methods.stream()
            .map(WebServiceMethod::getName)
            .toArray(value -> new String[value]);
    }
    
    private WebServiceSettingStore getWebServiceSettingStore() {
        ProjectEntity project = ProjectController.getInstance().getCurrentProject();
        return WebServiceSettingStore.create(project.getFolderLocation());
    }

    public void setRequestMethodSelection(int index) {
        cbRequestMethod.select(index);
    }

    public int getSelectedRequestMethodIndex() {
        return cbRequestMethod.getSelectionIndex();
    }

    public String getRequestMethod() {
        return cbRequestMethod.getText();
    }

    public String getRequestURL() {
        return txtRequestURL.getText();
    }

    public void setLayoutDataColumnsSpan(int numberOfColumn) {
        layoutData.horizontalSpan = numberOfColumn;
        layout();
    }

    public CCombo getRequestMethodControl() {
        return cbRequestMethod;
    }

    public Text getRequestURLControl() {
        return txtRequestURL;
    }

    public ToolItem getSendControl() {
        return btnSend;
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    public void setSendButtonState(boolean sendingState) {
        this.sendingState = sendingState;
        if (this.sendingState) {
            btnSend.setToolTipText(StringConstants.STOP);
            btnSend.setImage(ImageConstants.IMG_24_STOP);
        } else {
            btnSend.setToolTipText(ComposerWebserviceMessageConstants.BTN_SEND_TEST_REQUEST);
            btnSend.setImage(ImageConstants.IMG_24_PLAY);
        }
        btnSend.getParent().update();
    }

    public boolean getSendingState() {
        return sendingState;
    }
    
    public Menu getSendMenu() {
        return menuSend;
    }
    
    public Menu getAddRequestToTestCaseMenu() {
        return menuAddRequestToTestCase;
    }

    @Override
    public void handleEvent(org.osgi.service.event.Event event) {
        if (cbRequestMethod == null || cbRequestMethod.isDisposed()) {
            return;
        }
        if (EventConstants.UPDATE_WEBSERVICE_METHODS.equals(event.getTopic())) {
            if (!isSOAP()) {
                //reset methods with updated ones
                cbRequestMethod.setItems(getRestRequestMethods());
            }
        }
    }
}
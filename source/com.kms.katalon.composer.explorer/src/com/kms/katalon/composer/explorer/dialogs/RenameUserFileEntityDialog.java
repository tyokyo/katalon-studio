package com.kms.katalon.composer.explorer.dialogs;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.kms.katalon.composer.components.impl.constants.StringConstants;
import com.kms.katalon.composer.components.impl.dialogs.CustomTitleAreaDialog;
import com.kms.katalon.constants.GlobalMessageConstants;
import com.kms.katalon.controller.EntityNameController;
import com.kms.katalon.entity.file.FileEntity;

public class RenameUserFileEntityDialog extends CustomTitleAreaDialog {

    private FileEntity currentFile;
    
    private List<String> siblingFileNames;
    
    private Text txtName;
    
    private String name;
    
    public RenameUserFileEntityDialog(Shell parentShell, FileEntity userFileEntity, List<String> siblingFileNames) {
        super(parentShell);
        this.currentFile = userFileEntity;
        this.siblingFileNames = siblingFileNames;
    }

    @Override
    protected Composite createContentArea(Composite parent) {
        Composite body = new Composite(parent, SWT.NONE);
        body.setLayout(new GridLayout(2, false));
        body.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        Label lblName = new Label(body, SWT.NONE);
        lblName.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
        lblName.setText(GlobalMessageConstants.NAME);
        
        txtName = new Text(body, SWT.BORDER);
        txtName.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        return body;
    }

    @Override
    protected void registerControlModifyListeners() {
        txtName.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent arg0) {
                checkNewName(txtName.getText());
            }            
        });
    }
    
    private void checkNewName(String newName) {
        if (isNameDuplicated(newName)) {
            setMessage(StringConstants.DIA_NAME_EXISTED, IMessageProvider.ERROR);
            getButton(OK).setEnabled(false);
            return;
        }
        
        try {
            EntityNameController.getInstance().validateName(newName);
            setMessage(StringConstants.DIA_MSG_CREATE_NEW_FILE, IMessageProvider.INFORMATION);
            getButton(OK).setEnabled(true);
        } catch (Exception e) {
            setMessage(e.getMessage(), IMessageProvider.ERROR);
            getButton(OK).setEnabled(false);
        }
    }
    
    private boolean isNameDuplicated(String newName) {
        return siblingFileNames.stream()
                .filter(f -> f.equalsIgnoreCase(newName))
                .findAny()
                .isPresent();
    }
    
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }
    
    @Override
    protected void okPressed() {
        name = txtName.getText();
        super.okPressed();
    }

    @Override
    protected void setInput() {
        txtName.setText(currentFile.getName());
        setMessage(StringConstants.DIA_MSG_RENAME_FILE, IMessageProvider.INFORMATION);
    }
    
    public String getNewFileName() {
        return name;
    }

    @Override
    protected Point getInitialSize() {
        return new Point(400, 250);
    }
}
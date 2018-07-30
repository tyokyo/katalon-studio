package com.kms.katalon.composer.components.impl.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.kms.katalon.composer.components.adapter.CComboContentAdapter;
import com.kms.katalon.composer.components.impl.constants.ComposerComponentsImplMessageConstants;
import com.kms.katalon.composer.components.impl.constants.StringConstants;
import com.kms.katalon.composer.components.impl.util.ControlUtils;
import com.kms.katalon.composer.components.impl.util.PlatformUtil;
import com.kms.katalon.entity.repository.WebElementPropertyEntity;

public class AddTestObjectPropertyDialog extends Dialog {

    private String name;

    private String value;

    private String condition;

    private Text txtValue;

    private CCombo ccbName;

    private CCombo ccbConditions;

    private static final String[] commonNames = { "class", "css", "id", "name", "title", "xpath" };

    public AddTestObjectPropertyDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);

        Composite container = new Composite(area, SWT.NONE);
        GridData gd_container = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd_container.widthHint = 450;
        container.setLayoutData(gd_container);
        GridLayout gl_container = new GridLayout(2, false);
        gl_container.marginWidth = 0;
        gl_container.marginHeight = 0;
        container.setLayout(gl_container);

        Label lblName = new Label(container, SWT.NONE);
        lblName.setText(StringConstants.NAME);

        ccbName = new CCombo(container, SWT.BORDER | SWT.FLAT);
        ccbName.setLayoutData(platformGridData(new GridData(SWT.FILL, SWT.CENTER, true, false)));
        ccbName.setItems(commonNames);
        new AutoCompleteField(ccbName, new CComboContentAdapter(), commonNames);

        Label lblCondition = new Label(container, SWT.NONE);
        lblCondition.setText(ComposerComponentsImplMessageConstants.VIEW_LBL_MATCH_COND);

        ccbConditions = new CCombo(container, SWT.BORDER | SWT.READ_ONLY);
        ccbConditions.setLayoutData(platformGridData(new GridData(SWT.FILL, SWT.CENTER, true, false)));
        ccbConditions.setItems(WebElementPropertyEntity.MATCH_CONDITION.getTextVlues());
        ccbConditions.select(0);

        Label lblValue = new Label(container, SWT.NONE);
        lblValue.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        lblValue.setText(StringConstants.VALUE);

        txtValue = new Text(container, SWT.BORDER);
        txtValue.setLayoutData(platformGridData(new GridData(SWT.FILL, SWT.CENTER, true, false)));

        Label separator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return area;
    }

    private GridData platformGridData(GridData gridData) {
        if (PlatformUtil.isMacOS()) {
            gridData.heightHint = ControlUtils.DF_CONTROL_HEIGHT;
        }
        return gridData;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(ComposerComponentsImplMessageConstants.VIEW_LBL_ADD_PROPERTY);
    }
    
    public Point getSize() {
        return getInitialSize();
    }

    @Override
    protected void okPressed() {
        name = ccbName.getText();
        value = txtValue.getText();
        condition = ccbConditions.getItem(ccbConditions.getSelectionIndex());
        if (name.trim().isEmpty()) {
            MessageDialog.openWarning(getParentShell(), StringConstants.WARN,
                    ComposerComponentsImplMessageConstants.VIEW_WARN_MSG_PROPERTY_CANNOT_BE_BLANK);
        } else {
            super.okPressed();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }
}
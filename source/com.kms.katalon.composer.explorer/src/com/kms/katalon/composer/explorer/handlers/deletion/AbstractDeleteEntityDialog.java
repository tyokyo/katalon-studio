package com.kms.katalon.composer.explorer.handlers.deletion;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.kms.katalon.composer.components.impl.dialogs.AbstractDialog;
import com.kms.katalon.composer.components.impl.dialogs.YesNoAllOptions;
import com.kms.katalon.composer.components.log.LoggerSingleton;
import com.kms.katalon.composer.explorer.constants.StringConstants;
import com.kms.katalon.entity.file.FileEntity;

public abstract class AbstractDeleteEntityDialog extends AbstractDialog {

    private AbstractDeleteReferredEntityHandler fHandler;

    /** Entity ID for display */
    private String entityId = StringConstants.EMPTY;

    /** List of referenced entity */
    private List<FileEntity> affectedEntities = new ArrayList<FileEntity>();

    protected TableViewer tableViewer;

    protected Label lblStatus;

    public AbstractDeleteEntityDialog(Shell parentShell, AbstractDeleteReferredEntityHandler deleteHandler) {
        super(parentShell);
        fHandler = deleteHandler;
    }

    /*
     * (non-Javadoc)
     * @see
     * com.kms.katalon.composer.components.impl.dialogs.AbstractDialog#createDialogContainer(org.eclipse.swt.widgets
     * .Composite)
     */
    @Override
    protected Control createDialogContainer(Composite parent) {
        Composite mainComposite = new Composite(parent, SWT.NONE);
        mainComposite.setLayout(new GridLayout(1, false));

        Control composite = createDialogComposite(mainComposite);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        return mainComposite;
    }

    protected Control createDialogComposite(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout glComposite = new GridLayout(1, false);
        glComposite.marginWidth = 0;
        glComposite.marginHeight = 0;
        glComposite.verticalSpacing = 10;
        composite.setLayout(glComposite);

        Composite compositeHeader = new Composite(composite, SWT.NONE);
        compositeHeader.setLayout(new GridLayout(2, false));
        compositeHeader.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblStatusImg = new Label(compositeHeader, SWT.NONE);
        lblStatusImg.setImage(Display.getCurrent().getSystemImage(SWT.ICON_WARNING));

        lblStatus = new Label(compositeHeader, SWT.WRAP);
        lblStatus.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        lblStatus.setText(MessageFormat.format(StringConstants.DIA_MSG_HEADER_ENTITY_REFERENCES, getEntityId()));

        createDialogBody(composite);

        return composite;
    }

    protected Control createDialogBody(Composite parent) {
        tableViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
        Table table = tableViewer.getTable();
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableViewerColumn tbvclmOrder = new TableViewerColumn(tableViewer, SWT.NONE);
        TableColumn tblclmnOrder = tbvclmOrder.getColumn();
        tblclmnOrder.setWidth(40);
        tblclmnOrder.setText(StringConstants.NO_);
        tbvclmOrder.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                if (element == null || !(element instanceof FileEntity)) {
                    return StringConstants.EMPTY;
                }
                return Integer.toString(getAffectedEntities().indexOf(element) + 1);
            }
        });

        TableViewerColumn tbvclmSourceID = new TableViewerColumn(tableViewer, SWT.NONE);
        TableColumn tblclmnSourceID = tbvclmSourceID.getColumn();
        tblclmnSourceID.setWidth(350);
        tblclmnSourceID.setText(StringConstants.DIA_FIELD_SOURCE_ID);
        tbvclmSourceID.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                if (element == null || !(element instanceof FileEntity)) {
                    return StringConstants.EMPTY;
                }
                try {
                    return ((FileEntity) element).getIdForDisplay();
                } catch (Exception e) {
                    return StringConstants.EMPTY;
                }
            }
        });

        tableViewer.setContentProvider(ArrayContentProvider.getInstance());
        return parent;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
     */
    @Override
    protected void buttonPressed(int buttonId) {
        fHandler.setDeletePreferenceOption(YesNoAllOptions.getOption(buttonId));
        super.okPressed();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    protected final void createButtonsForButtonBar(Composite parent) {
        // create OK and Cancel buttons by default
        for (YesNoAllOptions option : fHandler.getAvailableDeletionOptions()) {
            createButton(parent, option.ordinal(), option.toString(), true);
        }
    }

    @Override
    protected void registerControlModifyListeners() {
        mainComposite.addListener(SWT.Resize, new Listener() {

            @Override
            public void handleEvent(Event event) {
                lblStatus.pack(true);
            }
        });
    }

    @Override
    protected void setInput() {
        try {
            tableViewer.setInput(getAffectedEntities());
            getButton(YesNoAllOptions.NO.ordinal()).forceFocus();
            mainComposite.layout(true, true);
        } catch (Exception e) {
            LoggerSingleton.logError(e);
        }
    }

    /**
     * @return Deleting entity ID
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * Set deleting entity ID
     * 
     * @param entityId
     */
    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    /**
     * Get referenced entities
     * 
     * @return List of referenced entity
     */
    public List<FileEntity> getAffectedEntities() {
        return affectedEntities;
    }

    /**
     * Set referenced entities
     * 
     * @param affectedEntities List of referenced entity
     */
    public void setAffectedEntities(List<FileEntity> affectedEntities) {
        this.affectedEntities = affectedEntities;
    }

}

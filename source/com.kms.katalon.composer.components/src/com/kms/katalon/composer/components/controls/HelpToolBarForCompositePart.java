package com.kms.katalon.composer.components.controls;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import com.kms.katalon.composer.components.constants.ComposerComponentsMessageConstants;

public abstract class HelpToolBarForCompositePart extends ToolBarForMPart {

    private EPartService partService;

    public HelpToolBarForCompositePart(MPart part, EPartService partService) {
        super(part);
        this.partService = partService;
        createControls();
    }

    private void createControls() {
        new HelpToolItem(this, "", ComposerComponentsMessageConstants.LABEL_HELP_WITH_DOCUMENTATION) {
            @Override
            protected SelectionListener getSelectionListener() {
                return new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        String documentationUrlForPartObject = getDocumentationUrlForPartObject(
                                partService.getActivePart().getObject());
                        if (documentationUrlForPartObject == null) {
                            return;
                        }
                        openBrowserToLink(documentationUrlForPartObject);
                    }
                };
            }
        };
    }

    protected abstract String getDocumentationUrlForPartObject(Object partObject);

}
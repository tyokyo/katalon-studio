package com.kms.katalon.composer.testcase.support;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;

import com.kms.katalon.composer.testcase.parts.TestCaseVariablePart;
import com.kms.katalon.entity.variable.VariableEntity;

public class VariableDescriptionEditingSupport extends EditingSupport {
    
    private TestCaseVariablePart variablesPart;
    
    public VariableDescriptionEditingSupport(ColumnViewer viewer, TestCaseVariablePart variablesPart) {
        super(viewer);
        this.variablesPart = variablesPart;
    }

    @Override
    protected CellEditor getCellEditor(Object element) {
        return new TextCellEditor((Composite) this.getViewer().getControl());
    }

    @Override
    protected boolean canEdit(Object element) {
        return true;
    }

    @Override
    protected Object getValue(Object element) {
        if (element != null && element instanceof VariableEntity) {
            return ((VariableEntity) element).getDescription();
        }
        return StringUtils.EMPTY;
    }

    @Override
    protected void setValue(Object element, Object value) {
        if (element != null && element instanceof VariableEntity && value != null && value instanceof String) {
            VariableEntity param = (VariableEntity) element;
            String newParamDesc = (String) value;
            if (!newParamDesc.equals(param.getDescription())) {
                param.setDescription(newParamDesc);
                variablesPart.setDirty(true);
                getViewer().update(element, null);
            }
        }
    }

}
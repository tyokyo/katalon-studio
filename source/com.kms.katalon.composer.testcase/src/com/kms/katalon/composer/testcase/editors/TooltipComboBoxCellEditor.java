package com.kms.katalon.composer.testcase.editors;

import java.text.MessageFormat;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.kms.katalon.composer.testcase.components.TooltipCCombo;

/**
 * Cell editor that use TooltipCCombo
 *
 */
public class TooltipComboBoxCellEditor extends CellEditor {

    /**
     * The list of items to present in the combo box.
     */
    private String[] items;

    /**
     * The list of tooltips to present in the combo box;
     */
    private String[] toolTips;

    /**
     * The zero-based index of the selected item.
     */
    int selection;

    /**
     * The custom tooltip combo box control.
     */

    TooltipCCombo comboBox;

    private static final int defaultStyle = SWT.NONE;

    /**
     * Creates a new cell editor with no control and no st of choices. Initially, the cell editor has no cell validator.
     *
     * @since 2.1
     * @see CellEditor#setStyle
     * @see CellEditor#create
     * @see TooltipComboBoxCellEditor#setItems
     * @see CellEditor#dispose
     */
    public TooltipComboBoxCellEditor() {
        setStyle(defaultStyle);
    }

    /**
     * Creates a new cell editor with a combo containing the given list of choices and parented under the given control.
     * The cell editor value is the zero-based index of the selected item. Initially, the cell editor has no cell
     * validator and the first item in the list is selected.
     *
     * @param parent
     *            the parent control
     * @param items
     *            the list of strings for the combo box
     */
    public TooltipComboBoxCellEditor(Composite parent, String[] items, String[] toolTips) {
        this(parent, items, toolTips, defaultStyle);
    }

    /**
     * Creates a new cell editor with a combo containing the given list of choices and parented under the given control.
     * The cell editor value is the zero-based index of the selected item. Initially, the cell editor has no cell
     * validator and the first item in the list is selected.
     *
     * @param parent
     *            the parent control
     * @param items
     *            the list of strings for the combo box
     * @param style
     *            the style bits
     * @since 2.1
     */
    public TooltipComboBoxCellEditor(Composite parent, String[] items, String[] toolTips, int style) {
        super(parent, style);
        setItems(items, toolTips);
    }

    /**
     * Returns the list of choices for the combo box
     *
     * @return the list of choices for the combo box
     */
    public String[] getItems() {
        return this.items;
    }

    /**
     * Sets the list of choices for the combo box
     *
     * @param items
     *            the list of choices for the combo box
     */
    public void setItems(String[] items, String[] toolTips) {
        Assert.isNotNull(items);
        Assert.isNotNull(toolTips);
        this.items = items;
        this.toolTips = toolTips;
        populateComboBoxItems();
    }

    public String[] getToolTips() {
        return toolTips;
    }

    @Override
    protected Control createControl(Composite parent) {

        comboBox = new TooltipCCombo(parent, getStyle());
        comboBox.setFont(parent.getFont());

        populateComboBoxItems();

        comboBox.addKeyListener(new KeyAdapter() {
            // hook key pressed - see PR 14201
            @Override
            public void keyPressed(KeyEvent e) {
                keyReleaseOccured(e);
            }
        });

        comboBox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
                applyEditorValueAndDeactivate();
            }

            @Override
            public void widgetSelected(SelectionEvent event) {
                selection = comboBox.getSelectionIndex();
            }
        });

        comboBox.addTraverseListener(new TraverseListener() {
            @Override
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_ESCAPE || e.detail == SWT.TRAVERSE_RETURN) {
                    e.doit = false;
                }
            }
        });

        comboBox.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                TooltipComboBoxCellEditor.this.focusLost();
            }
        });
        return comboBox;
    }

    /**
     * The <code>ComboBoxCellEditor</code> implementation of this <code>CellEditor</code> framework method returns the
     * zero-based index of the current selection.
     *
     * @return the zero-based index of the current selection wrapped as an <code>Integer</code>
     */
    @Override
    protected Object doGetValue() {
        return new Integer(selection);
    }

    @Override
    protected void doSetFocus() {
        comboBox.setFocus();
    }

    /**
     * The <code>ComboBoxCellEditor</code> implementation of this <code>CellEditor</code> framework method sets the
     * minimum width of the cell. The minimum width is 10 characters if <code>comboBox</code> is not <code>null</code>
     * or <code>disposed</code> else it is 60 pixels to make sure the arrow button and some text is visible. The list of
     * CCombo will be wide enough to show its longest item.
     */
    @Override
    public LayoutData getLayoutData() {
        LayoutData layoutData = super.getLayoutData();
        if ((comboBox == null) || comboBox.isDisposed()) {
            layoutData.minimumWidth = 60;
        } else {
            // make the comboBox 10 characters wide
            GC gc = new GC(comboBox);
            layoutData.minimumWidth = (gc.getFontMetrics().getAverageCharWidth() * 10) + 10;
            gc.dispose();
        }
        return layoutData;
    }

    /**
     * The <code>ComboBoxCellEditor</code> implementation of this <code>CellEditor</code> framework method accepts a
     * zero-based index of a selection.
     *
     * @param value
     *            the zero-based index of the selection wrapped as an <code>Integer</code>
     */
    @Override
    protected void doSetValue(Object value) {
        Assert.isTrue(comboBox != null && (value instanceof Integer));
        selection = ((Integer) value).intValue();
        comboBox.select(selection);
    }

    /**
     * Updates the list of choices for the combo box for the current control.
     */
    private void populateComboBoxItems() {
        if (comboBox != null && items != null) {
            comboBox.removeAll();
            for (int i = 0; i < items.length; i++) {
                comboBox.add(items[i], i, (i < toolTips.length) ? toolTips[i] : "");
            }

            setValueValid(true);
            selection = 0;
        }
    }

    /**
     * Applies the currently selected value and deactivates the cell editor
     */
    void applyEditorValueAndDeactivate() {
        // must set the selection before getting value
        selection = comboBox.getSelectionIndex();
        Object newValue = doGetValue();
        markDirty();
        boolean isValid = isCorrect(newValue);
        setValueValid(isValid);

        if (!isValid) {
            // Only format if the 'index' is valid
            if (items.length > 0 && selection >= 0 && selection < items.length) {
                // try to insert the current value into the error message.
                setErrorMessage(MessageFormat.format(getErrorMessage(), new Object[] { items[selection] }));
            } else {
                // Since we don't have a valid index, assume we're using an
                // 'edit'
                // combo so format using its text value
                setErrorMessage(MessageFormat.format(getErrorMessage(), new Object[] { comboBox.getText() }));
            }
        }

        fireApplyEditorValue();
        deactivate();
    }

    @Override
    protected void focusLost() {
        if (isActivated()) {
            applyEditorValueAndDeactivate();
        }
    }

    @Override
    protected void keyReleaseOccured(KeyEvent keyEvent) {
        if (keyEvent.character == '\u001b') { // Escape character
            fireCancelEditor();
        } else if (keyEvent.character == '\t') { // tab key
            applyEditorValueAndDeactivate();
        }
    }
}
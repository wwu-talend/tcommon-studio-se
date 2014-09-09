// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.core.ui.context.nattableTree;

import org.eclipse.nebula.widgets.nattable.edit.editor.AbstractCellEditor;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.widget.EditModeEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.talend.core.model.process.IContextParameter;

/**
 * created by ldong on Sep 5, 2014 Detailled comment
 * 
 */
public class CustomTextCellEditor extends AbstractCellEditor {

    private final IContextParameter realPara;

    private final IStyle cellStyle;

    private final boolean commitOnUpDown;

    private final boolean moveSelectionOnEnter;

    protected boolean freeEdit = false;

    protected boolean commitOnEnter = true;

    /*
     * The wrapped editor control.
     */
    private ContextValuesNatText buttonText;

    public CustomTextCellEditor(IContextParameter realPara, IStyle cellStyle, boolean commitOnUpDown, boolean moveSelectionOnEnter) {
        this.realPara = realPara;
        this.cellStyle = cellStyle;
        this.commitOnUpDown = commitOnUpDown;
        this.moveSelectionOnEnter = moveSelectionOnEnter;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor#createEditorControl(org.eclipse.swt.widgets.Composite
     * )
     */
    @Override
    public Control createEditorControl(Composite parent) {
        int style = this.editMode == EditModeEnum.INLINE ? SWT.NONE : SWT.BORDER;
        if (!this.freeEdit) {
            style |= SWT.READ_ONLY;
        }
        final ContextValuesNatText text = new ContextValuesNatText(parent, cellStyle, realPara, style);

        text.setCursor(new Cursor(Display.getDefault(), SWT.CURSOR_IBEAM));

        addTextListener(text);
        return text;

    }

    protected void addTextListener(final ContextValuesNatText text) {
        text.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent event) {
                if (commitOnEnter && (event.keyCode == SWT.CR || event.keyCode == SWT.KEYPAD_CR)) {
                    MoveDirectionEnum move = MoveDirectionEnum.NONE;
                    if (moveSelectionOnEnter && editMode == EditModeEnum.INLINE) {
                        if (event.stateMask == 0) {
                            move = MoveDirectionEnum.DOWN;
                        } else if (event.stateMask == SWT.SHIFT) {
                            move = MoveDirectionEnum.UP;
                        }
                    }

                    commit(move);
                } else if (event.keyCode == SWT.ESC && event.stateMask == 0) {
                    close();
                } else if (commitOnUpDown && editMode == EditModeEnum.INLINE) {
                    if (event.keyCode == SWT.ARROW_UP) {
                        commit(MoveDirectionEnum.UP);
                    } else if (event.keyCode == SWT.ARROW_DOWN) {
                        commit(MoveDirectionEnum.DOWN);
                    }
                }
            }

        });

        text.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                commit(MoveDirectionEnum.NONE, editMode == EditModeEnum.INLINE);
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor#getEditorControl()
     */
    @Override
    public Control getEditorControl() {
        return this.buttonText;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor#getEditorValue()
     */
    @Override
    public Object getEditorValue() {
        return this.buttonText.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor#setEditorValue(java.lang.Object)
     */
    @Override
    public void setEditorValue(Object value) {
        this.buttonText.setValue(value != null && value.toString().length() > 0 ? value.toString() : "");

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.nebula.widgets.nattable.edit.editor.AbstractCellEditor#activateCell(org.eclipse.swt.widgets.Composite
     * , java.lang.Object)
     */
    @Override
    public Control activateCell(Composite parent, Object originalCanonicalValue) {
        this.buttonText = (ContextValuesNatText) createEditorControl(parent);

        setCanonicalValue(originalCanonicalValue);

        return this.buttonText;
    }

    /**
     * Getter for freeEdit.
     * 
     * @return the freeEdit
     */
    public boolean isFreeEdit() {
        return this.freeEdit;
    }

    /**
     * Sets the freeEdit.
     * 
     * @param freeEdit the freeEdit to set
     */
    public void setFreeEdit(boolean freeEdit) {
        this.freeEdit = freeEdit;
    }

}

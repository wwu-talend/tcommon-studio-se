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
package org.talend.repository.ui.wizards.metadata.connection.files.ldif;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.talend.commons.ui.swt.formtools.Form;
import org.talend.commons.ui.swt.formtools.LabelledCombo;
import org.talend.commons.ui.swt.formtools.LabelledFileField;
import org.talend.commons.ui.swt.formtools.UtilsButton;
import org.talend.commons.ui.utils.PathUtils;
import org.talend.commons.utils.encoding.CharsetToolkit;
import org.talend.core.model.metadata.IMetadataContextModeManager;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.repository.metadata.i18n.Messages;
import org.talend.repository.ui.swt.utils.AbstractLdifFileStepForm;

/**
 * @author cantoine
 * 
 */
public class LdifFileStep1Form extends AbstractLdifFileStepForm {

    private static Logger log = Logger.getLogger(LdifFileStep1Form.class);

    /**
     * Settings.
     */
    private static final int WIDTH_GRIDDATA_PIXEL = 300;

    /**
     * Main Fields.
     */
    private LabelledCombo serverCombo;

    private LabelledFileField fileField;

    /**
     * Another.
     */
    private boolean filePathIsDone;

    private Text fileViewerText;

    private UtilsButton cancelButton;

    private boolean readOnly;

    /**
     * Constructor to use by RCP Wizard.
     * 
     * @param existingNames
     * 
     * @param Composite
     * @param Wizard
     * @param Style
     */
    public LdifFileStep1Form(Composite parent, ConnectionItem connectionItem, String[] existingNames,
            IMetadataContextModeManager contextModeManager) {
        super(parent, connectionItem, existingNames);
        setConnectionItem(connectionItem);
        setContextModeManager(contextModeManager);
        setupForm();
    }

    /**
     * 
     * Initialize value, forceFocus first field.
     */
    @Override
    protected void initialize() {

        if (getConnection().getServer() == null) {
            serverCombo.select(0);
            getConnection().setServer(serverCombo.getText());
        } else {
            serverCombo.setText(getConnection().getServer());
        }
        serverCombo.clearSelection();

        // Just mask it.
        serverCombo.setReadOnly(true);

        if (getConnection().getFilePath() != null) {
            fileField.setText(getConnection().getFilePath().replace("\\\\", "\\")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        adaptFormToEditable();
        // init the fileViewer
        checkFilePathAndManageIt();

    }

    /**
     * DOC cantoine Comment method "adaptFormToReadOnly".
     * 
     */
    @Override
    protected void adaptFormToReadOnly() {
        readOnly = isReadOnly();
        // serverCombo.setReadOnly(isReadOnly());
        fileField.setReadOnly(isReadOnly());
        updateStatus(IStatus.OK, ""); //$NON-NLS-1$

    }

    @Override
    protected void addFields() {
        // Group File Location
        Group group = Form.createGroup(this, 1, Messages.getString("FileStep2.groupDelimitedFileSettings"), 120); //$NON-NLS-1$
        Composite compositeFileLocation = Form.startNewDimensionnedGridLayout(group, 3, WIDTH_GRIDDATA_PIXEL, 120);

        // server Combo
        String[] serverLocation = { "Localhost 127.0.0.1" }; //$NON-NLS-1$
        serverCombo = new LabelledCombo(compositeFileLocation, Messages.getString("FileStep1.server"), Messages //$NON-NLS-1$
                .getString("FileStep1.serverTip"), serverLocation, 2, true, SWT.NONE); //$NON-NLS-1$

        // file Field
        String[] extensions = { "*.ldif", "*.txt", "*.*", "*" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        fileField = new LabelledFileField(compositeFileLocation, Messages.getString("FileStep1.filepath"), extensions); //$NON-NLS-1$

        // Group File Viewer
        group = Form.createGroup(this, 1, Messages.getString("FileStep1.groupFileViewer"), 150); //$NON-NLS-1$
        Composite compositeFileViewer = Form.startNewDimensionnedGridLayout(group, 1, WIDTH_GRIDDATA_PIXEL, 150);

        fileViewerText = new Text(compositeFileViewer, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.minimumWidth = WIDTH_GRIDDATA_PIXEL;
        gridData.minimumHeight = 150;
        fileViewerText.setLayoutData(gridData);
        fileViewerText.setToolTipText(Messages.getString("FileStep1.fileViewerTip1") + " " + maximumRowsToPreview + " " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + Messages.getString("FileStep1.fileViewerTip2")); //$NON-NLS-1$
        fileViewerText.setEditable(false);
        fileViewerText.setText(Messages.getString("FileStep1.fileViewerAlert")); //$NON-NLS-1$

        if (!isInWizard()) {
            // Composite BottomButton
            Composite compositeBottomButton = Form.startNewGridLayout(this, 2, false, SWT.CENTER, SWT.CENTER);

            // Button Cancel
            cancelButton = new UtilsButton(compositeBottomButton, Messages.getString("CommonWizard.cancel"), WIDTH_BUTTON_PIXEL, //$NON-NLS-1$
                    HEIGHT_BUTTON_PIXEL);
            // nextButton = new UtilsButton(compositeBottomButton, Messages.getString("CommonWizard.next"),
            // WIDTH_BUTTON_PIXEL, HEIGHT_BUTTON_PIXEL);
        }
        addUtilsButtonListeners();
    }

    @Override
    protected void addUtilsButtonListeners() {

        if (!isInWizard()) {
            // Event cancelButton
            cancelButton.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(final SelectionEvent e) {
                    getShell().close();
                }
            });
        }

    }

    /**
     * Main Fields addControls.
     */
    @Override
    protected void addFieldsListeners() {
        // Event serverCombo
        serverCombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                getConnection().setServer(serverCombo.getText());
                checkFieldsValue();
            }
        });

        // fileField : Event modifyText
        fileField.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                if (!isContextMode()) {
                    getConnection().setFilePath(PathUtils.getPortablePath(fileField.getText()));
                    fileViewerText.setText(Messages.getString("FileStep1.fileViewerProgress")); //$NON-NLS-1$
                    checkFilePathAndManageIt();
                }
            }
        });
    }

    /**
     * checkFileFieldsValue active fileViewer if file exist.
     */
    private void checkFilePathAndManageIt() {
        updateStatus(IStatus.OK, null);
        filePathIsDone = false;
        String fileStr = fileField.getText();
        if (isContextMode() && getContextModeManager() != null) {
            fileStr = getContextModeManager().getOriginalValue(getConnection().getFilePath());
            fileStr = TalendQuoteUtils.removeQuotes(fileStr);
        }

        if (fileStr == null || fileStr == "") { //$NON-NLS-1$
            fileViewerText.setText(Messages.getString("FileStep1.fileViewerTip1") + " " + maximumRowsToPreview + " " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    + Messages.getString("FileStep1.fileViewerTip2")); //$NON-NLS-1$
        } else {
            fileViewerText.setText(Messages.getString("FileStep1.fileViewerProgress")); //$NON-NLS-1$

            StringBuffer previewRows = new StringBuffer(""); //$NON-NLS-1$
            BufferedReader in = null;

            try {

                File file = new File(fileStr);
                Charset guessedCharset = CharsetToolkit.guessEncoding(file, 4096);

                String str;
                int numberLine = 0;
                // read the file width the limit : MAXIMUM_ROWS_TO_PREVIEW
                in = new BufferedReader(new InputStreamReader(new FileInputStream(fileStr), guessedCharset.displayName()));

                while (((str = in.readLine()) != null) && (numberLine <= maximumRowsToPreview)) {
                    numberLine++;
                    previewRows.append(str + "\n"); //$NON-NLS-1$
                }

                // show lines
                fileViewerText.setText(new String(previewRows));
                filePathIsDone = true;

                if (!isContextMode()) {
                    // init of List of attributes when you change the FilePath in input.
                    if (getConnection().getValue() != null && !getConnection().getValue().isEmpty()) {
                        getConnection().getValue().removeAll(getConnection().getValue());
                    }
                }

            } catch (Exception e) {
                String msgError = Messages.getString("FileStep1.filepath") + " \"" + fileStr.replace("\\\\", "\\") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                        + "\"\n"; //$NON-NLS-1$
                if (e instanceof FileNotFoundException) {
                    msgError = msgError + Messages.getString("FileStep1.fileNotFoundException"); //$NON-NLS-1$
                } else if (e instanceof EOFException) {
                    msgError = msgError + Messages.getString("FileStep1.eofException"); //$NON-NLS-1$
                } else if (e instanceof IOException) {
                    msgError = msgError + Messages.getString("FileStep1.fileLocked"); //$NON-NLS-1$
                } else {
                    msgError = msgError + Messages.getString("FileStep1.noExist"); //$NON-NLS-1$
                }
                fileViewerText.setText(msgError);
                if (!isReadOnly()) {
                    updateStatus(IStatus.ERROR, msgError);
                }
            } finally {
                String msgError = Messages.getString("FileStep1.filepath") + " \"" + fileViewerText.getText().replace("\\\\", "\\") + "\"\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    msgError = msgError + Messages.getString("FileStep1.fileLocked"); //$NON-NLS-1$
                }
            }
            checkFieldsValue();
        }
    }

    /**
     * Ensures that fields are set.
     * 
     * @return
     */
    @Override
    protected boolean checkFieldsValue() {
        // The fields
        // serverCombo.setEnabled(true);
        //
        // if (serverCombo.getText() == "") { //$NON-NLS-1$
        // fileField.setEditable(false);
        // updateStatus(IStatus.ERROR, Messages.getString("FileStep1.serverAlert")); //$NON-NLS-1$
        // return false;
        // } else {
        if (!isContextMode()) {
            fileField.setEditable(true);
        }
        // }

        if (fileField.getText() == "") { //$NON-NLS-1$
            updateStatus(IStatus.ERROR, Messages.getString("FileStep1.filepathAlert")); //$NON-NLS-1$
            return false;
        }

        if (!filePathIsDone) {
            updateStatus(IStatus.ERROR, Messages.getString("FileStep1.fileIncomplete")); //$NON-NLS-1$
            return false;
        }

        updateStatus(IStatus.OK, null);
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.widgets.Control#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (isReadOnly() != readOnly) {
            adaptFormToReadOnly();
        }
        if (visible) {
            initialize();
        }
    }

    @Override
    protected void adaptFormToEditable() {
        super.adaptFormToEditable();
        fileField.setEditable(!isContextMode());
    }
}

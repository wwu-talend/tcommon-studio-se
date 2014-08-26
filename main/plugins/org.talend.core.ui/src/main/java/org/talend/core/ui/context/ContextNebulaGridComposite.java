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
package org.talend.core.ui.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.tooltip.AbstractTreeTooltip;
import org.talend.commons.utils.threading.ExecutionLimiter;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.context.JobContextManager;
import org.talend.core.model.context.JobContextParameter;
import org.talend.core.model.metadata.MetadataTalendType;
import org.talend.core.model.metadata.types.ContextParameterJavaTypeManager;
import org.talend.core.model.metadata.types.PerlTypesManager;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.Project;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.core.properties.tab.HorizontalTabFactory;
import org.talend.core.ui.context.ContextTreeTable.ContextTreeNode;
import org.talend.core.ui.context.model.ContextTabChildModel;
import org.talend.core.ui.context.model.ContextValueErrorChecker;
import org.talend.core.ui.context.model.ContextViewerProvider;
import org.talend.core.ui.context.model.table.ContextTableCellModifier;
import org.talend.core.ui.context.model.table.ContextTableConstants;
import org.talend.core.ui.context.model.table.ContextTableTabChildModel;
import org.talend.core.ui.context.model.table.ContextTableTabParentModel;
import org.talend.core.ui.i18n.Messages;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.repository.ProjectManager;

/**
 * created by ldong on Jul 8, 2014 Detailled comment
 * 
 */
public class ContextNebulaGridComposite extends AbstractContextTabEditComposite {

    public static final int CONTEXT_COLUMN_WIDTH = 200;

    public static final String NEW_PARAM_NAME = "new"; //$NON-NLS-1$

    private TreeViewer viewer;

    private ContextViewerProvider provider;

    private IContextModelManager modelManager = null;

    private ContextTableCellModifier cellModifier;

    private DefaultCellEditorFactory cellFactory;

    private ConfigureContextAction configContext;

    private Label contextUnAvailableLabel;

    private Combo contextsCombo;

    private Button contextConfigButton;

    private CellEditor[] cellEditors;

    private ContextValueErrorChecker valueChecker;

    private static final int VALUES_INDEX = 1;

    private ContextManagerHelper helper;

    private List<Button> buttonList;

    private Listener sortListener;

    private Button moveDownButton;

    private Button moveUpButton;

    private Button orderButton;

    private Button removeButton;

    private Composite contextTableComp;

    private ContextTreeTable treeTable;

    private HorizontalTabFactory tabFactory = null;

    /**
     * Constructor.
     * 
     * @param parent
     * @param style
     */
    public ContextNebulaGridComposite(Composite parent, IContextModelManager manager) {
        super(parent, SWT.NONE);
        modelManager = manager;
        cellFactory = new DefaultCellEditorFactory(this);
        buttonList = new ArrayList<Button>();
        this.helper = new ContextManagerHelper(manager.getContextManager());
        tabFactory = new HorizontalTabFactory();
        this.setBackground(parent.getBackground());
        this.setLayout(GridLayoutFactory.swtDefaults().spacing(0, 0).create());
        initializeUI();
    }

    public IContextManager getContextManager() {
        return modelManager.getContextManager();
    }

    /**
     * zwang Comment method "initializeUI".
     * 
     * @param viewer
     */
    private void initializeUI() {
        if (getContextManager() == null) {
            createContextNotAvailableGroup(this);
        } else {
            createContextsGroup(this);

            contextTableComp = new Composite(this, SWT.NULL);
            GridLayout dataTableLayout = new GridLayout(1, Boolean.TRUE);
            contextTableComp.setLayout(dataTableLayout);
            GridData gridData = new GridData(GridData.FILL_BOTH);
            // gridData.heightHint = 550;
            // gridData.widthHint = 500;
            contextTableComp.setLayoutData(gridData);
            treeTable = new ContextTreeTable();

            createNatTable(new ArrayList<ContextTableTabParentModel>());

            final Composite buttonsComposite = new Composite(this, SWT.NONE);
            buttonsComposite.setLayout(GridLayoutFactory.swtDefaults().spacing(0, 0).margins(0, 0).numColumns(7).create());
            GridDataFactory.swtDefaults().align(SWT.FILL, SWT.DOWN).grab(true, false).applyTo(buttonsComposite);
            buttonList.clear();
            Button addButton = createAddPushButton(buttonsComposite);
            buttonList.add(addButton);
            removeButton = createRemovePushButton(buttonsComposite);
            buttonList.add(removeButton);

            boolean isRepositoryContext = (modelManager instanceof ContextComposite)
                    && ((ContextComposite) modelManager).isRepositoryContext();
            if (!isRepositoryContext) {// for bug 7393
                moveUpButton = createMoveUpPushButton(buttonsComposite);
                buttonList.add(moveUpButton);
                moveDownButton = createMoveDownPushButton(buttonsComposite);
                buttonList.add(moveDownButton);
            }

            if ((modelManager instanceof ContextComposite) && !((ContextComposite) modelManager).isRepositoryContext()) {
                Button selectContextVariablesButton = createSelectContextVariablesPushButton(buttonsComposite);
                buttonList.add(selectContextVariablesButton);
            }
            addListener(SWT.Resize, resizeListener);
        }
    }

    private int lastCompositeSize = 0;

    private boolean propertyResized;

    private void resizeScrolledComposite() {

        lastCompositeSize = getParent().getClientArea().height;

        // setMinSize(compositeSize);
        propertyResized = true;
    }

    private void createNatTable(List<ContextTableTabParentModel> listOfData) {
        // Composite panel = new Composite(contextTableComp, SWT.NULL);// | SWT.V_SCROLL | SWT.H_SCROLL
        // panel.setLayout(new GridLayout());
        // panel.setLayoutData(new GridData(GridData.FILL_BOTH));
        // panel.setAlwaysShowScrollBars(false);
        // panel.setExpandHorizontal(true);
        // panel.setExpandVertical(true);

        GridData layoutDataFillBoth = new GridData(GridData.FILL_BOTH);
        Composite subPanel = new Composite(contextTableComp, SWT.NULL);
        subPanel.setLayoutData(layoutDataFillBoth);
        subPanel.setLayout(new GridLayout());

        ContextTreeTable.TControl tControl = treeTable.createTable(subPanel, modelManager, listOfData);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(tControl.getControl());

        // panel.setContent(subPanel);
    }

    private Button createAddPushButton(final Composite parent) {
        Button addPushButton = new Button(parent, SWT.PUSH);
        addPushButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                IContextParameter parameter = (IContextParameter) createNewEntry();
                if (parameter != null) {
                    // set the source to built-in
                    parameter.setSource(IContextParameter.BUILT_IN);
                    modelManager.onContextAddParameter(getContextManager(), parameter);

                    // see feature 4661: Add an option to propagate when add or remove a variable in a repository
                    // context to jobs/joblets.
                    if (ContextUtils.isPropagateContextVariable() && getContextManager() != null) {
                        IContextManager manager = getContextManager();
                        if (manager != null && manager instanceof JobContextManager) {
                            JobContextManager jobManger = (JobContextManager) manager;
                            // set updated flag.
                            jobManger.setModified(true);
                            jobManger.addNewParameters(parameter.getName());
                        }
                    }

                }
            }

        });
        Image image = ImageProvider.getImage(EImage.ADD_ICON);
        addPushButton.setImage(image);
        return addPushButton;
    }

    /**
     * bqian Comment method "createTreeTooltip".
     * 
     * @param tree
     */
    protected void createTreeTooltip(final Tree tree) {
        new AbstractTreeTooltip(tree) {

            /*
             * (non-Javadoc)
             * 
             * @see
             * org.talend.commons.ui.swt.tooltip.AbstractTreeTooltip#getTooltipContent(org.eclipse.swt.widgets.TreeItem)
             */
            @Override
            public String getTooltipContent(TreeItem item) {

                String property = ""; //$NON-NLS-1$
                if (properties != null && properties.length > VALUES_INDEX) {
                    property = properties[VALUES_INDEX];
                }

                IContextParameter para = cellModifier.getRealParameter(property, item.getData());
                if (para.getType().equalsIgnoreCase(PerlTypesManager.STRING)) {
                    return Messages.getString("PromptDialog.stringTip"); //$NON-NLS-1$
                }

                return null;
            }
        };
    }

    public Object createNewEntry() {
        List<IContextParameter> listParams = getContextManager().getDefaultContext().getContextParameterList();
        Integer numParam = new Integer(1);
        boolean paramNameFound;
        String paramName = null;
        do { // look for a new name
            paramNameFound = true;
            paramName = NEW_PARAM_NAME + numParam;
            for (int i = 0; i < listParams.size(); i++) {
                if (paramName.equals(listParams.get(i).getName())) {
                    paramNameFound = false;
                }
            }
            if (!paramNameFound) {
                numParam++;
            }
        } while (!paramNameFound);

        JobContextParameter contextParam = new JobContextParameter();
        contextParam.setName(paramName);
        ECodeLanguage curLanguage = LanguageManager.getCurrentLanguage();
        if (curLanguage == ECodeLanguage.JAVA) {
            contextParam.setType(ContextParameterJavaTypeManager.getDefaultJavaType().getId());
        } else {
            contextParam.setType(MetadataTalendType.getDefaultTalendType());
        }
        contextParam.setPrompt(paramName + "?"); //$NON-NLS-1$
        String defaultValue;
        if (curLanguage == ECodeLanguage.JAVA) {
            defaultValue = ContextParameterJavaTypeManager.getDefaultValueFromJavaIdType(ContextParameterJavaTypeManager
                    .getDefaultJavaType().getId(), false);
        } else {
            defaultValue = TalendQuoteUtils.addQuotes(""); //$NON-NLS-1$
        }
        contextParam.setValue(defaultValue);
        contextParam.setComment(""); //$NON-NLS-1$
        contextParam.setSource(""); //$NON-NLS-1$
        return contextParam;
    }

    private Button createRemovePushButton(final Composite parent) {
        Button removePushButton = new Button(parent, SWT.PUSH);
        removePushButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {

                IStructuredSelection sel = treeTable.getSelection();

                Object[] obj = new Object[sel.toList().size()];

                int i = 0;
                for (Object node : sel.toList().toArray()) {
                    if (node instanceof ContextTreeNode) {
                        obj[i++] = ((ContextTreeNode) node).getTreeData();
                    }

                }

                for (Object object : obj) { // multi delete
                    if (object == null) {
                        return;
                    }
                    if (object instanceof ContextTableTabParentModel) {
                        ContextTableTabParentModel parentModel = (ContextTableTabParentModel) object;
                        removeParentModelInGroupBySource(parentModel);
                    } else if (object instanceof ContextTableTabChildModel) {
                        ContextTableTabChildModel childModel = (ContextTableTabChildModel) object;
                        removeChildModelInGroupBySource(childModel);
                    }

                    modelManager.refreshTableTab();
                    checkButtonEnableState();
                }
            }
        });

        Image image = ImageProvider.getImage(EImage.DELETE_ICON);
        removePushButton.setImage(image);
        return removePushButton;
    }

    private void removeChildModelInGroupBySource(ContextTableTabChildModel child) {
        IContextParameter contextPara = child.getContextParameter();
        String sourceId = contextPara.getSource();
        String contextName = contextPara.getName();
        modelManager.onContextRemoveParameter(getContextManager(), contextName, sourceId);
    }

    private void removeParentModelInGroupBySource(ContextTableTabParentModel parentModel) {
        Set<String> paraNames = new HashSet<String>();
        String sourceId = parentModel.getSourceId();
        if (IContextParameter.BUILT_IN.equals(sourceId)) {
            String paraName = parentModel.getContextParameter().getName();
            paraNames.add(paraName);
        } else {
            List<ContextTabChildModel> children = parentModel.getChildren();
            if (children != null && children.size() > 0) {
                for (ContextTabChildModel child : children) {
                    IContextParameter contextPara = child.getContextParameter();
                    String paraName = contextPara.getName();
                    paraNames.add(paraName);
                }
            }
        }
        modelManager.onContextRemoveParameter(getContextManager(), paraNames, sourceId);
    }

    private Button createSelectContextVariablesPushButton(final Composite parent) {
        Button selectContextVariablesPushButton = new Button(parent, SWT.PUSH);
        Image image = ImageProvider.getImage(ECoreImage.CONTEXT_ICON);
        selectContextVariablesPushButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                SelectRepositoryContextDialog dialog = new SelectRepositoryContextDialog(getContextModelManager(), parent
                        .getShell(), helper);
                dialog.open();
                refresh();
            }

        });
        selectContextVariablesPushButton.setImage(image);
        return selectContextVariablesPushButton;
    }

    private Button createMoveUpPushButton(final Composite parent) {
        Button moveUpPushButton = new Button(parent, SWT.PUSH);
        Image image = ImageProvider.getImage(EImage.UP_ICON);
        moveUpPushButton.setImage(image);
        moveUpPushButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection sel = treeTable.getSelection();
                if (ContextManagerHelper.changeContextOrder(sel, modelManager, true)) {
                    checkButtonEnableState();
                }
            }
        });
        return moveUpPushButton;
    }

    private Button createMoveDownPushButton(final Composite parent) {
        Button moveDownPushButton = new Button(parent, SWT.PUSH);
        Image image = ImageProvider.getImage(EImage.DOWN_ICON);
        moveDownPushButton.setImage(image);
        moveDownPushButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection sel = treeTable.getSelection();
                if (ContextManagerHelper.changeContextOrder(sel, modelManager, false)) {
                    checkButtonEnableState();
                }
            }

        });
        return moveDownPushButton;
    }

    private void checkButtonEnableState() {
        boolean selectionEnable = false;
        boolean removeEnable = false;
        if (this.treeTable != null) {
            ISelection selection = this.treeTable.getSelection();
            if (selection != null) {
                selectionEnable = !selection.isEmpty();
                removeEnable = !selection.isEmpty();
                if (selection instanceof IStructuredSelection) {
                    IStructuredSelection sel = (IStructuredSelection) selection;
                    if (sel.size() > 1) {
                        // Multi selection, not support sort
                        selectionEnable = false;
                    }
                }
            }
        }
        selectionEnable = selectionEnable && !modelManager.isReadOnly();
        removeEnable = removeEnable && !modelManager.isReadOnly();
        boolean moveState = selectionEnable;
        if (this.moveUpButton != null) {
            this.moveUpButton.setEnabled(moveState);
        }
        if (this.moveDownButton != null) {
            this.moveDownButton.setEnabled(moveState);
        }
        if (this.removeButton != null) {
            this.removeButton.setEnabled(removeEnable);
        }
    }

    private void activateCellEditor(final TreeItem item, final Tree tree, final TreeEditor treeEditor, int columnIndex, int column) {

        IContextParameter para = cellModifier.getRealParameter(properties[column], item.getData());

        if (para == null) {
            return;
        }
        valueChecker.checkErrors(item, column, para);
        if (!para.isBuiltIn()) {
            // not built-in
            return;
        }
        cellEditor = cellFactory.getCustomCellEditor(para, tree);

        if (cellEditor == null) {
            // unable to create the editor
            return;
        }

        // activate the cell editor
        cellEditor.activate();
        // if the cell editor has no control we can stop now
        Control control = cellEditor.getControl();
        if (control == null) {
            cellEditor.deactivate();
            cellEditor = null;
            return;
        }
        Text textControl = valueChecker.getTextControl(control);
        if (textControl != null) {
            if (ContextParameterUtils.isPasswordType(para)) {
                textControl.setEchoChar('*');
            } else {
                textControl.setEchoChar((char) 0);
            }
        }

        valueChecker.register(control);
        // add our editor listener
        cellEditor.addListener(createEditorListener(treeEditor, column));

        // set the layout of the tree editor to match the cell editor
        CellEditor.LayoutData layout = cellEditor.getLayoutData();
        treeEditor.horizontalAlignment = layout.horizontalAlignment;
        treeEditor.grabHorizontal = layout.grabHorizontal;
        treeEditor.minimumWidth = layout.minimumWidth;

        treeEditor.setEditor(control, item, column);
        // give focus to the cell editor
        cellEditor.setFocus();

    }

    protected void handleSelect(final TreeItem item, final Tree tree, final TreeEditor treeEditor, int columnIndex, int column) {
        // get the new selection
        activateCellEditor(item, tree, treeEditor, columnIndex, column);
    }

    @Override
    public boolean isGroupBySource() {
        boolean isRepositoryContext = false;
        if (modelManager != null) {
            isRepositoryContext = (modelManager instanceof ContextComposite)
                    && ((ContextComposite) modelManager).isRepositoryContext();
        }
        boolean value = getPreferenceStore().getBoolean(ITalendCorePrefConstants.CONTEXT_GROUP_BY_SOURCE);
        return value && !isRepositoryContext;
    }

    private void deactivateCellEditor(final TreeEditor tableEditor, int columnIndex) {
        tableEditor.setEditor(null, null, columnIndex);
        if (cellEditor != null) {
            Control control = cellEditor.getControl();
            if (control != null) {
                valueChecker.unregister(control);
            }
            cellEditor.deactivate();
            cellEditor.removeListener(editorListener);
            cellEditor = null;
        }
    }

    private ICellEditorListener createEditorListener(final TreeEditor tableEditor, final int columnIndex) {
        editorListener = new ICellEditorListener() {

            @Override
            public void cancelEditor() {
                deactivateCellEditor(tableEditor, columnIndex);
            }

            @Override
            public void editorValueChanged(boolean oldValidState, boolean newValidState) {
            }

            @Override
            public void applyEditorValue() {
                editing = true;
            }
        };
        return editorListener;
    }

    private void createContextNotAvailableGroup(Composite parentComposite) {
        contextUnAvailableLabel = new Label(parentComposite, SWT.NULL);
        contextUnAvailableLabel.setText(Messages.getString("ContextNebulaComposite.ContextsUnAvailable"));
    }

    private void createContextsGroup(Composite parentComposite) {
        Composite contextsSelectComp = new Composite(parentComposite, SWT.NULL);
        contextsSelectComp.setLayout(new GridLayout(3, false));
        GridLayout layout2 = (GridLayout) contextsSelectComp.getLayout();
        layout2.marginHeight = 0;
        layout2.marginTop = 0;
        layout2.marginBottom = 0;

        Label contextSeletLabel = new Label(contextsSelectComp, SWT.NULL);
        contextSeletLabel.setText("Default context enviroment");
        contextsCombo = new Combo(contextsSelectComp, SWT.READ_ONLY);
        contextsCombo.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Object obj = e.getSource();
                String selectContext = ((Combo) obj).getText();
                IContext defaultContext = modelManager.getContextManager().getDefaultContext();
                if (selectContext.equals(defaultContext.getName())) {
                } else {
                    IContext newSelContext = null;
                    for (IContext enviroContext : modelManager.getContextManager().getListContext()) {
                        if (selectContext.equals(enviroContext.getName())) {
                            newSelContext = enviroContext;
                        }
                    }
                    modelManager.onContextChangeDefault(modelManager.getContextManager(), newSelContext);
                    refresh();
                }

            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {

            }

        });

        configContext = new ConfigureContextAction(modelManager, this.getShell());
        contextConfigButton = new Button(contextsSelectComp, SWT.NULL);
        contextConfigButton.setImage(ImageProvider.getImage(configContext.getImageDescriptor()));
        contextConfigButton.setToolTipText(configContext.getText());
        contextConfigButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                configContext.run();
            }
        });
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (configContext != null) {
            configContext.setEnabled(enabled);
        }

    }

    private ICellEditorListener editorListener;

    private CellEditor cellEditor;

    private String[] properties;

    private boolean editing;

    /**
     * bqian Comment method "getContexts".
     * 
     * @return
     */
    public List<IContext> getContexts() {
        List<IContext> contexts = new ArrayList<IContext>();
        IContextManager cm = modelManager.getContextManager();
        if (cm != null) {
            contexts = cm.getListContext();
        }
        return contexts;
    }

    @Override
    public IContextModelManager getContextModelManager() {
        return this.modelManager;
    }

    @Override
    public TreeViewer getViewer() {
        return this.viewer;
    }

    public ContextValueErrorChecker getValueChecker() {
        return this.valueChecker;
    }

    private void reInitizalize() {
        if (contextUnAvailableLabel != null && !contextUnAvailableLabel.isDisposed()) {
            contextUnAvailableLabel.dispose();
        }
        if (contextsCombo == null) {
            initializeUI();
        }
    }

    @Override
    public void refresh() {
        if (getContextManager() != null) {
            reInitizalize();
            List<IContext> contextList = getContexts();
            List<IContextParameter> contextDatas = ContextTemplateComposite.computeContextTemplate(contextList);
            if (contextsCombo.getItems().length > 0) {
                contextsCombo.removeAll();
            }
            if (modelManager.getContextManager() != null) {
                for (IContext context : modelManager.getContextManager().getListContext()) {
                    if (!Arrays.asList(contextsCombo.getItems()).contains(context.getName())) {
                        contextsCombo.add(context.getName());
                    }
                }

                for (int i = 0; i < contextsCombo.getItemCount(); i++) {
                    IContext defaultContext = modelManager.getContextManager().getDefaultContext();
                    if (defaultContext.getName().equals(contextsCombo.getItem(i))) {
                        contextsCombo.select(i);
                        break;
                    }
                }
            }
            int visibleItemCount = contextsCombo.getItemCount();
            if (visibleItemCount > 20) {
                visibleItemCount = 20;
            }
            contextsCombo.setVisibleItemCount(visibleItemCount);

            List<ContextTableTabParentModel> listofData = constructContextDatas(contextDatas);
            // dispose the data table composite
            disposeDataTable();
            // create the data table composite
            createNatTable(listofData);

            contextTableComp.getParent().layout();
            contextTableComp.layout();
            treeTable.refresh();
        }
    }

    public int getLastCompositeSize() {
        return this.lastCompositeSize;
    }

    private final Listener resizeListener = new Listener() {

        @Override
        public void handleEvent(Event event) {
            resizeLimiter.resetTimer();
            resizeLimiter.startIfExecutable(true, null);
        }
    };

    private final ExecutionLimiter resizeLimiter = new ExecutionLimiter(250, true) {

        @Override
        public void execute(final boolean isFinalExecution, Object data) {
            if (!isDisposed()) {
                getDisplay().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        if (!isDisposed() && !getParent().isDisposed()) {
                            int currentSize = getParent().getClientArea().height;
                            // if (getLastCompositeSize() != currentSize) {
                            refresh();
                            // }
                        }
                    }

                });
            }
        }
    };

    private ContextTableTabParentModel lookupContextParentForNonBuiltinNode(String sourceId,
            List<ContextTableTabParentModel> output) {
        ContextTableTabParentModel firstLevelNode = null;
        if (output != null && output.size() > 0) {
            for (ContextTableTabParentModel parent : output) {
                String tempSourceId = parent.getSourceId();
                if (tempSourceId != null && sourceId.equals(tempSourceId)) {
                    firstLevelNode = parent;
                    break;
                }
            }
        }
        return firstLevelNode;
    }

    private List<ContextTableTabParentModel> constructContextDatas(List<IContextParameter> contextDatas) {
        List<ContextTableTabParentModel> output = new ArrayList<ContextTableTabParentModel>();
        if (!contextDatas.isEmpty()) {
            int i = 0;
            for (IContextParameter para : contextDatas) {
                String sourceId = para.getSource();
                if (IContextParameter.BUILT_IN.equals(sourceId)) {
                    sourceId = para.getSource();
                    ContextTableTabParentModel firstLevelNode = new ContextTableTabParentModel();
                    String sourceLabel = sourceId;
                    ContextItem contextItem = ContextUtils.getContextItemById2(sourceId);
                    if (contextItem != null) {
                        sourceLabel = contextItem.getProperty().getLabel();
                        final ProjectManager pm = ProjectManager.getInstance();
                        if (!pm.isInCurrentMainProject(contextItem)) {
                            final Project project = pm.getProject(contextItem);
                            if (project != null) {
                                firstLevelNode.setProjectLabel(project.getLabel());
                            }
                        }
                    }
                    firstLevelNode.setOrder(i);
                    firstLevelNode.setSourceName(sourceLabel);
                    firstLevelNode.setSourceId(sourceId);
                    firstLevelNode.setContextParameter(para);
                    output.add(firstLevelNode);
                } else {
                    ContextTableTabParentModel firstLevelNode = null;
                    if (sourceId != null) {
                        firstLevelNode = lookupContextParentForNonBuiltinNode(sourceId, output);
                        if (firstLevelNode == null) {
                            firstLevelNode = new ContextTableTabParentModel();
                            output.add(firstLevelNode);
                            String sourceLabel = sourceId;
                            ContextItem contextItem = ContextUtils.getContextItemById2(sourceId);
                            if (contextItem != null) {
                                sourceLabel = contextItem.getProperty().getLabel();
                                final ProjectManager pm = ProjectManager.getInstance();
                                if (!pm.isInCurrentMainProject(contextItem)) {
                                    final Project project = pm.getProject(contextItem);
                                    if (project != null) {
                                        firstLevelNode.setProjectLabel(project.getLabel());
                                    }
                                }
                            }
                            firstLevelNode.setSourceName(sourceLabel);
                            firstLevelNode.setOrder(i);
                            firstLevelNode.setSourceId(sourceId);
                        }

                        ContextTableTabChildModel child = new ContextTableTabChildModel();
                        child.setSourceId(sourceId);
                        child.setContextParameter(para);
                        child.setParent(firstLevelNode);
                        firstLevelNode.addChild(child);
                    }
                }
                i++;
            }
        }
        return output;
    }

    private void disposeDataTable() {
        if (contextTableComp != null && !contextTableComp.isDisposed()) {
            for (Control control : contextTableComp.getChildren()) {
                control.dispose();
            }
        }
    }

    /**
     * cli Comment method "createColumnsAndCellEditors".
     */
    private void createColumnsAndCellEditors(final Tree tree, List<IContext> contextList) {
        TreeColumn column = new TreeColumn(tree, SWT.NONE);
        column.setText(Messages.getString("ConextTableValuesComposite.nameLabel")); //$NON-NLS-1$
        column.setWidth(ContextTableValuesComposite.CONTEXT_COLUMN_WIDTH);

        column = new TreeColumn(tree, SWT.NONE);
        column.setText(Messages.getString("ContextTemplateComposite.typeLabel")); //$NON-NLS-1$
        column.setWidth(ContextTableValuesComposite.CONTEXT_COLUMN_WIDTH);

        for (IContext context : contextList) {
            column = new TreeColumn(tree, SWT.NONE);
            column.setText(context.getName());
            column.setWidth(ContextTableValuesComposite.CONTEXT_COLUMN_WIDTH);
        }

        cellEditors = new CellEditor[getContexts().size() + 2];
        for (int i = 0; i < getContexts().size() + 2; i++) {
            if (i == 0) {
                cellEditors[i] = new TextCellEditor(tree);
            } else if (i == 1) {
                String[] values = ContextParameterJavaTypeManager.getJavaTypesLabels();
                cellEditors[i] = new ComboBoxCellEditor(tree, values, SWT.NULL);
            } else {
                cellEditors[i] = new TextCellEditor(tree);
            }
        }
        ((CCombo) cellEditors[1].getControl()).setEditable(false);

        properties = new String[contextList.size() + 2];
        properties[0] = ContextTableConstants.COLUMN_NAME_PROPERTY;
        properties[1] = ContextTableConstants.COLUMN_TYPE_PROPERTY;
        for (int i = 0; i < contextList.size(); i++) {
            properties[i + 2] = contextList.get(i).getName();
        }
        viewer.setColumnProperties(properties);
        viewer.setCellEditors(cellEditors);
    }

    public String[] getColumnProperties() {
        return this.properties;
    }

    private void checkItemValueErrors(final TreeItem[] items) {
        if (items == null) {
            return;
        }
        for (TreeItem item : items) {
            for (int i = 1; i < viewer.getColumnProperties().length; i++) {
                IContextParameter para = cellModifier.getRealParameter((String) viewer.getColumnProperties()[i], item.getData());
                if (para != null && para instanceof IContextParameter) {
                    valueChecker.checkErrors(item, i, para);
                }
            }
            checkItemValueErrors(item.getItems());
        }
    }

    /**
     * Clear the data in this viewer.
     * 
     * @param jobContextManager2
     */
    public void clear() {
        // final Tree tree = viewer.getTree();
        // TreeColumn[] columns = tree.getColumns();
        // for (TreeColumn tableColumn : columns) {
        // tableColumn.dispose();
        // }
        // viewer.setInput(Collections.EMPTY_LIST);
    }

    /**
     * DOC zli ContextCompare class global comment. Detailled comment
     */
    private class ContextCompare implements java.util.Comparator<IContext> {

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(IContext o1, IContext o2) {
            String name1 = o1.getName().toUpperCase();
            String name2 = o2.getName().toUpperCase();
            return name1.compareTo(name2);
        }
    }
}

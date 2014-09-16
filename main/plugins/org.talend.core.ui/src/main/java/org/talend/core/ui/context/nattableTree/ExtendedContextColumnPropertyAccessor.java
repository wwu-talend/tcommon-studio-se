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

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gef.commands.Command;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.context.JobContextManager;
import org.talend.core.model.metadata.types.ContextParameterJavaTypeManager;
import org.talend.core.model.metadata.types.JavaType;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.ui.context.IContextModelManager;
import org.talend.core.ui.context.model.table.ContextTableConstants;
import org.talend.core.ui.context.model.table.ContextTableTabChildModel;
import org.talend.core.ui.context.model.table.ContextTableTabParentModel;
import org.talend.core.ui.context.model.template.ContextConstant;

/**
 * this one is specially for access the context columns
 */
public class ExtendedContextColumnPropertyAccessor<R> implements IColumnPropertyAccessor<R> {

    private static final Log log = LogFactory.getLog(ReflectiveColumnPropertyAccessor.class);

    private final List<String> propertyNames;

    // for the different of contexts,there is different column groups
    private final ColumnGroupModel groupModel;

    private Map<String, PropertyDescriptor> propertyDescriptorMap;

    private final static String JOBLET_CONTEXT = " (from joblet)";

    private final static String REPOSITORYT_CONTEXT = " (from repository context)";

    /**
     * @param propertyNames of the members of the row bean
     */
    public ExtendedContextColumnPropertyAccessor(String[] propertyNames, ColumnGroupModel groupModel) {
        this.propertyNames = Arrays.asList(propertyNames);
        this.groupModel = groupModel;
    }

    @Override
    public int getColumnCount() {
        return propertyNames.size();
    }

    @Override
    public Object getDataValue(R rowObj, int columnIndex) {
        Class<? extends Object> contextRowClas = rowObj.getClass();
        try {
            Method managerMethod = contextRowClas.getMethod("getManager");
            IContextModelManager manager = (IContextModelManager) managerMethod.invoke(rowObj);
            Method dataMethod = contextRowClas.getMethod("getTreeData");
            Object treeData = dataMethod.invoke(rowObj);
            return getPropertyValue(manager, treeData, columnIndex);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setDataValue(R rowObj, int columnIndex, Object newValue) {
        Class<? extends Object> contextRowClas = rowObj.getClass();
        try {
            Method managerMethod = contextRowClas.getMethod("getManager");
            IContextModelManager manager = (IContextModelManager) managerMethod.invoke(rowObj);
            Method nameMethod = contextRowClas.getMethod("getName");
            String paraName = (String) nameMethod.invoke(rowObj);
            Method dataMethod = contextRowClas.getMethod("getTreeData");
            Object treeData = dataMethod.invoke(rowObj);
            setPropertyValue(manager, treeData, paraName, columnIndex, newValue);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getColumnProperty(int columnIndex) {
        return propertyNames.get(columnIndex);
    }

    @Override
    public int getColumnIndex(String propertyName) {
        return propertyNames.indexOf(propertyName);
    }

    private String getCurrentModelName(Object element) {
        if (element instanceof ContextTableTabParentModel) {
            if (((ContextTableTabParentModel) element).getContextParameter() == null) {
                return ((ContextTableTabParentModel) element).getSourceName();
            } else {
                return ((ContextTableTabParentModel) element).getContextParameter().getName();
            }
        } else {
            return ((ContextTableTabChildModel) element).getContextParameter().getName();
        }
    }

    private Object getPropertyValue(IContextModelManager manager, Object element, int columnIndex) {

        if (isEmptyTreeNode(element)) {
            return "";
        }
        String contextParaName = getCurrentModelName(element);
        String currentColumnName = getColumnProperty(columnIndex);
        if (currentColumnName.equals(ContextTableConstants.COLUMN_NAME_PROPERTY)) {
            if (element instanceof ContextTableTabParentModel) {
                String sourceId = ((ContextTableTabParentModel) element).getSourceId();
                if (!sourceId.equals(IContextParameter.BUILT_IN)) {
                    Item item = ContextUtils.getContextItemById3(sourceId);
                    if (item != null) {
                        if (item instanceof JobletProcessItem) {
                            return contextParaName + JOBLET_CONTEXT;
                        } else {
                            return contextParaName + REPOSITORYT_CONTEXT;
                        }
                    }
                } else {
                    return contextParaName;
                }
            } else {
                return contextParaName;
            }
        } else {
            IContextParameter currentParam = getRealParameter(manager.getContextManager(), currentColumnName, element);
            if (currentParam != null) {
                if (columnIndex == 1) {
                    String contextParaType = currentParam.getType();
                    JavaType javaType = ContextParameterJavaTypeManager.getJavaTypeFromId(contextParaType);
                    if (javaType != null) {
                        return javaType.getLabel();
                    } else {
                        return contextParaType;
                    }
                } else {
                    if (this.groupModel.isPartOfAGroup(columnIndex)) {
                        String columnGroupName = this.groupModel.getColumnGroupByIndex(columnIndex).getName();
                        if (manager.getContextManager() != null) {
                            List<IContext> contexts = manager.getContextManager().getListContext();
                            IContextParameter currentPara = findContextPara(contexts, columnGroupName, contextParaName);
                            if (currentColumnName.equals(ContextTableConstants.COLUMN_CHECK_PROPERTY)) {
                                return currentPara.isPromptNeeded();
                            } else if (currentColumnName.equals(ContextTableConstants.COLUMN_PROMPT_PROPERTY)) {
                                return currentPara.getPrompt();
                            } else {
                                String value = currentPara.getValue();
                                if (NatTableCellEditorFactory.isPassword(currentPara.getType())) {
                                    return value != null ? value.replaceAll(".", "*") : "";
                                }
                                return value;
                            }
                        }
                    }
                }
            }
        }
        return "";
    }

    private IContextParameter findContextPara(List<IContext> contexts, String columnGroupName, String contextParaName) {
        for (IContext envContext : contexts) {
            if (envContext.getName().equals(columnGroupName)) {
                List<IContextParameter> list = envContext.getContextParameterList();
                if (list != null && list.size() > 0) {
                    for (IContextParameter contextPara : list) {
                        String tempContextParaName = contextPara.getName();
                        if (tempContextParaName.equals(contextParaName)) {
                            return contextPara;
                        }
                    }
                }
            }
        }
        return null;
    }

    private void setPropertyValue(IContextModelManager manager, Object dataElement, String contextParaName, int columnIndex,
            Object newValue) {
        String currentColumnName = getColumnProperty(columnIndex);
        if (this.groupModel.isPartOfAGroup(columnIndex)) {
            String columnGroupName = this.groupModel.getColumnGroupByIndex(columnIndex).getName();
            IContextManager contextManger = manager.getContextManager();
            if (contextManger != null) {
                // change the property of context such as prompt,promptyNeeded,value etc.
                List<Object> list = new ArrayList<Object>();
                list.add(dataElement);

                IContextParameter para = null;
                para = getRealParameter(contextManger, columnGroupName, dataElement);
                if (para == null) {
                    return;
                }
                Command cmd = new SetContextGroupParameterCommand(manager, para, currentColumnName, newValue);
                runCommand(cmd, manager);
                if (currentColumnName.equals(ContextTableConstants.COLUMN_CHECK_PROPERTY)) {
                    manager.refresh();
                }
            }
        } else {
            if (currentColumnName.equals(ContextTableConstants.COLUMN_TYPE_PROPERTY)) {
                ContextTableTabParentModel parent = (ContextTableTabParentModel) dataElement;
                IContextParameter contextPara = parent.getContextParameter();
                if (contextPara.getType() == ((String) newValue)) {
                    return;
                }
                String newType = getRealType((String) newValue);
                contextPara.setType(newType);

                Command cmd = new SetContextTypeCommand(manager, contextPara, newType);
                runCommand(cmd, manager);
            } else if (currentColumnName.equals(ContextTableConstants.COLUMN_NAME_PROPERTY)) {
                ContextTableTabParentModel parent = (ContextTableTabParentModel) dataElement;
                IContextParameter contextPara = parent.getContextParameter();
                String sourceId = contextPara.getSource();
                String newParaName = (String) newValue;

                if (manager.getContextManager() instanceof JobContextManager) {
                    // in case joblet rename will propagate to the job,just record it
                    JobContextManager contextManager = (JobContextManager) manager.getContextManager();
                    contextManager.addNewName(newParaName, contextPara.getName());
                    contextManager.setModified(true);
                }
                Command cmd = new SetContextNameCommand(manager, contextPara, newParaName, sourceId);
                runCommand(cmd, manager);
            }
        }
    }

    private String getRealType(String type) {
        final ECodeLanguage codeLanguage = LanguageManager.getCurrentLanguage();
        if (codeLanguage == ECodeLanguage.JAVA) {
            StringBuffer sb = new StringBuffer("id_"); //$NON-NLS-1$
            JavaType javaType = JavaTypesManager.getJavaTypeFromLabel(type);
            if (type.indexOf(ContextConstant.DOWNWARDS_STRING) != -1) {
                return javaType.getId();
            } else {
                if (javaType != null) {
                    return javaType.getId();
                } else {
                    return sb.append(type.trim()).toString();
                }

            }
        } else {
            return type;
        }
    }

    public IContextParameter getRealParameter(IContextManager manager, String property, Object element) {
        IContextParameter para = null;
        IContext context = null;
        if (manager != null) {
            if (!(property.equals(ContextTableConstants.COLUMN_NAME_PROPERTY))) {
                context = manager.getContext(property);
            }
            if (context == null) {
                return null;
            }
            if (element instanceof ContextTableTabParentModel) {
                if (IContextParameter.BUILT_IN.equals(((ContextTableTabParentModel) element).getSourceId())) {
                    IContextParameter builtContextParameter = ((ContextTableTabParentModel) element).getContextParameter();
                    if (builtContextParameter != null) {
                        para = context.getContextParameter(builtContextParameter.getName());
                    }
                }
            } else if (element instanceof ContextTableTabChildModel) {
                ContextTableTabChildModel child = (ContextTableTabChildModel) element;
                String sourceId = child.getContextParameter().getSource();
                para = context.getContextParameter(sourceId, ((ContextTableTabChildModel) element).getContextParameter()
                        .getName());
            }
        }
        return para;
    }

    class SetContextGroupParameterCommand extends Command {

        IContextParameter param;

        IContextModelManager modelManager;

        Object newValue, oldValue;

        String property;

        public SetContextGroupParameterCommand(IContextModelManager modelManager, IContextParameter param, String property,
                Object newValue) {
            super();
            this.modelManager = modelManager;
            this.param = param;
            this.property = property;
            this.newValue = newValue;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.gef.commands.Command#execute()
         */
        @Override
        public void execute() {
            boolean modified = false;
            if (property.equals(ContextTableConstants.COLUMN_CHECK_PROPERTY)) {
                if (param.isPromptNeeded() == (Boolean) newValue) {
                    return;
                }
                oldValue = param.isPromptNeeded();
                param.setPromptNeeded((Boolean) newValue);
                modified = true;
            } else if (property.equals(ContextTableConstants.COLUMN_PROMPT_PROPERTY)) {
                if (param.getPrompt().equals(newValue)) {
                    return;
                }
                oldValue = param.getPrompt();
                param.setPrompt((String) newValue);
                modified = true;
            } else if (property.equals(ContextTableConstants.COLUMN_CONTEXT_VALUE)) {
                if (param.getValue() != null && param.getValue().equals(newValue)) {
                    return;
                }
                oldValue = param.getValue();
                if (newValue != null) {
                    param.setValue((String) newValue);
                    modified = true;
                }
            }
            if (modified) {
                updateRelation();
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.gef.commands.Command#undo()
         */
        @Override
        public void undo() {
            boolean modified = false;
            if (property.equals(ContextTableConstants.COLUMN_CHECK_PROPERTY)) {
                param.setPromptNeeded((Boolean) oldValue);
                modified = true;
            } else if (property.equals(ContextTableConstants.COLUMN_PROMPT_PROPERTY)) {
                param.setPrompt((String) oldValue);
                modified = true;
            } else if (property.equals(ContextTableConstants.COLUMN_CONTEXT_VALUE)) {
                param.setValue((String) oldValue);
                modified = true;
            }

            if (modified) {
                updateRelation();
            }
        }

        private void updateRelation() {
            // set updated flag.
            if (param != null) {
                IContextManager manager = modelManager.getContextManager();
                if (manager != null && manager instanceof JobContextManager) {
                    JobContextManager jobContextManager = (JobContextManager) manager;
                    // not added new
                    if (!modelManager.isRepositoryContext() || modelManager.isRepositoryContext()
                            && jobContextManager.isOriginalParameter(param.getName())) {
                        jobContextManager.setModified(true);
                        manager.fireContextsChangedEvent();
                    }
                }
            }
        }

    }

    class SetContextTypeCommand extends Command {

        IContextParameter param;

        IContextModelManager modelManager;

        String newValue, oldValue;

        public SetContextTypeCommand(IContextModelManager modelManager, IContextParameter param, String newValue) {
            super();
            this.modelManager = modelManager;
            this.param = param;
            this.newValue = newValue;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.gef.commands.Command#execute()
         */
        @Override
        public void execute() {
            boolean modified = false;
            if (modelManager.getContextManager() != null) {
                for (IContext context : modelManager.getContextManager().getListContext()) {
                    for (IContextParameter contextParameter : context.getContextParameterList()) {
                        if (param.getName().equals(contextParameter.getName())) {
                            oldValue = param.getType();
                            contextParameter.setType(newValue);
                        }
                    }
                }
            }
            if (modified) {
                updateRelation();
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.gef.commands.Command#undo()
         */
        @Override
        public void undo() {
            boolean modified = false;
            if (modelManager.getContextManager() != null) {
                for (IContext context : modelManager.getContextManager().getListContext()) {
                    for (IContextParameter contextParameter : context.getContextParameterList()) {
                        if (param.getName().equals(contextParameter.getName())) {
                            contextParameter.setType(oldValue);
                            modified = true;
                        }
                    }
                }
            }
            if (modified) {
                updateRelation();
            }
        }

        private void updateRelation() {
            // set updated flag.
            if (param != null) {
                IContextManager manager = modelManager.getContextManager();
                if (manager != null && manager instanceof JobContextManager) {
                    JobContextManager jobContextManager = (JobContextManager) manager;
                    // not added new
                    if (!modelManager.isRepositoryContext() || modelManager.isRepositoryContext()
                            && jobContextManager.isOriginalParameter(param.getName())) {
                        jobContextManager.setModified(true);
                        manager.fireContextsChangedEvent();
                    }
                }
            }
        }

    }

    class SetContextNameCommand extends Command {

        IContextParameter param;

        IContextModelManager modelManager;

        String sourceId;

        String newName, originalName;

        public SetContextNameCommand(IContextModelManager modelManager, IContextParameter param, String newName, String sourceId) {
            super();
            this.modelManager = modelManager;
            this.param = param;
            this.newName = newName;
            this.sourceId = sourceId;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.gef.commands.Command#execute()
         */
        @Override
        public void execute() {
            boolean modified = false;
            if (modelManager.getContextManager() != null) {
                originalName = param.getName();
                for (IContext context : modelManager.getContextManager().getListContext()) {
                    for (IContextParameter contextParameter : context.getContextParameterList()) {
                        String tempSourceId = contextParameter.getSource();
                        if (originalName.equals(contextParameter.getName()) && tempSourceId.equals(sourceId)) {
                            contextParameter.setName(newName);

                            if (contextParameter.getPrompt().equals(originalName + "?")) {
                                contextParameter.setPrompt(newName + "?");
                            }
                            modified = true;
                        }
                    }
                }
                param.setName(newName);
                if (param.getPrompt().equals(originalName + "?")) {
                    param.setPrompt(newName + "?");
                }
            }
            if (modified) {
                updateRelation();
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.gef.commands.Command#undo()
         */
        @Override
        public void undo() {
            boolean modified = false;
            if (modelManager.getContextManager() != null) {
                for (IContext context : modelManager.getContextManager().getListContext()) {
                    for (IContextParameter contextParameter : context.getContextParameterList()) {
                        if (newName.equals(contextParameter.getName())) {
                            contextParameter.setName(originalName);
                            if (contextParameter.getPrompt().equals(newName + "?")) {
                                contextParameter.setPrompt(originalName + "?");
                            }
                            modified = true;
                        }
                    }
                }
            }
            if (modified) {
                updateRelation();
            }
        }

        private void updateRelation() {
            // set updated flag.
            if (param != null) {
                IContextManager manager = modelManager.getContextManager();
                if (manager != null && manager instanceof JobContextManager) {
                    JobContextManager jobContextManager = (JobContextManager) manager;
                    // not added new
                    if (!modelManager.isRepositoryContext() || modelManager.isRepositoryContext()
                            && jobContextManager.isOriginalParameter(param.getName())) {
                        jobContextManager.setModified(true);
                        manager.fireContextsChangedEvent();
                    }
                }
            }
        }

    }

    public void runCommand(Command command, IContextModelManager modelManager) {
        if (modelManager.getCommandStack() == null) {
            command.execute();
        } else {
            modelManager.getCommandStack().execute(command);
        }
    }

    private boolean isEmptyTreeNode(Object treeData) {
        if (treeData instanceof ContextTableTabParentModel) {
            if (((ContextTableTabParentModel) treeData).getContextParameter() == null
                    && ((ContextTableTabParentModel) treeData).getChildren().size() == 0) {
                return true;
            }
        }
        return false;
    }
}

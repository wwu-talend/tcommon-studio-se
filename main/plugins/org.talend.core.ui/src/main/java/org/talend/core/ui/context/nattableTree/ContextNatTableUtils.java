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

import java.util.ArrayList;
import java.util.List;

import org.talend.core.GlobalServiceRegister;
import org.talend.core.ILibraryManagerUIService;
import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Project;
import org.talend.core.ui.context.model.table.ContextTableConstants;
import org.talend.core.ui.context.model.table.ContextTableTabChildModel;
import org.talend.core.ui.context.model.table.ContextTableTabParentModel;
import org.talend.repository.ProjectManager;

/**
 * created by ldong on Jul 28, 2014 Detailled comment
 * 
 */
public class ContextNatTableUtils {

    public static final String REQUIRE_BUNDLE_NAME = "org.talend.libraries.nattable"; //$NON-NLS-1$

    public static boolean isEmptyTreeNode(Object treeData) {
        if (treeData instanceof ContextTableTabParentModel) {
            if (((ContextTableTabParentModel) treeData).getContextParameter() == null
                    && ((ContextTableTabParentModel) treeData).getChildren().size() == 0) {
                return true;
            }
        }
        return false;
    }

    public static IContextParameter getRealParameter(IContextManager manager, String property, Object element) {
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

    public static boolean checkIsInstallExternalJar() {
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ILibraryManagerUIService.class)) {
            ILibraryManagerUIService libUiService = (ILibraryManagerUIService) GlobalServiceRegister.getDefault().getService(
                    ILibraryManagerUIService.class);

            return libUiService.isModuleInstalledForBundle(REQUIRE_BUNDLE_NAME);
        }
        return false;
    }

    public static List<ContextTableTabParentModel> constructContextDatas(List<IContextParameter> contextDatas) {
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
                            // the item maybe a joblet item now
                            Item contextItem = ContextUtils.getContextItemById3(sourceId);
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

    public static ContextTableTabParentModel lookupContextParentForNonBuiltinNode(String sourceId,
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

}

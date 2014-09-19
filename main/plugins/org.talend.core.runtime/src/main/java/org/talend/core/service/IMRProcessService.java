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
package org.talend.core.service;

import java.util.List;

import org.eclipse.ui.IEditorPart;
import org.talend.core.IService;
import org.talend.core.model.general.Project;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.nodes.IProjectRepositoryNode;

/**
 * DOC ggu class global comment. Detailled comment <br/>
 * 
 * $Id: talend.epf 55206 2011-02-15 17:32:14Z mhirt $
 * 
 */
public interface IMRProcessService extends IService {

    boolean needMRProcess();

    IRepositoryNode getRootNode(IProjectRepositoryNode projectNode);

    public boolean collectStandardProcessNode(List<String> filteredContents, Object node);

    public boolean isMapReduceItem(Item item);

    public boolean isMapReduceEditor(IEditorPart editorPart);

    public List<IRepositoryViewObject> getMapReduceProcesses(Project project);

    public void generateMRInfosParameter(IProcess2 process);
}

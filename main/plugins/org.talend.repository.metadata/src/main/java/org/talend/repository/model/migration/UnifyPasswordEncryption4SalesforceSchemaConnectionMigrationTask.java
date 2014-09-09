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
package org.talend.repository.model.migration;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.SalesforceSchemaConnectionItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;

/**
 * created by ggu on Aug 29, 2014 Detailled comment
 *
 */
public class UnifyPasswordEncryption4SalesforceSchemaConnectionMigrationTask extends AbstractItemMigrationTask {

    ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA);
        return toReturn;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org .talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        if (item instanceof SalesforceSchemaConnectionItem) {
            Connection connection = ((SalesforceSchemaConnectionItem) item).getConnection();
            if (connection instanceof SalesforceSchemaConnection) {
                SalesforceSchemaConnection ssConn = (SalesforceSchemaConnection) connection;
                try {
                    if (!ssConn.isContextMode()) {
                        // before this migration ,the pass is raw, didn't encrypt.
                        ssConn.setPassword(ssConn.getValue(ssConn.getPassword(), true));
                        ssConn.setProxyPassword(ssConn.getValue(ssConn.getProxyPassword(), true));
                        ssConn.setConsumeSecret(ssConn.getValue(ssConn.getConsumeSecret(), true));
                        factory.save(item, true);
                    }
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                    return ExecutionResult.FAILURE;
                }
            }
        }
        return ExecutionResult.SUCCESS_NO_ALERT;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.migration.IProjectMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2014, 8, 29, 12, 0, 0);
        return gc.getTime();
    }

}

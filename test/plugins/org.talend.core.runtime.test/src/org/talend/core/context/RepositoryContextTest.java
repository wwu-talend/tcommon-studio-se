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
package org.talend.core.context;

import static org.junit.Assert.*;

import org.junit.Test;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.User;

/**
 * created by Talend on Jul 2, 2014 Detailled comment
 * 
 */
public class RepositoryContextTest {

    @Test
    public void testSetUser() {
        RepositoryContext repositoryContext = new RepositoryContext();
        User user1 = PropertiesFactory.eINSTANCE.createUser();
        user1.setAuthenticationInfo("oldInfo");
        user1.setLogin("test");

        User user2 = PropertiesFactory.eINSTANCE.createUser();
        user2.setLogin("test");

        repositoryContext.setUser(user1);
        repositoryContext.setUser(user2);

        assertEquals(repositoryContext.getUser().getAuthenticationInfo(), "oldInfo");
    }
}

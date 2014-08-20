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
package org.talend.utils.security;

import junit.framework.Assert;

import org.junit.Test;

/**
 * created by ggu on Aug 20, 2014 Detailled comment
 *
 */
@SuppressWarnings("nls")
public class CryptoHelperTest {

    @Test
    public void testEncrypt4Null() {
        String encrypt = CryptoHelper.DEFAULT.encrypt(null);
        Assert.assertNull(encrypt);
    }

    @Test
    public void testEncrypt4EMPTY() {
        String encrypt = CryptoHelper.DEFAULT.encrypt("");
        Assert.assertNotNull(encrypt);
        Assert.assertEquals("", encrypt);
    }

    @Test
    public void testDecrypt4Null() {
        String encrypt = CryptoHelper.DEFAULT.decrypt(null);
        Assert.assertNull(encrypt);
    }

    @Test
    public void testDecrypt4EMPTY() {
        String encrypt = CryptoHelper.DEFAULT.decrypt("");
        Assert.assertNotNull(encrypt);
        Assert.assertEquals("", encrypt);
    }

    @Test
    public void testSpace() {
        String encrypt = CryptoHelper.DEFAULT.encrypt(" ");
        Assert.assertNotNull(encrypt);
        Assert.assertEquals("4n6Q8vByOrg=", encrypt);

        String decrypt = CryptoHelper.DEFAULT.decrypt(encrypt);
        Assert.assertNotNull(decrypt);
        Assert.assertEquals(" ", decrypt);
    }

    @Test
    public void testNumber() {
        String encrypt = CryptoHelper.DEFAULT.encrypt("123");
        Assert.assertNotNull(encrypt);
        Assert.assertEquals("/81ashGeQx8=", encrypt);

        String decrypt = CryptoHelper.DEFAULT.decrypt(encrypt);
        Assert.assertNotNull(decrypt);
        Assert.assertEquals("123", decrypt);
    }

    @Test
    public void testCharacters() {
        String encrypt = CryptoHelper.DEFAULT.encrypt("ABC 123");
        Assert.assertNotNull(encrypt);
        Assert.assertEquals("l/R1qjFcRGA=", encrypt);

        String decrypt = CryptoHelper.DEFAULT.decrypt(encrypt);
        Assert.assertNotNull(decrypt);
        Assert.assertEquals("ABC 123", decrypt);
    }

    @Test
    public void testMoreCharacters() {
        String encrypt = CryptoHelper.DEFAULT.encrypt("ABC 123 !@#$%%^+_ QETUVNK>?><");
        Assert.assertNotNull(encrypt);
        Assert.assertEquals("eVC4UR2126US+sO9OaetY+1lXzwYmFd8OG4BkrrOXuk=", encrypt);

        String decrypt = CryptoHelper.DEFAULT.decrypt(encrypt);
        Assert.assertNotNull(decrypt);
        Assert.assertEquals("ABC 123 !@#$%%^+_ QETUVNK>?><", decrypt);
    }

}

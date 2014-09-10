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
package org.talend.core.database.hbase.conn.version;

import java.util.ArrayList;
import java.util.List;

/**
 * DOC ycbai class global comment. Detailled comment
 */
public enum EHBaseDistribution4Versions {
    HDP_2_1(EHBaseDistributions.HORTONWORKS, "Hortonworks Data Platform V2.1.0(Baikal)", "HDP_2_1"),

    HDP_2_0(EHBaseDistributions.HORTONWORKS, "Hortonworks Data Platform V2.0.0(BigWheel)", "HDP_2_0"),

    HDP_1_3(EHBaseDistributions.HORTONWORKS, "Hortonworks Data Platform V1.3.0(Condor)", "HDP_1_3"),

    HDP_1_2(EHBaseDistributions.HORTONWORKS, "Hortonworks Data Platform V1.2.0(Bimota)", "HDP_1_2"),

    HDP_1_0(EHBaseDistributions.HORTONWORKS, "Hortonworks Data Platform V1.0.0(deprecated)", "HDP_1_0"),

    APACHE_0_20_203(EHBaseDistributions.APACHE, "Apache 0.20.203", "APACHE_0_20_203"),

    APACHE_1_0_3_EMR(EHBaseDistributions.AMAZON_EMR, "Apache 1.0.3(EMR)", "APACHE_1_0_3_EMR"),

    APACHE_1_0_0(EHBaseDistributions.APACHE, "Apache 1.0.0", "APACHE_1_0_0"),

    CLOUDERA_CDH5_1(EHBaseDistributions.CLOUDERA, "Cloudera CDH5.1", "Cloudera_CDH5_1"),

    CLOUDERA_CDH5(EHBaseDistributions.CLOUDERA, "Cloudera CDH5", "Cloudera_CDH5"),

    CLOUDERA_CDH4_YARN(EHBaseDistributions.CLOUDERA, "Cloudera CDH4 YARN", "Cloudera_CDH4_YARN"),

    CLOUDERA_CDH4(EHBaseDistributions.CLOUDERA, "Cloudera CDH4", "Cloudera_CDH4"),

    CLOUDERA_CDH3(EHBaseDistributions.CLOUDERA, "Cloudera CDH3(deprecated)", "Cloudera_CDH3"),

    MAPR_3_1_0(EHBaseDistributions.MAPR, "MapR 3.1.0", "MAPR310"),

    MAPR_3_0_1(EHBaseDistributions.MAPR, "MapR 3.0.1", "MAPR301"),

    MAPR_2_1_3(EHBaseDistributions.MAPR, "MapR 2.1.3", "MAPR213"),

    MAPR_2_1_2(EHBaseDistributions.MAPR, "MapR 2.1.2", "MAPR212"),

    MAPR(EHBaseDistributions.MAPR, "MapR 2.0.0", "MAPR2"),

    PIVOTAL_HD_2_0(EHBaseDistributions.PIVOTAL_HD, "Pivotal HD 2.0", "PIVOTAL_HD_2_0"),

    PIVOTAL_HD_1_0_1(EHBaseDistributions.PIVOTAL_HD, "Pivotal HD 1.0.1", "PIVOTAL_HD_1_0_1"),

    CUSTOM(EHBaseDistributions.CUSTOM, "", "");

    private EHBaseDistributions distribution;

    private String versionDisplayName;

    private String versionValue;

    EHBaseDistribution4Versions(EHBaseDistributions distribution, String versionDisplayName, String versionValue) {
        this.distribution = distribution;
        this.versionDisplayName = versionDisplayName;
        this.versionValue = versionValue;
    }

    public static List<EHBaseDistribution4Versions> indexOfByDistribution(String distribution) {
        List<EHBaseDistribution4Versions> distribution4Versions = new ArrayList<EHBaseDistribution4Versions>();
        if (distribution != null) {
            for (EHBaseDistribution4Versions d4v : EHBaseDistribution4Versions.values()) {
                if (d4v.getDistribution().getName().equals(distribution)
                        || d4v.getDistribution().getDisplayName().equals(distribution)) {
                    distribution4Versions.add(d4v);
                }
            }
        }
        return distribution4Versions;
    }

    public static EHBaseDistribution4Versions indexOfByVersionDisplay(String displayName) {
        return indexOf(displayName, true);
    }

    public static EHBaseDistribution4Versions indexOfByVersion(String value) {
        return indexOf(value, false);
    }

    private static EHBaseDistribution4Versions indexOf(String name, boolean display) {
        if (name != null) {
            for (EHBaseDistribution4Versions version : EHBaseDistribution4Versions.values()) {
                if (display) {
                    if (name.equalsIgnoreCase(version.getVersionDisplayName())) {
                        return version;
                    }
                } else {
                    if (name.equalsIgnoreCase(version.getVersionValue())) {
                        return version;
                    }
                }
            }
        }
        return null;
    }

    public static List<String> getHadoopDistributionVersions(String distribution) {
        return getHadoopDistributionVersions(distribution, true);
    }

    public static List<String> getHadoopDistributionVersions(String distribution, boolean display) {
        List<String> result = new ArrayList<String>();
        List<EHBaseDistribution4Versions> d4vList = EHBaseDistribution4Versions.indexOfByDistribution(distribution);
        for (EHBaseDistribution4Versions d4v : d4vList) {
            if (display) {
                result.add(d4v.getVersionDisplayName());
            } else {
                result.add(d4v.getVersionValue());
            }
        }
        return result;
    }

    public EHBaseDistributions getDistribution() {
        return this.distribution;
    }

    public String getVersionDisplayName() {
        return this.versionDisplayName;
    }

    public String getVersionValue() {
        return this.versionValue;
    }

}

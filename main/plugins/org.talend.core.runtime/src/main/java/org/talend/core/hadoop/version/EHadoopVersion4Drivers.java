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
package org.talend.core.hadoop.version;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

/**
 * DOC ycbai class global comment. Detailled comment
 */
public enum EHadoopVersion4Drivers {
    HDP_2_1(
            EHadoopDistributions.HORTONWORKS,
            "Hortonworks Data Platform V2.1.0(Baikal)",
            "HDP_2_1",
            true,
            false,
            new EMRVersion[] { EMRVersion.YARN }),

    HDP_2_0(
            EHadoopDistributions.HORTONWORKS,
            "Hortonworks Data Platform V2.0.0(BigWheel)",
            "HDP_2_0",
            true,
            false,
            new EMRVersion[] { EMRVersion.YARN }),

    HDP_1_3(EHadoopDistributions.HORTONWORKS, "Hortonworks Data Platform V1.3.0(Condor)", "HDP_1_3", true, false),

    HDP_1_2(EHadoopDistributions.HORTONWORKS, "Hortonworks Data Platform V1.2.0(Bimota)", "HDP_1_2", true, false),

    HDP_1_0(EHadoopDistributions.HORTONWORKS, "Hortonworks Data Platform V1.0.0(deprecated)", "HDP_1_0", true, false),

    APACHE_1_0_0(EHadoopDistributions.APACHE, "Apache 1.0.0", "APACHE_1_0_0", true, false),

    APACHE_0_20_204(EHadoopDistributions.APACHE, "Apache 0.20.204", "APACHE_0_20_204", false, false),

    APACHE_0_20_203(EHadoopDistributions.APACHE, "Apache 0.20.203", "APACHE_0_20_203", false, false),

    APACHE_0_20_2(EHadoopDistributions.APACHE, "Apache 0.20.2", "APACHE_0_20_2", false, true),

    CLOUDERA_CDH5_1(
                    EHadoopDistributions.CLOUDERA,
                    "Cloudera CDH5.1",
                    "Cloudera_CDH5_1",
                    true,
                    false,
                    new EMRVersion[] { EMRVersion.YARN }),

    CLOUDERA_CDH5(
                  EHadoopDistributions.CLOUDERA,
                  "Cloudera CDH5",
                  "Cloudera_CDH5",
                  true,
                  false,
                  new EMRVersion[] { EMRVersion.YARN }),

    CLOUDERA_CDH4_YARN(
                       EHadoopDistributions.CLOUDERA,
                       "Cloudera CDH4 YARN",
                       "Cloudera_CDH4_YARN",
                       true,
                       false,
                       new EMRVersion[] { EMRVersion.YARN }),

    CLOUDERA_CDH4(EHadoopDistributions.CLOUDERA, "Cloudera CDH4", "Cloudera_CDH4", true, false),

    CLOUDERA_CDH3(EHadoopDistributions.CLOUDERA, "Cloudera CDH3(deprecated)", "Cloudera_CDH3", false, false),

    MAPR310(EHadoopDistributions.MAPR, "MapR 3.1.0", "MAPR310", false, true),

    MAPR301(EHadoopDistributions.MAPR, "MapR 3.0.1", "MAPR301", false, true),

    MAPR213(EHadoopDistributions.MAPR, "MapR 2.1.3", "MAPR213", false, true),

    MAPR212(EHadoopDistributions.MAPR, "MapR 2.1.2", "MAPR212", false, true),

    MAPR2(EHadoopDistributions.MAPR, "MapR 2.0.0", "MAPR2", false, true),

    MAPR1(EHadoopDistributions.MAPR, "MapR 1.2.0", "MAPR1", false, true),

    APACHE_2_4_0_EMR(
                     EHadoopDistributions.AMAZON_EMR,
                     "Apache 2.4.0",
                     "APACHE_2_4_0_EMR",
                     true,
                     false,
                     new EMRVersion[] { EMRVersion.YARN }),

    APACHE_1_0_3_EMR(EHadoopDistributions.AMAZON_EMR, "Apache 1.0.3", "APACHE_1_0_3_EMR", true, false),

    MAPR_EMR(EHadoopDistributions.AMAZON_EMR, "MapR 1.2.8(deprecated)", "MapR_EMR", false, true),

    PIVOTAL_HD_2_0(
                   EHadoopDistributions.PIVOTAL_HD,
                   "Pivotal HD 2.0",
                   "PIVOTAL_HD_2_0",
                   true,
                   false,
                   new EMRVersion[] { EMRVersion.YARN }),

    PIVOTAL_HD_1_0_1(
                     EHadoopDistributions.PIVOTAL_HD,
                     "Pivotal HD 1.0.1",
                     "PIVOTAL_HD_1_0_1",
                     false,
                     false,
                     new EMRVersion[] { EMRVersion.YARN }),

    MICROSOFT_HD_INSIGHT_3_1(
                             EHadoopDistributions.MICROSOFT_HD_INSIGHT,
                             "Microsoft HD Insight 3.1",
                             "MICROSOFT_HD_INSIGHT_3_1",
                             true,
                             false,
                             new EMRVersion[] { EMRVersion.YARN }),

    CUSTOM(EHadoopDistributions.CUSTOM, "", "", false, false, new EMRVersion[] { EMRVersion.MR1, EMRVersion.YARN });

    private EHadoopDistributions distribution;

    private String versionDisplayName;

    private String versionValue;

    private boolean supportSecurity;

    private boolean supportGroup;

    private EMRVersion[] mrVersions;

    EHadoopVersion4Drivers(EHadoopDistributions distribution, String versionDisplayName, String versionValue,
            boolean supportSecurity, boolean supportGroup) {
        this(distribution, versionDisplayName, versionValue, supportSecurity, supportGroup, new EMRVersion[] { EMRVersion.MR1 });
    }

    EHadoopVersion4Drivers(EHadoopDistributions distribution, String versionDisplayName, String versionValue,
            boolean supportSecurity, boolean supportGroup, EMRVersion[] mrVersions) {
        this.distribution = distribution;
        this.versionDisplayName = versionDisplayName;
        this.versionValue = versionValue;
        this.supportSecurity = supportSecurity;
        this.supportGroup = supportGroup;
        this.mrVersions = mrVersions;
    }

    public EHadoopDistributions getDistribution() {
        return this.distribution;
    }

    public String getVersionDisplay() {
        return this.versionDisplayName;
    }

    public String getVersionValue() {
        return this.versionValue;
    }

    public EMRVersion[] getMrVersions() {
        return this.mrVersions;
    }

    public static EHadoopVersion4Drivers indexOfByVersionDisplay(String displayName) {
        return indexOf(displayName, true);
    }

    public static EHadoopVersion4Drivers indexOfByVersion(String value) {
        return indexOf(value, false);
    }

    private static EHadoopVersion4Drivers indexOf(String name, boolean display) {
        if (name != null) {
            for (EHadoopVersion4Drivers version : EHadoopVersion4Drivers.values()) {
                if (display) {
                    if (name.equalsIgnoreCase(version.getVersionDisplay())) {
                        return version;
                    }
                } else {
                    if (name.equalsIgnoreCase(version.getVersionValue())) {
                        return version;
                    }
                }
            }
        }
        return EHadoopVersion4Drivers.CUSTOM;
    }

    public static List<EHadoopVersion4Drivers> indexOfByDistribution(EHadoopDistributions distribution) {
        List<EHadoopVersion4Drivers> distribution4Versions = new ArrayList<EHadoopVersion4Drivers>();
        if (distribution != null) {
            for (EHadoopVersion4Drivers d4v : EHadoopVersion4Drivers.values()) {
                if (d4v.getDistribution().equals(distribution)) {
                    distribution4Versions.add(d4v);
                }
            }
        }
        return distribution4Versions;
    }

    public boolean isSupportSecurity() {
        return this.supportSecurity;
    }

    public boolean isSupportGroup() {
        return this.supportGroup;
    }

    public boolean isSupportMR1() {
        return ArrayUtils.contains(getMrVersions(), EMRVersion.MR1);

    }

    public boolean isSupportYARN() {
        return ArrayUtils.contains(getMrVersions(), EMRVersion.YARN);

    }

}

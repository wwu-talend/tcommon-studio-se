package org.talend.tdq.swtbot.test.report;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.talend.swtbot.test.commons.TalendSwtbotForTdq;
import org.talend.swtbot.test.commons.TalendSwtbotTdqCommon;
import org.talend.swtbot.test.commons.TalendSwtbotTdqCommon.TalendAnalysisTypeEnum;
import org.talend.swtbot.test.commons.TalendSwtbotTdqCommon.TalendItemTypeEnum;
import org.talend.swtbot.test.commons.TalendSwtbotTdqCommon.TalendMetadataTypeEnum;
import org.talend.swtbot.test.commons.TalendSwtbotTdqCommon.TalendReportDBType;
import org.talend.swtbot.test.commons.TalendSwtbotTdqCommon.TalendReportTemplate;

public class GenerateUserDefineReportDOverviewBasicMysqlTest extends TalendSwtbotForTdq{
	private final String REPORTLABEL = "REPORT_Userdefine_overview_b_mysql";
	@Before
	public void beforeClass(){
		TalendSwtbotTdqCommon.setReportDB(bot, TalendReportDBType.MySQL);
		TalendSwtbotTdqCommon.createConnection(bot, TalendMetadataTypeEnum.MYSQL);
		bot.editorByTitle(TalendMetadataTypeEnum.MYSQL.toString()+" 0.1").close();
		TalendSwtbotTdqCommon.ImportJRXMLTemplate(bot);
		TalendSwtbotTdqCommon
		.createAnalysis(bot, TalendAnalysisTypeEnum.CONNECTION,
				TalendMetadataTypeEnum.MYSQL);
		bot.editorByTitle(TalendAnalysisTypeEnum.CONNECTION.toString()+" 0.1").close();
		
	}
	@Test
	public void GenerateUserDefineReportDOverviewBasicMysql(){
		TalendSwtbotTdqCommon.createReport(bot, REPORTLABEL);
		TalendSwtbotTdqCommon.generateReport(bot, formBot, REPORTLABEL, 
				TalendReportTemplate.User_defined, "overview", 0,
				TalendAnalysisTypeEnum.CONNECTION.toString());
	
	
	}
	@After
	public void afterClass(){
		TalendSwtbotTdqCommon.deleteSource(bot, TalendItemTypeEnum.REPORT,
				REPORTLABEL);
		TalendSwtbotTdqCommon.deleteSource(bot, TalendItemTypeEnum.ANALYSIS,
				TalendAnalysisTypeEnum.CONNECTION.toString());
		TalendSwtbotTdqCommon.deleteSource(bot, TalendItemTypeEnum.METADATA,
				TalendMetadataTypeEnum.MYSQL.toString());
		
	}
}
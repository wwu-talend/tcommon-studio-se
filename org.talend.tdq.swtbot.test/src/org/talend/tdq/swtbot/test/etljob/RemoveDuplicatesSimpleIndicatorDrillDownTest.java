package org.talend.tdq.swtbot.test.etljob;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swtbot.swt.finder.matchers.WidgetOfType;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.talend.swtbot.test.commons.ContextMenuHelper;
import org.talend.swtbot.test.commons.TalendSwtbotForTdq;
import org.talend.swtbot.test.commons.TalendSwtbotTdqCommon;
import org.talend.swtbot.test.commons.TalendSwtbotTdqCommon.TalendAnalysisTypeEnum;
import org.talend.swtbot.test.commons.TalendSwtbotTdqCommon.TalendMetadataTypeEnum;
import org.talend.swtbot.test.commons.TalendSwtbotTdqCommon.TalendReportDBType;

public class RemoveDuplicatesSimpleIndicatorDrillDownTest extends TalendSwtbotForTdq{
	@Before
	public void beforeClass(){
		TalendSwtbotTdqCommon.setReportDB(bot, TalendReportDBType.MySQL);
		TalendSwtbotTdqCommon.createConnection(bot,
				TalendMetadataTypeEnum.MYSQL);
		TalendSwtbotTdqCommon
				.createAnalysis(bot, TalendAnalysisTypeEnum.COLUMN);
		
	}
	@Test
	public void removeDuplicatesSimpleIndicatorDrillDown(){
		bot.editorByTitle(TalendAnalysisTypeEnum.COLUMN.toString() + " 0.1")
		.show();
		formBot.hyperlink("Select columns to analyze").click();
		bot.waitUntil(Conditions.shellIsActive("Column Selection"));
		SWTBotShell shell = bot.shell("Column Selection");
		SWTBotTree tree = new SWTBotTree((Tree) bot.widget(WidgetOfType
				.widgetOfType(Tree.class)));
		tree.expandNode("tbi").getNode(0).expand().select("customer");
		bot.table().getTableItem("address1(varchar)").check();
		bot.button("OK").click();
		bot.waitUntil(Conditions.shellCloses(shell));
		formBot.ccomboBox(2).setSelection("Nominal");
		if (bot.editorByTitle(TalendAnalysisTypeEnum.COLUMN.toString() + " 0.1")
				.isDirty())
		bot.editorByTitle(TalendAnalysisTypeEnum.COLUMN.toString() + " 0.1")
			.save();
		formBot.hyperlink("Select indicators for each column").click();
		bot.waitUntil(Conditions.shellIsActive("Indicator Selection"));
		shell = bot.shell("Indicator Selection");
		tree = new SWTBotTree((Tree) bot.widget(WidgetOfType
				.widgetOfType(Tree.class)));
		tree.expandNode("Simple Statistics").select("Row Count").click(1);
		bot.checkBox().click();
		bot.button("OK").click();
		if (bot.editorByTitle(TalendAnalysisTypeEnum.COLUMN.toString() + " 0.1")
				.isDirty())
			bot.editorByTitle(TalendAnalysisTypeEnum.COLUMN.toString() + " 0.1")
			.save();
		bot.toolbarButtonWithTooltip("Run").click();
		try {
			shell = bot.shell("Run Analysis");
			bot.waitUntil(Conditions.shellCloses(shell));
		} catch (TimeoutException e) {
		}
		bot.cTabItem("Analysis Results").activate();
		formBot.section("Analysis Results").setFocus();
		bot.table().getTableItem("Duplicate Count").select();
		bot.sleep(10000);
		System.out.println(bot.table());
		ContextMenuHelper.clickContextMenu(bot.table(), "Remove duplicates");
		bot.sleep(10000);
		/*
		tree.expandNode("address1 (varchar)").getNode("Row Count")
			.doubleClick();
		bot.waitUntil(Conditions.shellIsActive("Indicator"));
		shell = bot.shell("Indicator");
		bot.textWithLabel("Lower threshold").setText("1");
		bot.textWithLabel("Higher threshold").setText("2");
		bot.button("Finish").click();
		 */
		
	}
	@After
	public void afterClass(){
		
	}

}
package com.kms.katalon.integration.analytics.report;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.kms.katalon.core.logging.model.TestSuiteLogRecord;
import com.kms.katalon.entity.testsuite.TestSuiteEntity;
import com.kms.katalon.execution.console.entity.ConsoleOption;
import com.kms.katalon.execution.entity.IExecutedEntity;
import com.kms.katalon.execution.entity.ReportFolder;
import com.kms.katalon.execution.entity.TestSuiteCollectionExecutedEntity;
import com.kms.katalon.execution.entity.TestSuiteExecutedEntity;
import com.kms.katalon.execution.integration.ReportIntegrationContribution;
import com.kms.katalon.execution.launcher.result.ExecutionEntityResult;
import com.kms.katalon.integration.analytics.AnalyticsComponent;
import com.kms.katalon.integration.analytics.constants.IntegrationAnalyticsMessages;
import com.kms.katalon.integration.analytics.entity.AnalyticsTestRun;
import com.kms.katalon.integration.analytics.entity.AnalyticsTracking;
import com.kms.katalon.logging.LogUtil;

public class AnalyticsReportIntegration implements ReportIntegrationContribution, AnalyticsComponent {
    
    private AnalyticsReportService reportService = new AnalyticsReportService();

    @Override
    public List<ConsoleOption<?>> getConsoleOptionList() {
        return Collections.emptyList();
    }

    @Override
    public void setArgumentValue(ConsoleOption<?> consoleOption, String argumentValue) throws Exception {
    }

    @Override
    public boolean isIntegrationActive(TestSuiteEntity testSuite) {
        try {
            return getSettingStore().isIntegrationEnabled();
        } catch (Exception e) {
            LogUtil.logError(e);
            return false;
        }
    }

    @Override
    public void uploadTestSuiteResult(TestSuiteEntity testSuite, ReportFolder reportFolder) throws Exception {
        reportService.upload(reportFolder);
    }
    
    @Override
    public void uploadTestSuiteCollectionResult(ReportFolder reportFolder) throws Exception {
    	reportService.upload(reportFolder);
    }
    
    @Override
    public void printIntegrateMessage() {
        LogUtil.printOutputLine(IntegrationAnalyticsMessages.MSG_INTEGRATE_WITH_KA);
    }
    
    @Override
    public void notifyProccess(Object event, ExecutionEntityResult result) {
    	try {
			boolean integrationActive = getSettingStore().isIntegrationEnabled();
			if (integrationActive) {
				IExecutedEntity executedEntity = result.getExecutedEntity();
				if (executedEntity instanceof TestSuiteExecutedEntity) {
					AnalyticsTestRun testRun = new AnalyticsTestRun();
					testRun.setName(result.getName());
					testRun.setSessionId(result.getSessionId());
					if (result.getTestStatusValue() != null) {
						testRun.setStatus(result.getTestStatusValue().name());
					}
					testRun.setTestSuiteId(executedEntity.getSourceId());
					testRun.setEnd(result.isEnd());
					reportService.updateExecutionProccess(testRun);
				} else if (executedEntity instanceof TestSuiteCollectionExecutedEntity) {
					AnalyticsTestRun testRun = new AnalyticsTestRun();
					testRun.setSessionId(result.getSessionId());
					testRun.setEnd(result.isEnd());
					reportService.updateExecutionProccess(testRun);
				}
			}
		} catch (Exception  e) {
			LogUtil.logError(e);
		}
    }
    
    @Override
    public void sendTrackingActivity(Long organizationId, String machineId, String sessionId, Date startTime, Date endTime, String ksVersion) {
        try {
            AnalyticsTracking trackingInfo = new AnalyticsTracking();
            trackingInfo.setMachineId(machineId);
            trackingInfo.setSessionId(sessionId);
            trackingInfo.setStartTime(startTime);
            trackingInfo.setEndTime(endTime);
            trackingInfo.setKsVersion(ksVersion);
            trackingInfo.setOrganizationId(organizationId);
            reportService.sendTrackingActivity(trackingInfo);
        } catch (Exception e) {
            LogUtil.logError(e);
        }
    }
}

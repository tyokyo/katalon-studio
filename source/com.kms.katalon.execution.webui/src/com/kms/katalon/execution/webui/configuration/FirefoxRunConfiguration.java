package com.kms.katalon.execution.webui.configuration;

import java.io.File;
import java.io.IOException;

import com.kms.katalon.core.setting.PropertySettingStoreUtil;
import com.kms.katalon.entity.testcase.TestCaseEntity;
import com.kms.katalon.entity.testsuite.TestSuiteEntity;
import com.kms.katalon.execution.webui.driver.FirefoxDriverConnector;

public class FirefoxRunConfiguration extends WebUiRunConfiguration {
    public FirefoxRunConfiguration(TestCaseEntity testCase) throws IOException {
        super(testCase, new FirefoxDriverConnector(testCase.getProject().getFolderLocation() + File.separator
                + PropertySettingStoreUtil.INTERNAL_SETTING_ROOT_FOLDLER_NAME));
    }

    public FirefoxRunConfiguration(TestSuiteEntity testSuite) throws IOException {
        super(testSuite, new FirefoxDriverConnector(testSuite.getProject().getFolderLocation() + File.separator
                + PropertySettingStoreUtil.INTERNAL_SETTING_ROOT_FOLDLER_NAME));
    }
}

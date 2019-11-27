package com.kms.katalon.core.windows.keyword.builtin

import org.openqa.selenium.WebElement

import com.kms.katalon.core.annotation.internal.Action
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.exception.StepFailedException
import com.kms.katalon.core.keyword.internal.AbstractKeyword
import com.kms.katalon.core.keyword.internal.KeywordMain
import com.kms.katalon.core.keyword.internal.SupportLevel
import com.kms.katalon.core.logging.KeywordLogger
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.testobject.WindowsTestObject
import com.kms.katalon.core.windows.driver.WindowsDriverFactory
import com.kms.katalon.core.windows.keyword.helper.WindowsActionHelper

import io.appium.java_client.windows.WindowsDriver

@Action(value = "switchToWindow")
public class SwitchToWindowKeyword extends AbstractKeyword {

    private KeywordLogger logger = KeywordLogger.getInstance(StartApplicationKeyword.class)

    @Override
    public SupportLevel getSupportLevel(Object ...params) {
        return SupportLevel.NOT_SUPPORT
    }

    @Override
    public Object execute(Object ...params) {
        WindowsTestObject windowsObject = (WindowsTestObject) params[0]
        FailureHandling flowControl = (FailureHandling) (params.length > 1 && 
            params[1] instanceof FailureHandling ? params[1] : RunConfiguration.getDefaultFailureHandling())
        switchToWindow(windowsObject, flowControl)
    }

    public WindowsDriver<WebElement> switchToWindow(WindowsTestObject windowsObject, FailureHandling flowControl) throws StepFailedException {
        return (WindowsDriver<WebElement>) KeywordMain.runKeyword({
            WindowsActionHelper.create(WindowsDriverFactory.getWindowsSession()).switchToWindow(windowsObject)
        }, flowControl)
    }
}


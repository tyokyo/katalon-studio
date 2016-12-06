package com.kms.katalon.core.mobile.keyword.builtin

import groovy.transform.CompileStatic
import io.appium.java_client.AppiumDriver
import io.appium.java_client.MobileElement
import io.appium.java_client.NetworkConnectionSetting
import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.android.AndroidKeyCode
import io.appium.java_client.ios.IOSDriver
import io.appium.java_client.remote.HideKeyboardStrategy

import java.text.MessageFormat
import java.util.concurrent.TimeUnit

import org.apache.commons.io.FileUtils
import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.transform.tailrec.VariableReplacedListener.*
import org.openqa.selenium.Dimension
import org.openqa.selenium.OutputType
import org.openqa.selenium.Point
import org.openqa.selenium.ScreenOrientation
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.WebDriverException
import org.openqa.selenium.WebElement
import org.openqa.selenium.interactions.touch.TouchActions
import org.openqa.selenium.support.ui.FluentWait

import com.google.common.base.Function
import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.exception.StepFailedException
import com.kms.katalon.core.helper.KeywordHelper
import com.kms.katalon.core.keyword.BuiltinKeywords
import com.kms.katalon.core.keyword.KeywordMain
import com.kms.katalon.core.logging.KeywordLogger
import com.kms.katalon.core.mobile.constants.StringConstants
import com.kms.katalon.core.mobile.helper.MobileCommonHelper
import com.kms.katalon.core.mobile.helper.MobileDeviceCommonHelper
import com.kms.katalon.core.mobile.helper.MobileElementCommonHelper
import com.kms.katalon.core.mobile.helper.MobileGestureCommonHelper
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.annotation.Action
import com.kms.katalon.core.mobile.keyword.MobileAbstractKeyword
import com.kms.katalon.core.keyword.SupportLevel
import com.kms.katalon.core.keyword.KeywordExecutor
import com.kms.katalon.core.mobile.keyword.*

@Action(value = "openNotifications")
public class OpenNotificationsKeyword extends MobileAbstractKeyword {

    @CompileStatic
    @Override
    public SupportLevel getSupportLevel(Object ...params) {
        return super.getSupportLevel(params)
    }

    @CompileStatic
    @Override
    public Object execute(Object ...params) {
        FailureHandling flowControl = (FailureHandling)(params.length > 0 && params[0] instanceof FailureHandling ? params[0] : RunConfiguration.getDefaultFailureHandling())
        openNotifications(flowControl)
    }

    @CompileStatic
    public void openNotifications(FailureHandling flowControl) throws StepFailedException {
        KeywordMain.runKeyword({
            AppiumDriver<?> driver = getAnyAppiumDriver()
            String context = driver.getContext()
            try {
                internalSwitchToNativeContext(driver)
                if (driver instanceof AndroidDriver) {
                    AndroidDriver androidDriver = (AndroidDriver) driver
                    Object version = androidDriver.getCapabilities().getCapability("platformVersion")
                    if (version != null && String.valueOf(version).compareTo("4.3") >= 0) {
                        ((AndroidDriver) driver).openNotifications()
                    } else {
                        MobileCommonHelper.swipe(driver, 50, 1, 50, 300)
                    }
                } else if (driver instanceof IOSDriver) {
                    MobileCommonHelper.swipe(driver, 50, 0, 50, 300)
                }
                logger.logPassed(StringConstants.KW_MSG_PASSED_OPEN_NOTIFICATIONS)
            } finally {
                driver.context(context)
            }
        }, flowControl, StringConstants.KW_MSG_CANNOT_OPEN_NOTIFICATIONS)
    }
}
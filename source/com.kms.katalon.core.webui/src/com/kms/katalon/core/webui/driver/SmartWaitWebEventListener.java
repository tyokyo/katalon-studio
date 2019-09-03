package com.kms.katalon.core.webui.driver;

import java.util.Optional;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.AbstractWebDriverEventListener;

import com.kms.katalon.core.configuration.RunConfiguration;
import com.kms.katalon.core.webui.common.internal.SmartWait;

/**
 * Event listener that triggers smart wait functionality on
 * <ul>
 * <li>before finding element</li>
 * </ul>
 *
 */
public class SmartWaitWebEventListener extends AbstractWebDriverEventListener {

    @Override
    public void beforeFindBy(By arg0, WebElement arg1, WebDriver arg2) {
        doSmartWait();
    }

    public void doSmartWait() {

        boolean localSmartWaitEnabled = (boolean) Optional
                .ofNullable(RunConfiguration.getExecutionProperties().get(RunConfiguration.LOCAL_SMART_WAIT_MODE))
                .orElse(false);

        boolean globalSmartWaitEnabled = (boolean) Optional
                .ofNullable(RunConfiguration.getExecutionProperties().get(RunConfiguration.GLOBAL_SMART_WAIT_MODE))
                .orElse(false);

        if (localSmartWaitEnabled || globalSmartWaitEnabled) {
            SmartWait.doSmartWait();
        }
    }

}
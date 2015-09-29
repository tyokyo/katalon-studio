package com.kms.katalon.execution.webui.util;

import java.io.IOException;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;

import com.kms.katalon.execution.webui.driver.ChromeDriverConnector;
import com.kms.katalon.execution.webui.driver.FirefoxDriverConnector;
import com.kms.katalon.execution.webui.driver.IEDriverConnector;
import com.kms.katalon.execution.webui.driver.SafariDriverConnector;
import com.kms.katalon.constants.PreferenceConstants;
import com.kms.katalon.core.webui.driver.WebUIDriverType;
import com.kms.katalon.execution.entity.IDriverConnector;
import com.kms.katalon.preferences.internal.ScopedPreferenceStore;

public class WebUIExecutionUtil {
    public static int getWaitForIEHanging() {
        IPreferenceStore store = (IPreferenceStore) new ScopedPreferenceStore(InstanceScope.INSTANCE,
                PreferenceConstants.WebUiPreferenceConstants.QUALIFIER);
        return store.getInt(PreferenceConstants.WebUiPreferenceConstants.EXECUTION_WAIT_FOR_IE_HANGING);
    }

    public static IDriverConnector getBrowserDriverConnector(WebUIDriverType webDriverType, String projectDirectory)
            throws IOException {
        switch (webDriverType) {
        case CHROME_DRIVER:
            return new ChromeDriverConnector(projectDirectory);
        case FIREFOX_DRIVER:
            return new FirefoxDriverConnector(projectDirectory);
        case IE_DRIVER:
            return new IEDriverConnector(projectDirectory);
        case SAFARI_DRIVER:
            return new SafariDriverConnector(projectDirectory);
        default:
            return null;
        }
    }
}

package com.kms.katalon.execution.session;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.kms.katalon.core.webui.driver.existings.ExistingRemoteWebDriver;

public class ExecutionSession {
    protected String sessionId;

    protected String remoteUrl;

    protected String driverTypeName;

    protected String logFolderPath;

    protected String title;

    protected boolean isAlive;

    protected boolean isAvailable;

    public ExecutionSession(String sessionId, String remoteUrl, String driverTypeName, String logFolderPath) {
        this.sessionId = sessionId;
        this.remoteUrl = remoteUrl;
        this.driverTypeName = driverTypeName;
        this.logFolderPath = logFolderPath;
        this.isAlive = true;
        this.isAvailable = false;
    }

    public void startWatcher() throws MalformedURLException {
        new Thread(new ExecutionSessionWatcher()).start();
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public String getDriverTypeName() {
        return driverTypeName;
    }

    public String getLogFolderPath() {
        return logFolderPath;
    }

    public String getTitle() {
        if (title == null) {
            return driverTypeName;
        }
        return title;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    private ExecutionSession geExecutionSession() {
        return this;
    }

    public void stop() {
        this.isAlive = false;
    }

    public void pause() {
        this.isAvailable = false;
    }

    public void resume() {
        this.isAvailable = true;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ExecutionSession)) {
            return false;
        }
        ExecutionSession other = (ExecutionSession) object;
        return new EqualsBuilder().append(this.sessionId, other.sessionId)
                .append(this.remoteUrl, other.remoteUrl)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(sessionId).append(remoteUrl).toHashCode();
    }

    protected class ExecutionSessionWatcher implements Runnable {
        private static final int DEFAULT_LOOP_INTERVAL = 1000;

        protected RemoteWebDriver existingDriver;

        protected RemoteWebDriver getExistingDriver() throws MalformedURLException, ConnectException {
            if (existingDriver == null) {
                existingDriver = new ExistingRemoteWebDriver(new URL(remoteUrl), sessionId);
            }
            return existingDriver;
        }

        @Override
        public void run() {
            while (isAlive) {
                try {
                    Thread.sleep(DEFAULT_LOOP_INTERVAL);
                    if (isAvailable) {
                        checkStatusAndUpdateTitle();
                    }
                } catch (InterruptedException e) {
                    // Ignore this
                } catch (Exception e) {
                    isAlive = false;
                }
            }
            ExecutionSessionSocketServer.getInstance().removeExecutionSession(geExecutionSession());
        }

        protected void checkStatusAndUpdateTitle() throws MalformedURLException, WebDriverException, ConnectException {
            title = getExistingDriver().getTitle();
        }
    }
}
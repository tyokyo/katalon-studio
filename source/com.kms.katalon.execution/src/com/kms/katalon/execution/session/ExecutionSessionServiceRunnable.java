package com.kms.katalon.execution.session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import org.apache.commons.lang3.StringUtils;

import com.kms.katalon.core.mobile.driver.MobileDriverType;
import com.kms.katalon.logging.LogUtil;

public class ExecutionSessionServiceRunnable implements Runnable {
    private Socket clientSocket;

    private ExecutionSessionSocketServer sessionServer;

    public ExecutionSessionServiceRunnable(Socket clientSocket, ExecutionSessionSocketServer sessionServer)
            throws IOException {
        super();
        this.clientSocket = clientSocket;
        this.sessionServer = sessionServer;
    }

    @Override
    public void run() {
        try (BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String sessionId = input.readLine();
            String remoteUrl = input.readLine();
            String driverTypeName = input.readLine();
            String logFolderPath = input.readLine();
            ExecutionSession executionSession = null;

            if (MobileDriverType.IOS_DRIVER.toString().equals(driverTypeName)
                    || MobileDriverType.ANDROID_DRIVER.toString().equals(driverTypeName)) {
                String remoteType = input.readLine();
                String title = input.readLine();
                if (StringUtils.isEmpty(remoteType)) {
                    executionSession = new MobileExecutionSession(title, sessionId, remoteUrl, driverTypeName,
                            logFolderPath);
                } else {
                    executionSession = new RemoteMobileExecutionSession(title, sessionId, remoteUrl, driverTypeName,
                            logFolderPath, remoteType);
                }
            } else {
                executionSession = new ExecutionSession(sessionId, remoteUrl, driverTypeName, logFolderPath);
            }
            sessionServer.addExecutionSession(executionSession);
            executionSession.startWatcher();
        } catch (Throwable e) {
            LogUtil.printAndLogError(e);
        } finally {
            if (this.clientSocket == null) {
                return;
            }
            try {
                this.clientSocket.close();
            } catch (IOException e) {
                LogUtil.logError(e);
            }
        }
    }
}

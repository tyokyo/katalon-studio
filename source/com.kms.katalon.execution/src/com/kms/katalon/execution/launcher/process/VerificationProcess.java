package com.kms.katalon.execution.launcher.process;

import com.kms.katalon.execution.logging.IOutputStream;
import com.kms.katalon.execution.logging.VerificationOutputStreamHandler;

public class VerificationProcess implements ILaunchProcess {
    
    private String testObjectId;

    private Process fSystemProcess;

    private VerificationOutputStreamHandler fOutputStreamHandler;
    private VerificationOutputStreamHandler fErrorStreamHandler;

    public VerificationProcess(String testObjectId, Process systemProcess) {
        fSystemProcess = systemProcess;
        
        this.testObjectId = testObjectId;

        buildStreamHandler(systemProcess);
    }

    private void buildStreamHandler(Process systemProcess) {
        fOutputStreamHandler = VerificationOutputStreamHandler.outputHandlerFrom(testObjectId,
                systemProcess.getInputStream());
        fOutputStreamHandler.start();

        fErrorStreamHandler = VerificationOutputStreamHandler.errorHandlerFrom(testObjectId,
                systemProcess.getErrorStream());
        fErrorStreamHandler.start();
    }

    @Override
    public int getExitValue() {
        return fSystemProcess.exitValue();
    }

    @Override
    public void terminate() {
        fSystemProcess.destroy();
    }

    @Override
    public boolean isTerminated() {
        try {
            getExitValue();
            return true;
        } catch (IllegalThreadStateException ex) {
            return false;
        }
    }

    @Override
    public IOutputStream getOutputStreamHandler() {
        return fOutputStreamHandler;
    }

    @Override
    public IOutputStream getErrorStreamHandler() {
        return fErrorStreamHandler;
    }
}
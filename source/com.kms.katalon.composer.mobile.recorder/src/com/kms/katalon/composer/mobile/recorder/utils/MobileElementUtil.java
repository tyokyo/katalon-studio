package com.kms.katalon.composer.mobile.recorder.utils;

import java.util.ArrayList;
import java.util.Map;

import com.kms.katalon.composer.mobile.objectspy.element.MobileElement;
import com.kms.katalon.controller.ObjectRepositoryController;
import com.kms.katalon.entity.folder.FolderEntity;
import com.kms.katalon.entity.repository.WebElementEntity;
import com.kms.katalon.entity.repository.WebElementPropertyEntity;
import com.kms.katalon.entity.util.Util;

public class MobileElementUtil {

    public static final String PAGE_TITLE_KEY = "title";

    public static WebElementEntity convertElementToWebElementEntity(MobileElement element, WebElementEntity refElement,
            FolderEntity parentFolder) throws Exception {
        WebElementEntity newWebElement = new WebElementEntity();
        newWebElement.setName(ObjectRepositoryController.getInstance().getAvailableWebElementName(parentFolder,
                element.getName()));
        newWebElement.setParentFolder(parentFolder);
        newWebElement.setElementGuidId(Util.generateGuid());
        newWebElement.setProject(parentFolder.getProject());
        newWebElement.setWebElementProperties(new ArrayList<WebElementPropertyEntity>());
        for (Map.Entry<String, String> entry : element.getAttributes().entrySet()) {
            WebElementPropertyEntity webElementPropertyEntity = new WebElementPropertyEntity();
            webElementPropertyEntity.setName(entry.getKey());
            webElementPropertyEntity.setValue(entry.getValue());
            newWebElement.getWebElementProperties().add(webElementPropertyEntity);
        }

        if (refElement != null) {
            WebElementPropertyEntity webElementPropertyEntity = new WebElementPropertyEntity();
            webElementPropertyEntity.setName(WebElementEntity.ref_element);
            webElementPropertyEntity.setValue(refElement.getIdForDisplay());
            webElementPropertyEntity.setIsSelected(true);
            newWebElement.getWebElementProperties().add(webElementPropertyEntity);
        }
        return newWebElement;
    }

    public static FolderEntity convertPageElementToFolderEntity(MobileElement pageElement, FolderEntity parentFolder)
            throws Exception {
        FolderEntity newFolder = new FolderEntity();
        newFolder.setName(pageElement.getName());
        newFolder.setParentFolder(parentFolder);
        newFolder.setDescription("folder");
        newFolder.setFolderType(parentFolder.getFolderType());
        newFolder.setProject(parentFolder.getProject());

        return newFolder;
    }

}
package com.kms.katalon.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.ObjectUtils;

import com.kms.katalon.entity.file.FileEntity;

public class FilterController {

    private static final List<String> DEFAULT_KEYWORDS = Arrays.asList("id", "name", "tag", "comment", "description",
            "folder");

    private static FilterController instance;

    public static FilterController getInstance() {
        if (instance == null) {
            instance = new FilterController();
        }
        return instance;
    }

    public List<String> getDefaultKeywords() {
        return DEFAULT_KEYWORDS;
    }

    public boolean isMatched(FileEntity fileEntity, String filteringText) {
        String trimmedText = filteringText.trim();
        List<String> keywordList = new ArrayList<>();
        keywordList.addAll(DEFAULT_KEYWORDS);
        Map<String, String> tagMap = parseSearchedString(keywordList.toArray(new String[0]), trimmedText);

        if (!tagMap.isEmpty()) {
            for (Entry<String, String> entry : tagMap.entrySet()) {
                String keyword = entry.getKey();
                if (DEFAULT_KEYWORDS.contains(keyword) && !compare(fileEntity, keyword, entry.getValue())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public <T extends FileEntity> List<T> filter(List<T> entities, String filteringText) {
        return entities.stream().filter(e -> isMatched(e, filteringText)).collect(Collectors.toList());
    }

    /**
     * parse searched string into a map of search tags of an entity element
     * 
     * @param element
     * is ITreeEntity
     * @return
     */
    public Map<String, String> parseSearchedString(String[] searchTags, String contentString) {
        if (searchTags != null) {
            Map<String, String> tagMap = new HashMap<String, String>();
            for (int i = 0; i < searchTags.length; i++) {
                String tagRegex = searchTags[i] + "=\\([^\\)]+\\)";
                Matcher m = Pattern.compile(tagRegex).matcher(contentString);
                while (m.find()) {
                    String tagContent = contentString.substring(m.start() + searchTags[i].length() + 2, m.end() - 1);
                    tagMap.put(searchTags[i], tagContent);
                }
            }
            return tagMap;
        } else {
            return Collections.emptyMap();
        }

    }

    public String getPropertyValue(FileEntity fileEntity, String keyword) {
        switch (keyword) {
            case "id":
                return fileEntity.getIdForDisplay();
            case "name":
                return fileEntity.getName();
            case "tag":
                return fileEntity.getTag();
            case "description":
                return fileEntity.getDescription();
            case "folder":
                return fileEntity.getParentFolder() != null ? fileEntity.getParentFolder().getIdForDisplay() : "";
            default:
                return "";
        }
    }
    
    public boolean compare(FileEntity fileEntity, String keyword, String text) {
        if (fileEntity == null || keyword == null || text == null) {
            return false;
        }
        switch (keyword) {
            case "id":
                return ObjectUtils.equals(fileEntity.getIdForDisplay(), text);
            case "name":
                return ObjectUtils.equals(fileEntity.getName(), text);
            case "tag":
                return ObjectUtils.equals(fileEntity.getTag(), text);
            case "description":
                return ObjectUtils.equals(fileEntity.getDescription(), text);
            case "folder":
                String folderId = fileEntity.getParentFolder() != null ? fileEntity.getParentFolder().getIdForDisplay() : "";
                return folderId.equals(text) || folderId.startsWith(text + "/");
            default:
                return false;
        }
    }
}

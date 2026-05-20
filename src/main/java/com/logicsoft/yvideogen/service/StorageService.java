package com.logicsoft.yvideogen.service;

import java.util.List;

/**
 * Interface for storage services to save generated content
 */
public interface StorageService {
    
    /**
     * Upload/save content to storage
     * 
     * @param content The content to save (typically JSON or text)
     * @param fileName The name of the file to save
     * @return UploadResult containing file ID, link, and other metadata
     * @throws Exception if upload fails
     */
    UploadResult uploadContent(String content, String fileName) throws Exception;
    
    /**
     * Upload scenes to storage
     * 
     * @param theme The theme for the scenes
     * @param scenes List of scene descriptions
     * @return UploadResult containing file ID, link, and other metadata
     * @throws Exception if upload fails
     */
    UploadResult uploadScenes(String theme, List<String> scenes) throws Exception;
}


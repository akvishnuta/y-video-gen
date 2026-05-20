package com.logicsoft.yvideogen.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Common result object for storage uploads across different storage implementations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadResult {
    private String fileId;
    private String fileLink;
    private String fileName;
    private String storageType;  // e.g., "GoogleDrive", "S3", etc.
    private String message;
}


package com.logicsoft.yvideogen.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to test Google Drive upload functionality")
public class GoogleDriveTestRequest {

    @Schema(description = "File name for the test upload", example = "test_file.txt")
    private String fileName;

    @Schema(description = "Content/text to upload to the file", example = "This is a test file content")
    private String fileContent;

    @Schema(description = "Optional MIME type", example = "text/plain")
    private String mimeType;
}


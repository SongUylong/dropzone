package com.dropzone.eventservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileUploadResponse {
    private String bucket;
    private String url;
    private String fileName;
    private String contentType;
    private long size;
}

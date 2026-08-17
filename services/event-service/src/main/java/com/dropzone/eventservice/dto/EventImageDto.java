package com.dropzone.eventservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventImageDto {
    private Long id;
    private String imageUrl;
    private Integer displayOrder;
}

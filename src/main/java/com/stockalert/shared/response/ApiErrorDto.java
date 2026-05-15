package com.stockalert.shared.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorDto {

    private int status;
    private String error;
    private String path;
    private Map<String, String> validationErrors;
}

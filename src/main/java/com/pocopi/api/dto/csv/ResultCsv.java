package com.pocopi.api.dto.csv;

import com.pocopi.api.controllers.ResultsController;

public record ResultCsv(
    ResultCsvType type,
    String username,
    String csv
) {
}
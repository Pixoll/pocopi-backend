package com.pocopi.api.dto.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record FormOptionUpdate(
    @Min(1)
    Integer id,

    @Size(min = 1, max = 100)
    String text
) {
}

package com.pocopi.api.dto.config;

import com.pocopi.api.models.config.HomeInfoCardModel;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InformationCardUpdate(
    @Min(1)
    Integer id,

    @NotNull
    @Size(min = HomeInfoCardModel.TITLE_MIN_LEN, max = HomeInfoCardModel.TITLE_MAX_LEN)
    String title,

    @NotNull
    @Size(min = HomeInfoCardModel.DESCRIPTION_MIN_LEN, max = HomeInfoCardModel.DESCRIPTION_MAX_LEN)
    String description,

    @NotNull
    @Min(0x000000)
    @Max(0xffffff)
    Integer color
) {
}

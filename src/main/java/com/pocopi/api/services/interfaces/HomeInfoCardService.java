package com.pocopi.api.services.interfaces;

import com.pocopi.api.dto.HomeInfoCard.PatchInformationCard;
import com.pocopi.api.models.config.HomeInfoCardModel;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface HomeInfoCardService {
    Map<String, String> processCardInformation(List<PatchInformationCard> updateInformationCards, List<Optional<File>> updateImages);
    List<HomeInfoCardModel> findAllByConfigVersion(int configId);
}

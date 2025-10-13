package com.pocopi.api.services.interfaces;

import com.pocopi.api.dto.HomeInfoCard.PatchInformationCard;
import com.pocopi.api.models.HomeInfoCardModel;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface HomeInfoCardService {
    Map<String, String> processCardInformation(List<PatchInformationCard> updateInformationCards, List<File> updateImages);
    List<HomeInfoCardModel> findAllByConfigVersion(int configId);
}

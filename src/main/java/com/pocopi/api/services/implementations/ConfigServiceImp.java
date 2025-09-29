package com.pocopi.api.services.implementations;

import com.pocopi.api.dto.Config.SingleConfigResponse;
import com.pocopi.api.models.ConfigModel;
import com.pocopi.api.repositories.ConfigRepository;
import com.pocopi.api.services.interfaces.ConfigService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ConfigServiceImp implements ConfigService {
    private final ConfigRepository configRepository;
    public ConfigServiceImp(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @Override
    public List<SingleConfigResponse> getConfigs() {
        List<ConfigModel> configModels = configRepository.findAll();
        List<SingleConfigResponse> singleConfigResponses = new ArrayList<>();
        for (ConfigModel configModel : configModels) {
            singleConfigResponses.add(
                    new SingleConfigResponse(
                            configModel.getVersion(),
                            configModel.getIcon().getId(),
                            configModel.getTitle(),
                            configModel.getSubtitle(),
                            configModel.getDescription(),
                            configModel.getInformedConsent()
                    )
            );
        }
        return singleConfigResponses;
    }
}

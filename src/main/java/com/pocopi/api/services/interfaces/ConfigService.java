package com.pocopi.api.services.interfaces;

import com.pocopi.api.dto.Config.PatchRequest;
import com.pocopi.api.dto.Config.SingleConfigResponse;
import com.pocopi.api.models.config.ConfigModel;

public interface ConfigService {
    SingleConfigResponse getLastConfig();
    ConfigModel findLastConfig();
    String processUpdatedConfig(PatchRequest request);
}

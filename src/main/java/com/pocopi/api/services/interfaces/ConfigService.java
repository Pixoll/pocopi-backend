package com.pocopi.api.services.interfaces;

import com.pocopi.api.dto.Config.PatchRequest;
import com.pocopi.api.dto.Config.PatchResponse;
import com.pocopi.api.dto.Config.SingleConfigResponse;
import com.pocopi.api.models.config.ConfigModel;

public interface ConfigService {
    SingleConfigResponse getLastConfig();
    ConfigModel findLastConfig();
    PatchResponse processUpdatedConfig(PatchRequest request);
}

package com.pocopi.api.services.interfaces;

import com.pocopi.api.dto.Config.SingleConfigResponse;
import com.pocopi.api.models.ConfigModel;

public interface ConfigService {
    SingleConfigResponse getLastConfig();
    ConfigModel findLastConfig();
}

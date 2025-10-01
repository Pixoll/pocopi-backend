package com.pocopi.api.services.interfaces;

import com.pocopi.api.dto.Config.SingleConfigResponse;

public interface ConfigService {
    SingleConfigResponse getLastConfig();
}

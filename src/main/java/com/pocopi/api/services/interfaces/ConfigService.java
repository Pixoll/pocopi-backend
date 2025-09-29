package com.pocopi.api.services.interfaces;

import com.pocopi.api.dto.Config.SingleConfigResponse;
import java.util.List;

public interface ConfigService {
    List<SingleConfigResponse> getConfigs();
}

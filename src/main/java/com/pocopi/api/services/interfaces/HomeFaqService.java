package com.pocopi.api.services.interfaces;


import com.pocopi.api.dto.HomeFaq.PatchFaq;
import com.pocopi.api.models.HomeFaqModel;

import java.util.List;
import java.util.Map;

public interface HomeFaqService {
    Map<String, String> processFaq(List<PatchFaq> updateFaqs);
    List<HomeFaqModel> findAllByConfigVersion(int configId);
}

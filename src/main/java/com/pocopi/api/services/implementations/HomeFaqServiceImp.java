package com.pocopi.api.services.implementations;

import com.pocopi.api.dto.HomeFaq.PatchFaq;
import com.pocopi.api.models.config.HomeFaqModel;
import com.pocopi.api.repositories.HomeFaqRepository;
import com.pocopi.api.services.interfaces.HomeFaqService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class HomeFaqServiceImp implements HomeFaqService {
    private final HomeFaqRepository homeFaqRepository;

    public HomeFaqServiceImp(HomeFaqRepository homeFaqRepository) {
        this.homeFaqRepository = homeFaqRepository;
    }

    @Override
    public Map<String, String> processFaq(List<PatchFaq> updateFaqs) {
        Map<String, String> results = new HashMap<>();
        List<HomeFaqModel> allExistingFaqs = homeFaqRepository.findAll();
        Map<Integer, Boolean> processedFaqs = new HashMap<>();

        for (HomeFaqModel faq : allExistingFaqs) {
            processedFaqs.put(faq.getId(), false);
        }

        int order = 0;
        for (PatchFaq patchFaq : updateFaqs) {
            if (patchFaq.id().isPresent()) {
                Integer faqId = patchFaq.id().get();
                HomeFaqModel savedFaq = homeFaqRepository.getHomeFaqModelById(faqId);

                if (savedFaq == null) {
                    results.put("faq_" + faqId, "FAQ not found");
                    order++;
                    continue;
                }

                boolean infoChanged = checkChangeByFaq(savedFaq, patchFaq);
                boolean orderChanged = savedFaq.getOrder() != order;

                if (infoChanged || orderChanged) {
                    savedFaq.setAnswer(patchFaq.answer());
                    savedFaq.setQuestion(patchFaq.question());
                    savedFaq.setOrder((byte) order);

                    homeFaqRepository.save(savedFaq);
                    results.put("faq_" + faqId, "Updated successfully");
                } else {
                    results.put("faq_" + faqId, "No changes");
                }
                processedFaqs.put(faqId, true);

            } else {
                HomeFaqModel newFaq = new HomeFaqModel();
                newFaq.setAnswer(patchFaq.answer());
                newFaq.setQuestion(patchFaq.question());
                newFaq.setOrder((byte) order);

                HomeFaqModel savedNewFaq = homeFaqRepository.save(newFaq);
                results.put("faq_new_" + order, "Created with ID: " + savedNewFaq.getId());
            }

            order++;
        }

        for (Map.Entry<Integer, Boolean> entry : processedFaqs.entrySet()) {
            if (!entry.getValue()) {
                homeFaqRepository.deleteById(entry.getKey());
                results.put("faq_" + entry.getKey(), "Deleted");
            }
        }
        return results;
    }

    @Override
    public List<HomeFaqModel> findAllByConfigVersion(int configId) {
        return homeFaqRepository.findAllByConfigVersion(configId);
    }

    private boolean checkChangeByFaq(HomeFaqModel savedFaq, PatchFaq updated) {
        return (!Objects.equals(savedFaq.getAnswer(), updated.answer()) ||
            !Objects.equals(savedFaq.getQuestion(), updated.question())
        );
    }
}

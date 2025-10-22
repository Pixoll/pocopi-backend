package com.pocopi.api.services;

import com.pocopi.api.dto.home_faq.FrequentlyAskedQuestionUpdate;
import com.pocopi.api.models.config.HomeFaqModel;
import com.pocopi.api.repositories.HomeFaqRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class HomeFaqService {
    private final HomeFaqRepository homeFaqRepository;

    public HomeFaqService(HomeFaqRepository homeFaqRepository) {
        this.homeFaqRepository = homeFaqRepository;
    }

    public Map<String, String> processFaq(List<FrequentlyAskedQuestionUpdate> updateFaqs) {
        final Map<String, String> results = new HashMap<>();
        final List<HomeFaqModel> allExistingFaqs = homeFaqRepository.findAll();
        final Map<Integer, Boolean> processedFaqs = new HashMap<>();

        for (final HomeFaqModel faq : allExistingFaqs) {
            processedFaqs.put(faq.getId(), false);
        }

        int order = 0;
        for (final FrequentlyAskedQuestionUpdate frequentlyAskedQuestionUpdate : updateFaqs) {
            if (frequentlyAskedQuestionUpdate.id().isPresent()) {
                final Integer faqId = frequentlyAskedQuestionUpdate.id().get();
                final HomeFaqModel savedFaq = homeFaqRepository.getHomeFaqModelById(faqId);

                if (savedFaq == null) {
                    results.put("faq_" + faqId, "FAQ not found");
                    order++;
                    continue;
                }

                final boolean infoChanged = checkChangeByFaq(savedFaq, frequentlyAskedQuestionUpdate);
                final boolean orderChanged = savedFaq.getOrder() != order;

                if (infoChanged || orderChanged) {
                    savedFaq.setAnswer(frequentlyAskedQuestionUpdate.answer());
                    savedFaq.setQuestion(frequentlyAskedQuestionUpdate.question());
                    savedFaq.setOrder((byte) order);

                    homeFaqRepository.save(savedFaq);
                    results.put("faq_" + faqId, "Updated successfully");
                } else {
                    results.put("faq_" + faqId, "No changes");
                }
                processedFaqs.put(faqId, true);
            } else {
                final HomeFaqModel newFaq = new HomeFaqModel();
                newFaq.setAnswer(frequentlyAskedQuestionUpdate.answer());
                newFaq.setQuestion(frequentlyAskedQuestionUpdate.question());
                newFaq.setOrder((byte) order);

                final HomeFaqModel savedNewFaq = homeFaqRepository.save(newFaq);
                results.put("faq_new_" + order, "Created with ID: " + savedNewFaq.getId());
            }

            order++;
        }

        for (final Map.Entry<Integer, Boolean> entry : processedFaqs.entrySet()) {
            if (!entry.getValue()) {
                homeFaqRepository.deleteById(entry.getKey());
                results.put("faq_" + entry.getKey(), "Deleted");
            }
        }
        return results;
    }

    public List<HomeFaqModel> findAllByConfigVersion(int configId) {
        return homeFaqRepository.findAllByConfigVersion(configId);
    }

    private boolean checkChangeByFaq(HomeFaqModel savedFaq, FrequentlyAskedQuestionUpdate updated) {
        return !Objects.equals(savedFaq.getAnswer(), updated.answer())
               || !Objects.equals(savedFaq.getQuestion(), updated.question());
    }
}

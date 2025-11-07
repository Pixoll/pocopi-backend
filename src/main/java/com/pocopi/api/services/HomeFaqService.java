package com.pocopi.api.services;

import com.pocopi.api.dto.config.FrequentlyAskedQuestion;
import com.pocopi.api.dto.config.FrequentlyAskedQuestionUpdate;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.config.HomeFaqModel;
import com.pocopi.api.repositories.HomeFaqRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
public class HomeFaqService {
    private final HomeFaqRepository homeFaqRepository;

    public HomeFaqService(HomeFaqRepository homeFaqRepository) {
        this.homeFaqRepository = homeFaqRepository;
    }

    public List<FrequentlyAskedQuestion> getFaqsByConfigVersion(int configVersion) {
        return homeFaqRepository
            .findAllByConfigVersion(configVersion)
            .stream()
            .map(faq -> new FrequentlyAskedQuestion(faq.getId(), faq.getQuestion(), faq.getAnswer()))
            .collect(Collectors.toList());
    }

    @Transactional
    public boolean updateFaqs(ConfigModel config, List<FrequentlyAskedQuestionUpdate> updateFaqs) {
        final List<HomeFaqModel> storedFaqs = homeFaqRepository.findAllByConfigVersion(config.getVersion());

        if (updateFaqs == null || updateFaqs.isEmpty()) {
            if (storedFaqs.isEmpty()) {
                return false;
            }

            homeFaqRepository.deleteAll(storedFaqs);
            return true;
        }

        final AtomicBoolean modified = new AtomicBoolean(false);
        final Map<Integer, HomeFaqModel> storedFaqsMap = new HashMap<>();
        final Map<Integer, Boolean> processedFaqs = new HashMap<>();

        for (final HomeFaqModel faq : storedFaqs) {
            storedFaqsMap.put(faq.getId(), faq);
            processedFaqs.put(faq.getId(), false);
        }

        byte order = 0;

        for (final FrequentlyAskedQuestionUpdate faqUpdate : updateFaqs) {
            final boolean isNew = faqUpdate.id() == null
                || !storedFaqsMap.containsKey(faqUpdate.id());

            if (isNew) {
                final HomeFaqModel newFaq = HomeFaqModel.builder()
                    .config(config)
                    .order(order++)
                    .question(faqUpdate.question())
                    .answer(faqUpdate.answer())
                    .build();

                homeFaqRepository.save(newFaq);
                modified.set(true);
                continue;
            }

            final int faqId = faqUpdate.id();
            final HomeFaqModel storedFaq = storedFaqsMap.get(faqId);

            processedFaqs.put(faqId, true);

            final boolean updated = !Objects.equals(storedFaq.getAnswer(), faqUpdate.answer())
                || !Objects.equals(storedFaq.getQuestion(), faqUpdate.question())
                || storedFaq.getOrder() != order;

            if (!updated) {
                order++;
                continue;
            }

            storedFaq.setAnswer(faqUpdate.answer());
            storedFaq.setQuestion(faqUpdate.question());
            storedFaq.setOrder(order++);

            homeFaqRepository.save(storedFaq);
            modified.set(true);
        }

        processedFaqs.forEach((faqId, processed) -> {
            if (processed) {
                return;
            }

            final HomeFaqModel faq = storedFaqsMap.get(faqId);

            homeFaqRepository.delete(faq);
            modified.set(true);
        });

        return modified.get();
    }
}

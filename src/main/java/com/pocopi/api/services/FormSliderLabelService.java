package com.pocopi.api.services;

import com.pocopi.api.dto.form.SliderLabelUpdate;
import com.pocopi.api.models.form.FormQuestionModel;
import com.pocopi.api.models.form.FormQuestionSliderLabelModel;
import com.pocopi.api.repositories.FormQuestionSliderLabelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class FormSliderLabelService {
    private final FormQuestionSliderLabelRepository formQuestionSliderLabelRepository;

    public FormSliderLabelService(FormQuestionSliderLabelRepository formQuestionSliderLabelRepository) {
        this.formQuestionSliderLabelRepository = formQuestionSliderLabelRepository;
    }

    @Transactional
    public boolean updateSliderLabels(
        FormQuestionModel question,
        List<SliderLabelUpdate> sliderLabelsUpdates,
        Map<Integer, FormQuestionSliderLabelModel> storedSliderLabelsMap,
        Map<Integer, Boolean> processedSliderLabels
    ) {
        if (sliderLabelsUpdates == null || sliderLabelsUpdates.isEmpty()) {
            return true;
        }

        boolean modified = false;

        for (final SliderLabelUpdate sliderLabelUpdate : sliderLabelsUpdates) {
            final boolean isNew = sliderLabelUpdate.id() == null
                || !storedSliderLabelsMap.containsKey(sliderLabelUpdate.id());

            if (isNew) {
                final FormQuestionSliderLabelModel newSliderLabel = FormQuestionSliderLabelModel.builder()
                    .formQuestion(question)
                    .number((short) sliderLabelUpdate.number())
                    .label(sliderLabelUpdate.label())
                    .build();

                formQuestionSliderLabelRepository.save(newSliderLabel);
                modified = true;
                continue;
            }

            final int sliderLabelId = sliderLabelUpdate.id();
            final FormQuestionSliderLabelModel storedSliderLabel = storedSliderLabelsMap.get(sliderLabelId);

            processedSliderLabels.put(sliderLabelId, true);

            final boolean updated = !Objects.equals(storedSliderLabel.getNumber(), sliderLabelUpdate.number())
                || !Objects.equals(storedSliderLabel.getLabel(), sliderLabelUpdate.label());

            if (!updated) {
                continue;
            }

            storedSliderLabel.setNumber((short) sliderLabelUpdate.number());
            storedSliderLabel.setLabel(sliderLabelUpdate.label());

            formQuestionSliderLabelRepository.save(storedSliderLabel);
            modified = true;
        }

        return modified;
    }
}

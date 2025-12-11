package com.pocopi.api.unit.models.form;

import com.pocopi.api.models.form.UserFormAnswerModel;
import com.pocopi.api.models.form.UserFormSubmissionModel;
import com.pocopi.api.models.form.FormQuestionModel;
import com.pocopi.api.models.form.FormQuestionOptionModel;
import com.pocopi.api.models.form.FormModel;
import com.pocopi.api.models.form.FormType;
import com.pocopi.api.models.form.FormQuestionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserFormAnswerModelTest {

    @Test
    void testBuilderAndFields() {
        FormModel form = FormModel.builder()
                .title("AnswerFormTest")
                .type(FormType.PRE)
                .build();

        FormQuestionModel question = FormQuestionModel.builder()
                .form(form)
                .order((byte)1)
                .category("General")
                .type(FormQuestionType.SELECT_ONE)
                .build();

        UserFormSubmissionModel submission = UserFormSubmissionModel.builder()
                .form(form)
                .build();

        FormQuestionOptionModel option = FormQuestionOptionModel.builder()
                .formQuestion(question)
                .order((byte)2)
                .text("Option Text")
                .build();

        Integer value = 15;
        String answer = "Custom Answer";

        UserFormAnswerModel ufa = UserFormAnswerModel.builder()
                .formSubmission(submission)
                .question(question)
                .option(option)
                .value(value)
                .answer(answer)
                .build();

        assertEquals(submission, ufa.getFormSubmission());
        assertEquals(question, ufa.getQuestion());
        assertEquals(option, ufa.getOption());
        assertEquals(value, ufa.getValue());
        assertEquals(answer, ufa.getAnswer());
        assertEquals(0, ufa.getId());
    }

    @Test
    void testDefaults() {
        FormModel form = FormModel.builder()
                .title("FormDefaults")
                .type(FormType.POST)
                .build();

        FormQuestionModel question = FormQuestionModel.builder()
                .form(form)
                .order((byte)0)
                .category("Default")
                .type(FormQuestionType.TEXT_SHORT)
                .build();

        UserFormSubmissionModel submission = UserFormSubmissionModel.builder()
                .form(form)
                .build();

        UserFormAnswerModel ufa = UserFormAnswerModel.builder()
                .formSubmission(submission)
                .question(question)
                .build();

        assertNull(ufa.getOption());
        assertNull(ufa.getValue());
        assertNull(ufa.getAnswer());
    }

    @Test
    void testSettersAndGetters() {
        UserFormAnswerModel ufa = new UserFormAnswerModel();

        FormModel form = FormModel.builder()
                .title("SetterForm")
                .type(FormType.POST)
                .build();

        FormQuestionModel question = FormQuestionModel.builder()
                .form(form)
                .order((byte)5)
                .category("SetterCategory")
                .type(FormQuestionType.TEXT_LONG)
                .build();

        UserFormSubmissionModel submission = UserFormSubmissionModel.builder()
                .form(form)
                .build();

        FormQuestionOptionModel option = FormQuestionOptionModel.builder()
                .formQuestion(question)
                .order((byte)4)
                .text("Set Option")
                .build();

        ufa.setFormSubmission(submission);
        ufa.setQuestion(question);
        ufa.setOption(option);
        ufa.setValue(888);
        ufa.setAnswer("Set by setter");

        assertEquals(submission, ufa.getFormSubmission());
        assertEquals(question, ufa.getQuestion());
        assertEquals(option, ufa.getOption());
        assertEquals(888, ufa.getValue());
        assertEquals("Set by setter", ufa.getAnswer());
    }
}

package com.pocopi.api.services.implementations;

import com.pocopi.api.dto.Form.CreateFormRequest;
import com.pocopi.api.repositories.FormRepository;
import com.pocopi.api.services.interfaces.FormService;
import org.springframework.stereotype.Service;

@Service
public class FormServiceImp implements FormService {

    private final FormRepository formRepository;

    public FormServiceImp(FormRepository formRepository) {
        this.formRepository = formRepository;
    }

    @Override
    public String createForm(CreateFormRequest request) {
        return "";
    }

}

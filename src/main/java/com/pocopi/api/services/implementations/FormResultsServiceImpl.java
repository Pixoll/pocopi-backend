package com.pocopi.api.services.implementations;

import com.pocopi.api.dto.FormResult.*;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.form.FormModel;
import com.pocopi.api.models.form.UserFormAnswerModel;
import com.pocopi.api.models.user.UserModel;
import com.pocopi.api.repositories.*;
import com.pocopi.api.services.interfaces.FormResultsService;
import com.pocopi.api.mappers.FormResultsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FormResultsServiceImpl implements FormResultsService {
    private final UserRepository userRepository;
    private final UserFormAnswerRepository userFormAnswerRepository;
    private final FormRepository formRepository;
    private final ConfigRepository configRepository;
    private final FormResultsMapper formResultsMapper;

    @Autowired
    public FormResultsServiceImpl(
            UserRepository userRepository,
            UserFormAnswerRepository userFormAnswerRepository,
            FormRepository formRepository,
            ConfigRepository configRepository,
            FormResultsMapper formResultsMapper
    ) {
        this.userRepository = userRepository;
        this.userFormAnswerRepository = userFormAnswerRepository;
        this.formRepository = formRepository;
        this.configRepository = configRepository;
        this.formResultsMapper = formResultsMapper;
    }

    @Override
    public UserFormResultsResponse getUserFormResults(int userId) {
        UserModel user = userRepository.getUserByUserId(userId);
        if (user == null) throw new IllegalArgumentException("Usuario no encontrado");

        ConfigModel config = configRepository.findLastConfig();
        if (config == null) throw new IllegalStateException("No configuration found");

        List<FormModel> forms = formRepository.findAllByConfigVersion(config.getVersion());
        Set<Integer> formIds = forms.stream().map(FormModel::getId).collect(Collectors.toSet());

        List<UserFormAnswerModel> userAnswers = userFormAnswerRepository.findAllByUser_Id(userId).stream()
                .filter(ans -> formIds.contains(ans.getQuestion().getForm().getId()))
                .toList();

        return formResultsMapper.toUserFormResultsResponse(user, userAnswers);
    }

    @Override
    public GroupFormResultsResponse getGroupFormResults(int groupId) {
        List<UserModel> users = userRepository.findAllByGroup_Id(groupId);
        if (users == null || users.isEmpty()) {
            throw new IllegalArgumentException("No users found for this group");
        }

        List<UserFormResultsResponse> userResults = users.stream()
                .map(user -> getUserFormResults(user.getId()))
                .toList();

        return formResultsMapper.toGroupFormResultsResponse(groupId, userResults);
    }
}
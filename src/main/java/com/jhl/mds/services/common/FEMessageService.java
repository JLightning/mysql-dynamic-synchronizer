package com.jhl.mds.services.common;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FEMessageService {

    private List<String> errorMessages = new ArrayList<>();

    public void addError(String errorMessage) {
        errorMessages.add(errorMessage);
    }

    public List<String> getErrorMessages() {
        List<String> tmpErrorMessages = errorMessages;
        errorMessages = new ArrayList<>();
        return tmpErrorMessages;
    }
}

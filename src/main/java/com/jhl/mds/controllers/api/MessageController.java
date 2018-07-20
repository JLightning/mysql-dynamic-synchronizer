package com.jhl.mds.controllers.api;

import com.jhl.mds.jsclientgenerator.JsClientController;
import com.jhl.mds.services.common.FEMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@JsClientController(className = "MessageApiClient", fileName = "message-api-client")
@RequestMapping("/api/message")
public class MessageController {

    private FEMessageService feMessageService;

    @Autowired
    public MessageController(FEMessageService feMessageService) {
        this.feMessageService = feMessageService;
    }

    @GetMapping("/errors")
    public List<String> getErrorMessages(HttpServletResponse response) {
        return feMessageService.getErrorMessages();
    }
}

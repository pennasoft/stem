package belfius.gejb.stem.api.task.controller;

import belfius.gejb.stem.api.task.request.CreateCorrelationRequest;
import belfius.gejb.stem.common.task.model.CorrelationView;
import belfius.gejb.stem.common.task.model.CreateCorrelationCommand;
import belfius.gejb.stem.core.task.service.CorrelationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stem/correlations")
public class CorrelationController {

    private final CorrelationService correlationService;

    public CorrelationController(CorrelationService correlationService) {
        this.correlationService = correlationService;
    }

    @PostMapping
    public ResponseEntity<CorrelationView> createCorrelation(@Valid @RequestBody CreateCorrelationRequest request) {
        CorrelationView correlation = correlationService.registerCorrelation(
                new CreateCorrelationCommand(request.reference(), request.entity(), request.type(), request.createdBy())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(correlation);
    }
}


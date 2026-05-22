package belfius.gejb.stem.core.task.service;

import belfius.gejb.stem.common.task.model.CorrelationView;
import belfius.gejb.stem.common.task.model.CreateCorrelationCommand;

public interface CorrelationService {

    CorrelationView registerCorrelation(CreateCorrelationCommand command);
}


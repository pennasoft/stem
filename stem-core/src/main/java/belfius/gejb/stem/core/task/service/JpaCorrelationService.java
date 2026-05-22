package belfius.gejb.stem.core.task.service;

import belfius.gejb.stem.common.task.model.CorrelationView;
import belfius.gejb.stem.common.task.model.CreateCorrelationCommand;
import belfius.gejb.stem.core.task.eventing.TaskModelMapper;
import belfius.gejb.stem.core.task.persistence.CorrelationEntity;
import belfius.gejb.stem.core.task.persistence.CorrelationRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

public class JpaCorrelationService implements CorrelationService {

    private final CorrelationRepository correlationRepository;
    private final Clock clock;

    public JpaCorrelationService(CorrelationRepository correlationRepository, Clock clock) {
        this.correlationRepository = correlationRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public CorrelationView registerCorrelation(CreateCorrelationCommand command) {
        if (correlationRepository.existsByReferenceAndType(command.reference(), command.type())) {
            throw new DuplicateCorrelationException("Correlation already exists for reference/type");
        }

        Instant now = Instant.now(clock);
        CorrelationEntity entity = new CorrelationEntity();
        entity.setId(UUID.randomUUID());
        entity.setReference(command.reference());
        entity.setEntityLabel(command.entity());
        entity.setType(command.type());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setCreatedBy(command.createdBy());
        try {
            return TaskModelMapper.toView(correlationRepository.save(entity));
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateCorrelationException("Correlation already exists for reference/type");
        }
    }
}


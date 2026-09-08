package com.example.ledgercore.atm.query.handler;

import com.example.ledgercore.atm.entity.AtmTerminal;
import com.example.ledgercore.atm.query.dto.AtmTerminalQuery;
import com.example.ledgercore.atm.query.dto.AtmTerminalResponse;
import com.example.ledgercore.atm.query.port.inbound.GetAtmTerminalsUseCase;
import com.example.ledgercore.atm.query.repository.AtmTerminalQueryRepository;
import com.example.ledgercore.atm.query.specification.AtmTerminalSpecification;
import com.example.ledgercore.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAtmTerminalsHandler
        implements GetAtmTerminalsUseCase {

    private final AtmTerminalQueryRepository
            atmTerminalQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AtmTerminalResponse> execute(
            AtmTerminalQuery query
    ) {
        Specification<AtmTerminal> specification =
                Specification
                        .where(
                                AtmTerminalSpecification.search(
                                        query.search()
                                )
                        )
                        .and(
                                AtmTerminalSpecification.hasStatus(
                                        query.status()
                                )
                        );

        Pageable pageable =
                PageRequest.of(
                        query.page(),
                        query.size(),
                        Sort.by(
                                Sort.Direction.ASC,
                                "terminalCode"
                        )
                );

        Page<AtmTerminal> page =
                atmTerminalQueryRepository.findAll(
                        specification,
                        pageable
                );

        return new PageResponse<AtmTerminalResponse>(
                page.getContent()
                        .stream()
                        .map(this::toResponse)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    private AtmTerminalResponse toResponse(
            AtmTerminal terminal
    ) {
        return new AtmTerminalResponse(
                terminal.getId(),
                terminal.getTerminalCode(),
                terminal.getStatus(),
                terminal.getLocation(),
                terminal.getCreatedAt(),
                terminal.getUpdatedAt()
        );
    }
}
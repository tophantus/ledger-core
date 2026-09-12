package com.example.ledgercore.interest.query.handler;

import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.interest.entity.InterestRun;
import com.example.ledgercore.interest.query.dto.GetAdminInterestRunsQuery;
import com.example.ledgercore.interest.query.dto.InterestRunResponse;
import com.example.ledgercore.interest.query.mapper.InterestRunQueryMapper;
import com.example.ledgercore.interest.query.port.inbound.GetAdminInterestRunsUseCase;
import com.example.ledgercore.interest.query.repository.InterestRunQueryRepository;
import com.example.ledgercore.interest.query.specification.InterestRunSpecifications;
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
public class GetAdminInterestRunsHandler
        implements GetAdminInterestRunsUseCase {

    private final InterestRunQueryRepository
            interestRunQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InterestRunResponse> execute(
            GetAdminInterestRunsQuery query
    ) {
        validateQuery(query);

        Pageable pageable = PageRequest.of(
                query.page(),
                query.size(),
                Sort.by(
                        Sort.Order.desc("businessDate"),
                        Sort.Order.desc("createdAt"),
                        Sort.Order.desc("id")
                )
        );

        Specification<InterestRun> specification =
                buildSpecification(query);

        Page<InterestRun> runPage =
                interestRunQueryRepository.findAll(
                        specification,
                        pageable
                );

        return new PageResponse<>(
                runPage.getContent()
                        .stream()
                        .map(InterestRunQueryMapper::toResponse)
                        .toList(),
                runPage.getNumber(),
                runPage.getSize(),
                runPage.getTotalElements(),
                runPage.getTotalPages()
        );
    }

    private Specification<InterestRun> buildSpecification(
            GetAdminInterestRunsQuery query
    ) {
        Specification<InterestRun> specification =
                (root, criteriaQuery, criteriaBuilder) ->
                        criteriaBuilder.conjunction();

        if (query.businessDate() != null) {
            specification = specification.and(
                    InterestRunSpecifications.businessDate(
                            query.businessDate()
                    )
            );
        }

        if (query.fromDate() != null) {
            specification = specification.and(
                    InterestRunSpecifications.businessDateFrom(
                            query.fromDate()
                    )
            );
        }

        if (query.toDate() != null) {
            specification = specification.and(
                    InterestRunSpecifications.businessDateTo(
                            query.toDate()
                    )
            );
        }

        if (query.runType() != null) {
            specification = specification.and(
                    InterestRunSpecifications.runType(
                            query.runType()
                    )
            );
        }

        if (query.status() != null) {
            specification = specification.and(
                    InterestRunSpecifications.status(
                            query.status()
                    )
            );
        }

        return specification;
    }

    private void validateQuery(
            GetAdminInterestRunsQuery query
    ) {
        if (query == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (query.page() < 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (query.size() <= 0
                || query.size() > 100) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (query.businessDate() != null
                && (query.fromDate() != null
                || query.toDate() != null)) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (query.fromDate() != null
                && query.toDate() != null
                && query.fromDate().isAfter(
                query.toDate()
        )) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }
}
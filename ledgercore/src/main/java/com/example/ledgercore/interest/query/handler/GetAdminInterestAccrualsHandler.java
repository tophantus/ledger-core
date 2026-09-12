package com.example.ledgercore.interest.query.handler;

import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.interest.entity.InterestAccrual;
import com.example.ledgercore.interest.query.dto.GetAdminInterestAccrualsQuery;
import com.example.ledgercore.interest.query.dto.InterestAccrualResponse;
import com.example.ledgercore.interest.query.mapper.InterestAccrualQueryMapper;
import com.example.ledgercore.interest.query.port.inbound.GetAdminInterestAccrualsUseCase;
import com.example.ledgercore.interest.query.repository.InterestAccrualQueryRepository;
import com.example.ledgercore.interest.query.specification.InterestAccrualSpecifications;
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
public class GetAdminInterestAccrualsHandler
        implements GetAdminInterestAccrualsUseCase {

    private final InterestAccrualQueryRepository
            interestAccrualQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InterestAccrualResponse> execute(
            GetAdminInterestAccrualsQuery query
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

        Specification<InterestAccrual> specification =
                buildSpecification(query);

        Page<InterestAccrual> accrualPage =
                interestAccrualQueryRepository.findAll(
                        specification,
                        pageable
                );

        return new PageResponse<>(
                accrualPage.getContent()
                        .stream()
                        .map(
                                InterestAccrualQueryMapper::toResponse
                        )
                        .toList(),
                accrualPage.getNumber(),
                accrualPage.getSize(),
                accrualPage.getTotalElements(),
                accrualPage.getTotalPages()
        );
    }

    private Specification<InterestAccrual> buildSpecification(
            GetAdminInterestAccrualsQuery query
    ) {
        Specification<InterestAccrual> specification =
                Specification.allOf();

        if (query.runId() != null) {
            specification = specification.and(
                    InterestAccrualSpecifications.runId(
                            query.runId()
                    )
            );
        }

        if (query.businessDate() != null) {
            specification = specification.and(
                    InterestAccrualSpecifications.businessDate(
                            query.businessDate()
                    )
            );
        }

        if (query.accountId() != null) {
            specification = specification.and(
                    InterestAccrualSpecifications.accountId(
                            query.accountId()
                    )
            );
        }

        if (query.currency() != null) {
            specification = specification.and(
                    InterestAccrualSpecifications.currency(
                            query.currency()
                    )
            );
        }

        return specification;
    }

    private void validateQuery(
            GetAdminInterestAccrualsQuery query
    ) {
        if (query == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (query.runId() == null
                && query.businessDate() == null) {
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
    }
}
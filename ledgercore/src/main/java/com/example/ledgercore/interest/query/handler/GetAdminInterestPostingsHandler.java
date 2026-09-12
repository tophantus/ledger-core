package com.example.ledgercore.interest.query.handler;

import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.interest.entity.InterestPosting;
import com.example.ledgercore.interest.query.dto.GetAdminInterestPostingsQuery;
import com.example.ledgercore.interest.query.dto.InterestPostingResponse;
import com.example.ledgercore.interest.query.mapper.InterestPostingQueryMapper;
import com.example.ledgercore.interest.query.port.inbound.GetAdminInterestPostingsUseCase;
import com.example.ledgercore.interest.query.repository.InterestPostingQueryRepository;
import com.example.ledgercore.interest.query.specification.InterestPostingSpecifications;
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
public class GetAdminInterestPostingsHandler
        implements GetAdminInterestPostingsUseCase {

    private final InterestPostingQueryRepository
            interestPostingQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InterestPostingResponse> execute(
            GetAdminInterestPostingsQuery query
    ) {
        validateQuery(query);

        Pageable pageable = PageRequest.of(
                query.page(),
                query.size(),
                Sort.by(
                        Sort.Order.desc("periodEnd"),
                        Sort.Order.desc("createdAt"),
                        Sort.Order.desc("id")
                )
        );

        Specification<InterestPosting> specification =
                buildSpecification(query);

        Page<InterestPosting> postingPage =
                interestPostingQueryRepository.findAll(
                        specification,
                        pageable
                );

        return new PageResponse<>(
                postingPage.getContent()
                        .stream()
                        .map(
                                InterestPostingQueryMapper::toResponse
                        )
                        .toList(),
                postingPage.getNumber(),
                postingPage.getSize(),
                postingPage.getTotalElements(),
                postingPage.getTotalPages()
        );
    }

    private Specification<InterestPosting> buildSpecification(
            GetAdminInterestPostingsQuery query
    ) {
        Specification<InterestPosting> specification =
                Specification.allOf();

        if (query.runId() != null) {
            specification = specification.and(
                    InterestPostingSpecifications.runId(
                            query.runId()
                    )
            );
        }

        if (query.businessDate() != null) {
            specification = specification.and(
                    InterestPostingSpecifications.periodEnd(
                            query.businessDate()
                    )
            );
        }

        if (query.accountId() != null) {
            specification = specification.and(
                    InterestPostingSpecifications.accountId(
                            query.accountId()
                    )
            );
        }

        return specification;
    }

    private void validateQuery(
            GetAdminInterestPostingsQuery query
    ) {
        if (query == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (query.runId() == null
                && query.businessDate() == null
                && query.accountId() == null) {
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
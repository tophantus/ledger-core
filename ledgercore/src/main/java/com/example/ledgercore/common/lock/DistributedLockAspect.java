package com.example.ledgercore.common.lock;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
@Order(Ordered.LOWEST_PRECEDENCE)
public class DistributedLockAspect {

    private final RedissonClient redissonClient;

    private final ExpressionParser parser =
            new SpelExpressionParser();

    @Around("@annotation(lock)")
    public Object execute(
            ProceedingJoinPoint joinPoint,
            DistributedLock lock
    ) throws Throwable {

        List<RLock> locks =
                buildLocks(joinPoint, lock);

        List<RLock> acquiredLocks =
                new ArrayList<>();

        boolean unlockRegistered = false;

        try {
            acquireLocks(
                    locks,
                    acquiredLocks,
                    lock
            );

            registerUnlock(acquiredLocks);
            unlockRegistered = true;

            return joinPoint.proceed();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new BusinessException(
                    ErrorCode.INTERNAL_ERROR
            );

        } finally {
            if (!unlockRegistered) {
                unlock(acquiredLocks);
            }
        }
    }

    private void acquireLocks(
            List<RLock> locks,
            List<RLock> acquiredLocks,
            DistributedLock annotation
    ) throws InterruptedException {

        try {
            for (RLock lock : locks) {

                log.debug(
                        "[LOCK] Trying to acquire key={}",
                        lock.getName()
                );

                boolean acquired =
                        lock.tryLock(
                                annotation.waitTime(),
                                -1,
                                annotation.unit()
                        );

                if (!acquired) {
                    throw new BusinessException(
                            ErrorCode.REQUEST_IN_PROGRESS
                    );
                }

                acquiredLocks.add(lock);

                log.debug(
                        "[LOCK] Acquired key={}",
                        lock.getName()
                );
            }

        } catch (InterruptedException | RuntimeException e) {
            unlock(acquiredLocks);
            throw e;

        }
    }

    private void registerUnlock(
            List<RLock> locks
    ) {
        if (!TransactionSynchronizationManager
                .isSynchronizationActive()) {

            unlock(locks);
            return;
        }

        TransactionSynchronizationManager
                .registerSynchronization(
                        new TransactionSynchronization() {

                            @Override
                            public void afterCommit() {
                                unlock(locks);
                            }

                            @Override
                            public void afterCompletion(
                                    int status
                            ) {
                                if (status
                                        != STATUS_COMMITTED) {

                                    unlock(locks);
                                }
                            }
                        }
                );
    }

    private List<RLock> buildLocks(
            ProceedingJoinPoint joinPoint,
            DistributedLock lock
    ) {
        return Arrays.stream(lock.keys())
                .map(key ->
                        lock.prefix()
                                + parseKey(
                                joinPoint,
                                key
                        )
                )
                .distinct()
                .sorted()
                .map(redissonClient::getLock)
                .toList();
    }

    private String parseKey(
            ProceedingJoinPoint joinPoint,
            String expression
    ) {
        MethodSignature signature =
                (MethodSignature)
                        joinPoint.getSignature();

        StandardEvaluationContext context =
                new StandardEvaluationContext();

        String[] parameterNames =
                signature.getParameterNames();

        Object[] arguments =
                joinPoint.getArgs();

        for (int i = 0;
             i < parameterNames.length;
             i++) {

            context.setVariable(
                    parameterNames[i],
                    arguments[i]
            );
        }

        return parser
                .parseExpression(expression)
                .getValue(
                        context,
                        String.class
                );
    }

    private void unlock(
            List<RLock> locks
    ) {
        for (RLock lock : locks) {
            unlock(lock);
        }
    }

    private void unlock(
            RLock lock
    ) {
        try {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();

                log.debug(
                        "[LOCK] Released key={}",
                        lock.getName()
                );
            }
        } catch (Exception e) {
            log.error(
                    "[LOCK] Failed to release key={}",
                    lock.getName(),
                    e
            );
        }
    }
}
package com.musinsa.schoolsystem.global.readiness;

import com.musinsa.schoolsystem.global.error.CommonErrorCode;
import com.musinsa.schoolsystem.global.exception.BusinessException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class ReadinessGuardFilter extends OncePerRequestFilter {

    private final ApplicationReadinessState readinessState;

    @Qualifier("handlerExceptionResolver")
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!readinessState.isReady()) {
            handlerExceptionResolver.resolveException(
                    request,
                    response,
                    null,
                    new BusinessException(CommonErrorCode.DATA_INITIALIZATION_IN_PROGRESS)
            );
            return;
        }

        filterChain.doFilter(request, response);
    }
}

package com.totem.food.framework.adapters.out.web.internal.config;

import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeignHeaderInterceptorTest {

    @Mock
    private RequestTemplate requestTemplate;

    @Mock
    private ServletRequestAttributes servletRequestAttributes;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private FeignHeaderInterceptor feignHeaderInterceptor;

    @Test
    void testApply() {
        //## Mock - Object and Values
        String headerName = "x-user-identifier";
        String headerValue = "user-id-123";

        //## Given
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);

        when(httpServletRequest.getHeader(headerName)).thenReturn(headerValue);
        when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);

        //## When
        feignHeaderInterceptor.apply(requestTemplate);

        //## Then
        verify(requestTemplate, times(1)).header(headerName, headerValue);
    }
}
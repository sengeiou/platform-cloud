package com.plm.platform.server.system.aspect;

import com.plm.platform.common.core.exception.PlatformException;
import com.plm.platform.common.core.utils.PlatformUtil;
import com.plm.platform.server.system.annotation.ControllerEndpoint;
import com.plm.platform.server.system.service.ILogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author crystal
 */
@Aspect
@Slf4j
@Component
@RequiredArgsConstructor
public class ControllerEndpointAspect extends BaseAspectSupport {

    private final ILogService logService;

    @Pointcut("execution(* com.plm.platform.server.system.controller.*.*(..)) && @annotation(com.plm.platform.server.system.annotation.ControllerEndpoint)")
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint point) throws PlatformException {
        Object result;
        Method targetMethod = resolveMethod(point);
        ControllerEndpoint annotation = targetMethod.getAnnotation(ControllerEndpoint.class);
        String operation = annotation.operation();
        long start = System.currentTimeMillis();
        try {
            result = point.proceed();
            if (StringUtils.isNotBlank(operation)) {
                String username = PlatformUtil.getCurrentUsername();
                String ip = PlatformUtil.getHttpServletRequestIpAddress();
                logService.saveLog(point, targetMethod, ip, operation, username, start);
            }
            return result;
        } catch (Throwable throwable) {
            log.error(throwable.getMessage(), throwable);
            String exceptionMessage = annotation.exceptionMessage();
            String message = throwable.getMessage();
            String error = PlatformUtil.containChinese(message) ? exceptionMessage + "，" + message : exceptionMessage;
            throw new PlatformException(error);
        }
    }
}




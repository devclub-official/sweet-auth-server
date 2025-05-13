package com.ptpt.authservice.aspect;

import com.ptpt.authservice.annotation.PasswordEncryption;
import com.ptpt.authservice.service.EncryptService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Modifier;
import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
public class PasswordEncryptionAspect {

    private final EncryptService encryptService;

    /**
     * Advice: Aspect의 기능을 정의한 것으로 메서드의 실행 전, 후, 예외 처리 발생 시 실행되는 코드를 의미
     * Around : 대상 “메서드” 실행 전, 후 또는 예외 발생 시에 Advice 를 실행합니다.
     *
     * @param pjp
     * @return
     * @throws Throwable
     */
    @Around("execution(* com.ptpt.authservice.controller..*.*(..))")
    public Object passwordEncryptionAspect(ProceedingJoinPoint pjp) throws Throwable {
        Arrays.stream(pjp.getArgs())
                .forEach(this::fieldEncryption);

        return pjp.proceed();
    }

    public void fieldEncryption(Object object) {
        if (ObjectUtils.isEmpty(object)) {
            return;
        }

        FieldUtils.getAllFieldsList(object.getClass())
                .stream()
                .filter(filter -> !(Modifier.isFinal(filter.getModifiers()) && Modifier.isStatic(filter.getModifiers())))
                .forEach(field -> {
                    try {
                        boolean encryptionTarget = field.isAnnotationPresent(PasswordEncryption.class);
                        if (!encryptionTarget) {
                            return;
                        }

                        Object encryptionField = FieldUtils.readField(field, object, true);
                        if (!(encryptionField instanceof String)) {
                            return;
                        }

                        String encrypted = encryptService.encrypt((String) encryptionField);
                        FieldUtils.writeField(field, object, encrypted);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
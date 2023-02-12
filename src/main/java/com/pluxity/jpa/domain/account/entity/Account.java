package com.pluxity.jpa.domain.account.entity;

import com.pluxity.jpa.domain.EntityUpdate;
import com.pluxity.jpa.domain.Updatable;
import com.pluxity.jpa.domain.UpdateField;
import com.pluxity.jpa.domain.account.constant.AccountRole;
import com.pluxity.jpa.domain.account.dto.AccountRequestDto;
import com.pluxity.jpa.domain.account.dto.AccountResponseDto;
import jakarta.persistence.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false, unique = true, length = 16)
    @Updatable2
    private String userid;

    @Column(nullable = false)
    @Updatable2
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Updatable2
    private AccountRole role;

    @Builder
    public Account(String userid, String password, AccountRole role) {
        this.userid = userid;
        this.password = password;
        this.role = role;
    }

    public void update(Account account) {
        Optional.ofNullable(account.getUserid()).ifPresent(userid -> this.userid = userid);
        Optional.ofNullable(account.getPassword()).ifPresent(password -> this.password = password);
        Optional.ofNullable(account.getRole()).ifPresent(role -> this.role = role);
    }

    public void update2(Account account){
        BeanUtils.copyProperties(this, account);
    }

    public void update3(Account account) {
        BeanWrapper wrapper = new BeanWrapperImpl(this);
        BeanWrapper accountWrapper = new BeanWrapperImpl(account);
        for (PropertyDescriptor property : wrapper.getPropertyDescriptors()) {
            if (property.getWriteMethod() != null && accountWrapper.getPropertyValue(property.getName()) != null) {
                wrapper.setPropertyValue(property.getName(), accountWrapper.getPropertyValue(property.getName()));
            }
        }
    }

    public void update4(Account account) throws Exception {
        for (Field field : getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Updatable2.class)) {
                field.setAccessible(true);
                Object value = field.get(account);
                if (value != null) {
                    field.set(this, value);
                }
            }
        }
        Set<ConstraintViolation<Account>> violations = Validation.buildDefaultValidatorFactory().getValidator().validate(this);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

}

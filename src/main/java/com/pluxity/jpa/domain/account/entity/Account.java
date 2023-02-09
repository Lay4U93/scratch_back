package com.pluxity.jpa.domain.account.entity;

import com.pluxity.jpa.domain.Updatable;
import com.pluxity.jpa.domain.account.constant.AccountRole;
import com.pluxity.jpa.domain.account.dto.AccountRequestDto;
import com.pluxity.jpa.domain.account.dto.AccountResponseDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account implements Updatable<AccountRequestDto> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false, unique = true, length = 16)
    private String userid;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountRole role;

    @Builder
    public Account(String userid, String password, AccountRole role) {
        this.userid = userid;
        this.password = password;
        this.role = role;
    }

    public static AccountResponseDto toResponseDto(Account account) {
    }

}

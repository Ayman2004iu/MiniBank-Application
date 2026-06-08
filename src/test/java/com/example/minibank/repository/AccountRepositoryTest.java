package com.example.minibank.repository;

import com.example.minibank.model.Account;
import com.example.minibank.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void findByAccountNumber_ShouldReturnAccount() {
        User user = User.builder()
                .username("testuser")
                .email("test@test.com")
                .password("password")
                .build();
        userRepository.save(user);

        Account acc = Account.builder()
                .accountNumber("MB999")
                .balance(java.math.BigDecimal.TEN)
                .owner(user)
                .build();
        accountRepository.save(acc);

        boolean exists = accountRepository.existsByAccountNumber("MB999");
        assertThat(exists).isTrue();
    }
}
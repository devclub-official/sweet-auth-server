package fast.campus.authservice.repository.auth;

import fast.campus.authservice.domain.User;
import fast.campus.authservice.entity.user.UserEntity;
import fast.campus.authservice.exception.InvalidAuthException;
import fast.campus.authservice.repository.user.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionOperations;

import java.util.Optional;

// user 와 otp repository 를 같이 불러서 사용하는 repository class 생성
// service level 에서는 이 repository 만 바라보면 된다.

@Slf4j
@Repository
@RequiredArgsConstructor
public class AuthRepository {

    private final UserJpaRepository userJpaRepository;

//    transaction 관리가 필요하기 때문에 의존성 주입이 필요함
    private final TransactionOperations readTransactionOperations;
    private final TransactionOperations writeTransactionOperations;


//    사용자를 생성하는 메서드
//    이 로직이면 service 에 Entity 객체가 넘어가기 때문에 domain 객체를 나눠주면 좋다.
//    경계 분리!
//    public UserEntity createNewUser(UserEntity user) {
    public User createNewUser(User user) {
        return writeTransactionOperations.execute(status -> {
            Optional<UserEntity> userEntityOptional = userJpaRepository.findUserEntityByEmail(user.getEmail());

            if (userEntityOptional.isPresent()) {
                throw new RuntimeException(String.format("User [%s] already exists.", user.getEmail()));
            }

            UserEntity userEntity = userJpaRepository.save(user.toEntity());

            return  userEntity.toDomain();
        });
    }

//    조회만 하는 것이기 때문에 read transaction 만 사용
    public User getUserByUserId(String email) {
        return readTransactionOperations.execute(status -> {
            return userJpaRepository.findUserEntityByEmail(email)
                    .orElseThrow(InvalidAuthException::new)
                    .toDomain();
        });
    }
}



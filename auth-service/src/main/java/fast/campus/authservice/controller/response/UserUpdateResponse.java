package fast.campus.authservice.controller.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserUpdateResponse<T> {

    boolean success;

    String code;

    String message;

    T data;
}

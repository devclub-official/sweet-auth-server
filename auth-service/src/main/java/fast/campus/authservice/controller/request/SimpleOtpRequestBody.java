package fast.campus.authservice.controller.request;

import lombok.Getter;

import java.beans.ConstructorProperties;

@Getter
public class SimpleOtpRequestBody {

    private final String userId;

    private final String otp;

//    https://kdohyeon.tistory.com/97
    @ConstructorProperties({"userId", "otp"})
    public SimpleOtpRequestBody(String userId, String otp) {
        this.userId = userId;
        this.otp = otp;
    }
}

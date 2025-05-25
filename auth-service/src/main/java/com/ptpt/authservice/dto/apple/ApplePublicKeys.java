package com.ptpt.authservice.dto.apple;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class ApplePublicKeys {
    private List<ApplePublicKey> keys;
}
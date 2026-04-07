package com.ewalletgateway.service.interfaces;

import com.ewalletgateway.api.dto.UserLookupResponse;

public interface IUserLookupService {

    UserLookupResponse lookupByEmail(String authorizationHeader, String email);
}

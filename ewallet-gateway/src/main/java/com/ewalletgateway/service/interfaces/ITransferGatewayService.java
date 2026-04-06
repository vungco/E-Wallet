package com.ewalletgateway.service.interfaces;

import com.ewalletgateway.api.dto.AcceptedTransferResponse;
import com.ewalletgateway.api.dto.CreateTransferRequest;
import com.ewalletgateway.api.dto.TransferStatusResponse;

public interface ITransferGatewayService {

    AcceptedTransferResponse createTransfer(String authorizationHeader, CreateTransferRequest body);

    TransferStatusResponse getTransferStatus(String authorizationHeader, String requestId);
}

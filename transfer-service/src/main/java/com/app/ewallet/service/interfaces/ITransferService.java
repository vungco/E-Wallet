package com.app.ewallet.service.interfaces;

import com.app.ewallet.api.dto.AcceptedTransferResponse;
import com.app.ewallet.api.dto.CreateTransferRequest;
import com.app.ewallet.api.dto.TransferStatusResponse;

public interface ITransferService {

    AcceptedTransferResponse accept(CreateTransferRequest request, String accessToken, Long authenticatedUserId);

    TransferStatusResponse getTransfer(String requestId, Long authenticatedUserId);
}

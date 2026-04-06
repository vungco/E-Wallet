package com.app.ewallet.service.interfaces;

import com.app.ewallet.controller.dto.WalletResponse;

public interface IWalletQueryService {

    /** Mỗi user một ví (unique user_id trên bảng wallet). */
    WalletResponse getWalletByUserId(Long userId);
}

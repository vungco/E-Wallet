package com.app.ewallet.grpc;

import io.grpc.Context;

public final class TransferGrpcContext {

    public static final Context.Key<Long> USER_ID = Context.key("transferGrpcUserId");

    /** JWT thuần (không có tiền tố Bearer), dùng khi gọi gRPC tới wallet-registry (GetWallet, saga, …) */
    public static final Context.Key<String> ACCESS_TOKEN = Context.key("transferGrpcAccessToken");

    private TransferGrpcContext() {
    }
}

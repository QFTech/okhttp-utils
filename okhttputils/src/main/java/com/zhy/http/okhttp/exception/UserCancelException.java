package com.zhy.http.okhttp.exception;

import java.io.IOException;

/**
 * Created by chenfeiyue on 16/5/4.
 */
public class UserCancelException extends IOException {
    public UserCancelException() {
    }

    public UserCancelException(String detailMessage) {
        super(detailMessage);
    }
}

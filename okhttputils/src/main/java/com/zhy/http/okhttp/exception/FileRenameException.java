package com.zhy.http.okhttp.exception;

import java.io.IOException;

/**
 * Created by chenfeiyue on 16/5/4.
 */
public class FileRenameException extends IOException {
    public FileRenameException() {
    }

    public FileRenameException(String detailMessage) {
        super(detailMessage);
    }
}

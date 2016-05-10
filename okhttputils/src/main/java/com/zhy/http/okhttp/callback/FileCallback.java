package com.zhy.http.okhttp.callback;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.exception.FileRenameException;
import com.zhy.http.okhttp.exception.UserCancelException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import okhttp3.Response;

/**
 * Created by zhy on 15/12/15.
 *
 * modify by chenfeiyue 16/05/04
 */
public abstract class FileCallback extends Callback<File> {
    /**
     * 目标文件存储的文件夹路径
     */
    private String destFileDir;
    /**
     * 目标文件存储的文件名
     */
    private String destFileName;

    private String tempFileName;

    private Lock lock;
    private Condition condition;
    private boolean wait = false, cancel = false;

    /**
     * 设置获取到文件信息时是否挂起线程
     * 调用次方法需要调用continueDownload 或 cancelDownload
     * see {@link #continueDownload}
     * see {@link #cancelDownload}
     * @param wait true 挂起不下载， false 继续下载(默认)
     */
    public void setNeedWait(boolean wait) {
        this.wait = wait;
        if (wait)
            initLock();
    }

    private void initLock() {
        if (lock == null) {
            lock = new ReentrantLock();
            condition = lock.newCondition();
        }
    }

    public abstract void inProgress(float progress);

    /**
     * UI Thread
     * @param length
     */
    public void getContentLength(long length) {};

    public FileCallback(String destFileDir, String destFileName) {
        this.destFileDir = destFileDir;
        this.destFileName = destFileName;
        tempFileName = destFileName + ".tmp";
    }


    @Override
    public File parseNetworkResponse(Response response) throws Exception {
        return saveFile(response);
    }

    public File saveFile(Response response) throws IOException {
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            byte[] buf = new byte[2048 * 10];
            int len = 0;
            is = response.body().byteStream();
            final long total = response.body().contentLength();

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    getContentLength(total);
                }
            };
            // 回调返回文件长度
            OkHttpUtils.getInstance().getDelivery().post(runnable);

            // 返回文件信息，暂停下载
            lock();

            // 用户取消
            if (cancel) {
                throw new UserCancelException("download cancel by user !");
            }

            // 继续下载
            long sum = 0;

            File dir = new File(destFileDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, tempFileName);
            File destFile = new File(dir, destFileName);
            fos = new FileOutputStream(file);
            ProgressRunnable progressRunnable = new ProgressRunnable();
            while ((len = is.read(buf)) != -1) {
                sum += len;
                fos.write(buf, 0, len);
                final long finalSum = sum;
                progressRunnable.setProgress(finalSum * 1.0f / total);
                OkHttpUtils.getInstance().getDelivery().post(progressRunnable);
            }
            fos.flush();

            if (destFile.exists()) {
                destFile.delete();
            }
            // 下载完成 - 将临时下载文件转成原文件
            if (file.renameTo(destFile)) {
                // 删除旧文件
                return destFile;
            }
            throw new FileRenameException("file rename failed !");

        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (fos != null) fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 线程挂起suspend
     */
    private void lock() {
        if (wait) {
            initLock();
            lock.lock();//Acqurie the lock
            try {
                condition.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * 获取文件信息，继续下载
     */
    public void continueDownload() {
        cancel = false;
        initLock();
        lock.lock();
        try {
            condition.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取文件信息，取消下载
     */
    public void cancelDownload() {
        cancel = true;
        continueDownload();
    }


    private class ProgressRunnable implements Runnable {
        float progress = 0f;

        public void setProgress(float progress) {
            this.progress = progress;
        }

        @Override
        public void run() {
            inProgress(progress);
        }
    }
}

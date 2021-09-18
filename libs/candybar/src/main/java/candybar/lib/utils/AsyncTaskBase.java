package candybar.lib.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class AsyncTaskBase {
    private boolean mCancelled = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Future<?> mFuture;

    private static final ExecutorService THREAD_POOL =
            new ThreadPoolExecutor(5, 128, 1, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>());

    protected void preRun() {
    }

    protected abstract boolean run();

    protected void postRun(boolean ok) {
    }

    protected void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    protected boolean isCancelled() {
        return mCancelled;
    }

    public void cancel(boolean mayInterruptIfRunning) {
        mCancelled = true;
        if (mFuture != null) {
            mFuture.cancel(mayInterruptIfRunning);
        }
    }

    protected AsyncTaskBase execute(ExecutorService executorService) {
        preRun();
        mFuture = executorService.submit(() -> {
            if (!mCancelled) {
                final boolean result = run();
                handler.post(() -> {
                    if (!mCancelled) postRun(result);
                });
            }
        });
        return this;
    }

    public AsyncTaskBase execute() {
        return execute(Executors.newSingleThreadExecutor());
    }

    public AsyncTaskBase executeOnThreadPool() {
        return execute(THREAD_POOL);
    }
}

package com.example.asm_final.utils;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
/** One outstanding UI action; disposed screens never receive late results. */
public final class AsyncActionRunner implements AutoCloseable {
    private final Executor worker, callback;
    private boolean running;
    private volatile boolean closed;
    public AsyncActionRunner(Executor worker, Executor callback) { this.worker=worker; this.callback=callback; }
    public <T> void submit(Callable<T> action, Consumer<T> success, Consumer<Exception> failure) {
        if(closed || running) return;
        running=true;
        worker.execute(() -> {
            T value;
            try { value=action.call(); }
            catch(Exception error) {
                callback.execute(() -> { if(!closed) { running=false; failure.accept(error); } });
                return;
            }
            callback.execute(() -> { if(!closed) { running=false; success.accept(value); } });
        });
    }
    @Override public void close() { closed=true; }
}

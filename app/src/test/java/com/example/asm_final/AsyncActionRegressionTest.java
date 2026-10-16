package com.example.asm_final;
import com.example.asm_final.utils.AsyncActionRunner;
import org.junit.Test;
import java.util.*;
import java.util.concurrent.Executor;
import static org.junit.Assert.*;
public class AsyncActionRegressionTest {
    private static class Queue implements Executor {
        final List<Runnable> tasks=new ArrayList<>();
        public void execute(Runnable task) { tasks.add(task); }
        void next() { tasks.remove(0).run(); }
    }
    @Test public void workRunsOffCallerAndDoesNotSubmitTwice() {
        Queue worker=new Queue(),ui=new Queue();int[] count={0};
        AsyncActionRunner runner=new AsyncActionRunner(worker,ui);
        runner.submit(()->{count[0]++;return true;},ok->count[0]++,error->fail());
        runner.submit(()->{count[0]++;return true;},ok->fail(),error->fail());
        assertEquals(0,count[0]);assertEquals(1,worker.tasks.size());
        worker.next();assertEquals(1,count[0]);ui.next();assertEquals(2,count[0]);
    }
    @Test public void destroyedScreenDoesNotReceiveLateResult() {
        Queue worker=new Queue(),ui=new Queue();AsyncActionRunner runner=new AsyncActionRunner(worker,ui);
        runner.submit(()->true,ok->fail("Disposed screen received a result"),error->fail());
        worker.next();runner.close();ui.next();
    }
    @Test public void failureAllowsRetry() {
        Queue worker=new Queue(),ui=new Queue();int[] failures={0};AsyncActionRunner runner=new AsyncActionRunner(worker,ui);
        runner.submit(()->{throw new IllegalStateException();},ok->fail(),error->failures[0]++);
        worker.next();ui.next();assertEquals(1,failures[0]);
        runner.submit(()->true,ok->{},error->fail());assertEquals(1,worker.tasks.size());
    }
}

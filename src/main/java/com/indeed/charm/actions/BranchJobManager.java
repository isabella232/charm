package com.indeed.charm.actions;

import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 */
public class BranchJobManager {

    private final List<BranchJob> branchJobs = Lists.newLinkedList();
    private final ExecutorService service = Executors.newFixedThreadPool(1, new ThreadFactory() {
        public Thread newThread(Runnable r) {
            return new Thread(null, r, BranchJobManager.class.getSimpleName());
        }
    });
    private final Map<Long, BranchJob> history = new MapMaker()
            .softValues()
            .makeMap();
    private final AtomicLong lastId = new AtomicLong(0);

    public void submit(BranchJob job) {
        long id = lastId.incrementAndGet();
        job.setId(id);
        Future<Boolean> future = service.submit(job);
        job.setFuture(future);
        branchJobs.add(job);
        history.put(id, job);
    }

    public List<BranchJob> getRecentJobs() {
        List<BranchJob> recent = Lists.newArrayListWithCapacity(branchJobs.size());
        ListIterator<BranchJob> jobs = branchJobs.listIterator();
        while (jobs.hasNext()) {
            BranchJob job = jobs.next();
            recent.add(job); // inactive jobs get to be returned once...
            if (job.getFuture().isDone() || job.getFuture().isCancelled()) {
                jobs.remove();
            }
        }
        return recent;
    }

    public BranchJob getJobForId(long id) {
        return history.get(id);
    }

}

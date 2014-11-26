package com.job.syn;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Handler;
import android.os.Message;

/**
 * 
 * @author Octopus
 * @Data 2014年11月24日
 * @Time 下午4:16:33
 * @Tags
 * @TODO TODO 任务调度器,负责将管理和调度所有的任务
 */
@SuppressWarnings("rawtypes")
public final class JobScheduler {
	// 开始
	static final int MESSAGE_START = 0;
	// 更新
	static final int MESSAGE_UPDATE = 1;
	// 结束
	static final int MESSAGE_END = 2;
	private final HashMap<String, JobDetail> mMap = new HashMap<String, JobDetail>();
	private static JobHandler sJobHandler = new JobHandler();
	private final String mGroupKey;
	private int mThreadCount = 0;

	private JobScheduler(String group) {
		mGroupKey = group;
	}

	private void newJobs(JobDetail job) {
		mMap.put(job.key(), job);
		checkOrNewThread();
	}

	private void checkOrNewThread() {
		if (mThreadCount < DEFAULT_THREAD_COUNT) {
			startNewJob(mGroupKey);
			mThreadCount++;
		}
	}

	private static class JobHandler extends Handler {
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			int what = msg.what;
			JobResult<?> jResult = (JobResult<?>) msg.obj;
			switch (what) {
			case MESSAGE_START:
				jResult.tccJob.onRunning();
				sExecutors.execute(jResult.tccJob);
				break;
			case MESSAGE_UPDATE:
				jResult.tccJob.onUpdate(jResult.data);
				break;
			case MESSAGE_END:
				jResult.tccJob.onResult(jResult.data);
				String group = jResult.tccJob.group();
				JobScheduler ts = sJobsMap.get(group);
				ts.mMap.remove(jResult.tccJob.key());
				startNewJob(group);
				break;
			}
		}
	}

	protected static void sendNewRequirement(int what, JobResult r) {
		Message.obtain(sJobHandler, what, r).sendToTarget();
	}

	private static ExecutorService sExecutors = Executors.newCachedThreadPool();
	private static HashMap<String, JobScheduler> sJobsMap = new HashMap<String, JobScheduler>();
	private static HashMap<String, Integer> sThreadMap = new HashMap<String, Integer>();
	private static final int DEAFAULT_THREAD_MAX_COUNT = 7;
	private static final int DEFAULT_THREAD_COUNT = 3;

	public static boolean pull(JobDetail job) {
		JobScheduler tcd = sJobsMap.get(job.group());
		if (tcd != null && job.isInIdle()) {
			tcd.mMap.remove(job.key());
			return true;
		}
		return false;
	}

	public static boolean push(JobDetail newJob) {
		JobScheduler tcd = sJobsMap.get(newJob.group());
		if (tcd == null) {
			String key = newJob.group();
			tcd = new JobScheduler(key);
			sJobsMap.put(key, tcd);
			sThreadMap.put(key, DEFAULT_THREAD_COUNT);
		}
		tcd.newJobs(newJob);
		return true;
	}

	public static JobDetail getJobInGroup(String group, String key) {
		JobScheduler tcd = sJobsMap.get(key);
		if (tcd != null)
			return tcd.mMap.get(key);
		return null;
	}

	private static boolean startNewJob(String group) {
		JobScheduler tcd = sJobsMap.get(group);
		if (tcd != null) {
			Set<String> sets = tcd.mMap.keySet();
			for (String key : sets) {
				JobDetail job = tcd.mMap.get(key);
				if (job != null && job.isInIdle()) {
					job.onReady();
					sendNewRequirement(MESSAGE_START, new JobResult<Object>(null, job));
					return true;
				}
			}
			tcd.mThreadCount = Math.max(0, tcd.mThreadCount - 1);
		}
		return false;
	}

	protected static class JobResult<DATA> {
		DATA data;
		JobDetail tccJob;

		public JobResult(DATA data, JobDetail job) {
			this.data = data;
			this.tccJob = job;
		}
	}

}

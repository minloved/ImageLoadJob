package com.job.syn;

import com.job.syn.JobScheduler.JobResult;

/**
 * 
 * @author Octopus(张宇)
 * @Data 2014年11月26日
 * @Time 下午3:39:08
 * @Tags @param <PRO> 进度条数据
 * @Tags @param <RESULT> 结果
 * @TODO TODO
 */
public abstract class JobDetail<PRO, RESULT> implements Runnable, IJobGroup {
	// 标识工作没有没调度
	private static final int SC_IDLE = 0;
	// 标识工作没有没调度
	private static final int SC_READY = 1;
	// 标识工作已经开始调度
	private static final int SC_RUNNING = 2;
	// 标识工作调度完成
	private static final int SC_DEAD = 3;

	private int mScheduleState = SC_IDLE;

	public JobDetail() {
	}

	@Override
	public void run() {
		result(work());
	}

	public final void update(PRO update) {
		JobScheduler.sendNewRequirement(JobScheduler.MESSAGE_UPDATE, new JobResult<PRO>(update, this));
	}

	private final void result(RESULT result) {
		JobScheduler.sendNewRequirement(JobScheduler.MESSAGE_END, new JobResult<RESULT>(result, this));
	}

	public final boolean abolish() {
		return JobScheduler.pull(this);
	}

	public void onReady() {
		mScheduleState = SC_READY;
	}

	public void onRunning() {
		mScheduleState = SC_RUNNING;
	}

	public void onUpdate(PRO update) {
	}

	public void onResult(RESULT result) {
		mScheduleState = SC_DEAD;
	}

	public final boolean isInIdle() {
		return mScheduleState == SC_IDLE;
	}

	public abstract RESULT work();

}

package com.job.syn;

/**
 * 
 * @author Octopus(张宇)
 * @Data 2014年11月26日
 * @Time 下午3:38:58
 * @Tags
 * @TODO TODO
 */
public interface IJobGroup {
	// 任务属于哪个组
	public String group();

	// 同一组任务不同的任务
	public String key();
}

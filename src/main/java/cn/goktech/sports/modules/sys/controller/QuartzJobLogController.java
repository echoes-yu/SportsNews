package cn.goktech.sports.modules.sys.controller;

import java.util.Map;

import cn.goktech.sports.common.entity.Page;
import cn.goktech.sports.modules.sys.service.QuartzJobLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.goktech.sports.common.annotation.SysLog;
import cn.goktech.sports.common.entity.Page;
import cn.goktech.sports.common.entity.R;
import cn.goktech.sports.modules.sys.entity.QuartzJobLogEntity;
import cn.goktech.sports.modules.sys.service.QuartzJobLogService;

/**
 * 定时任务日志
 * @author zcl<yczclcn@163.com>
 */
@RestController
@RequestMapping("/quartz/job/log")
public class QuartzJobLogController {

	@Autowired
	private QuartzJobLogService quartzJobLogService;
	
	/**
	 * 列表
	 * @param params
	 * @return
	 */
	@RequestMapping("/list")
	public Page<QuartzJobLogEntity> list(@RequestBody Map<String, Object> params) {
		return quartzJobLogService.listForPage(params);
	}
	
	/**
	 * 删除
	 * @param id
	 * @return
	 */
	@SysLog("删除定时任务日志")
	@RequestMapping("/remove")
	public R batchRemove(@RequestBody Long[] id) {
		return quartzJobLogService.batchRemove(id);
	}
	
	/**
	 * 清空
	 * @return
	 */
	@SysLog("清空定时任务日志")
	@RequestMapping("/clear")
	public R batchRemoveAll() {
		return quartzJobLogService.batchRemoveAll();
	}
	
}

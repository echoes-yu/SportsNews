package cn.goktech.sports.modules.sys.service.impl;

import cn.goktech.sports.common.entity.Page;
import cn.goktech.sports.modules.sys.dao.SysLogMapper;
import cn.goktech.sports.modules.sys.entity.SysLogEntity;
import cn.goktech.sports.modules.sys.service.SysLogService;
import cn.goktech.sports.common.entity.Page;
import cn.goktech.sports.common.entity.Query;
import cn.goktech.sports.common.entity.R;
import cn.goktech.sports.common.utils.CommonUtils;
import cn.goktech.sports.modules.sys.dao.SysLogMapper;
import cn.goktech.sports.modules.sys.entity.SysLogEntity;
import cn.goktech.sports.modules.sys.service.SysLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 系统日志
 * @author zcl<yczclcn@163.com>
 */
@Service("sysLogService")
public class SysLogServiceImpl implements SysLogService {

    @Autowired
    private SysLogMapper sysLogMapper;

    /**
     * 分页查询
     * @param params
     * @return
     */
    @Override
    public Page<SysLogEntity> listLog(Map<String, Object> params) {
        Query query = new Query(params);
        Page<SysLogEntity> page = new Page<>(query);
        sysLogMapper.listForPage(page, query);
        return page;
    }

    /**
     * 批量删除
     * @param id
     * @return
     */
    @Override
    public R batchRemove(Long[] id) {
        int count = sysLogMapper.batchRemove(id);
        return CommonUtils.msg(id, count);
    }

    /**
     * 清空
     * @return
     */
    @Override
    public R batchRemoveAll() {
        int count = sysLogMapper.batchRemoveAll();
        return CommonUtils.msg(count);
    }

}

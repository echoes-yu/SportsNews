package cn.goktech.sports.modules.sys.service;

import cn.goktech.sports.common.entity.Page;
import cn.goktech.sports.modules.sys.entity.GeneratorParamEntity;
import cn.goktech.sports.common.entity.Page;
import cn.goktech.sports.modules.sys.entity.GeneratorParamEntity;
import cn.goktech.sports.modules.sys.entity.TableEntity;

import java.util.Map;

/**
 * 代码生成器
 * @author zcl<yczclcn@163.com>
 */
public interface SysGeneratorService {

	/**
	 * 分页查询表格
	 * @param params
	 * @return
	 */
	Page<TableEntity> listTable(Map<String, Object> params);

	/**
	 * 生成代码
	 * @param params
	 * @return
	 */
	byte[] generator(GeneratorParamEntity params);
	
}

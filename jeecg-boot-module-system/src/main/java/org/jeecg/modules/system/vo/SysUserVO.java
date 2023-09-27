package org.jeecg.modules.system.vo;

import lombok.Data;
import org.jeecg.modules.system.entity.SysUser;

import java.io.Serializable;
import java.util.List;

@Data
public class SysUserVO extends SysUser{

	/**部门名称*/
	private String departs;

	/**角色名称*/
	private String roles;

	/**性别*/
	private String sexStr;

}

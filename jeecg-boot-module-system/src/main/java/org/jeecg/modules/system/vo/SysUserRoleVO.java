package org.jeecg.modules.system.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class SysUserRoleVO implements Serializable{
	private static final long serialVersionUID = 1L;

	/**部门id*/
	private String roleId;
	/**对应的用户id集合*/
	private List<String> userIdList;
	public SysUserRoleVO(String roleId, List<String> userIdList) {
		super();
		this.roleId = roleId;
		this.userIdList = userIdList;
	}


}

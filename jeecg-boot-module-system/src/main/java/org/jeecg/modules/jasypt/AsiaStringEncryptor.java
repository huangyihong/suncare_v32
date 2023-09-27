/**
 * AsiaStringEncryptor.java	  V1.0   2022年7月5日 上午10:39:18
 *
 * Copyright (c) 2022 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package org.jeecg.modules.jasypt;

import org.jasypt.encryption.StringEncryptor;

public class AsiaStringEncryptor implements StringEncryptor {

	@Override
	public String encrypt(String message) {
		return JasyptUtils.encrypt(message);
	}

	@Override
	public String decrypt(String encryptedMessage) {
		return JasyptUtils.decrypt(encryptedMessage);
	}
	
}

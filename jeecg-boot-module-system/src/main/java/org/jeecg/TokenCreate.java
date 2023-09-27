/**
 * EngineMain.java	  V1.0   2019年12月25日 下午5:45:50
 * <p>
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 * <p>
 * Modification history(By    Time    Reason):
 * <p>
 * Description:
 */

package org.jeecg;

import org.jeecg.common.system.util.JwtUtil;

public class TokenCreate {

    public static void main(String[] args) throws Exception {
//        String token = JwtUtil.sign("interface_suncarev3", "9bd71a7e8fa5da3e94b266778d83de934f0548d332e7f715", "jieshou");
        String token = JwtUtil.sign("interface_riskreport", "fb19073433b77d8ee23bcc47c3477672cf7e11f1d4d12220", "shangrao3");
        System.out.println();
        System.out.println();
        System.out.println(token);
    }

}

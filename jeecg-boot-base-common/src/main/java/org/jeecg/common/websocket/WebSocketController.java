package org.jeecg.common.websocket;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.api.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/websocket")
public class WebSocketController {

    @Autowired
    private WebSocket webSocket;


    @PostMapping("/sendUser")
    public Result<?> sendUser(@RequestParam(name = "userId") String userId, @RequestParam(name = "msg") String msg) {
		webSocket.sendOneMessage(userId, msg);
        return Result.ok();
    }

}

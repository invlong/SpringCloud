package com.springboot.admin;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Slf4j
public class DingDingMessageUtil {

    public static void sendTextMessage(String msg) {
        try {
            log.info("sendTextMessage msg is {}",msg);
            Message message = new Message();
            message.setMsgtype("text");
            message.setText(new MessageInfo(msg));
            URL url = new URL("https://oapi.dingtalk.com/robot/send?access_token=24d98d73ad9c6ddcc499158b2ba18abcc5a4f7f8574b36c7108af5a276ab112c");
            // 建立 http 连接
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Content-Type", "application/Json; charset=UTF-8");
            conn.connect();
            OutputStream out = conn.getOutputStream();
            String textMessage = JSONObject.toJSONString(message);
            byte[] data = textMessage.getBytes();
            out.write(data);
            out.flush();
            out.close();
            InputStream in = conn.getInputStream();
            byte[] readDate = new byte[in.available()];
            in.read(readDate);
            log.info("sendTextMessage readDate is {}",new String(readDate));
        } catch (Exception e) {
            log.error("sendTextMessage error",e);
        }
    }
}

class Message {
    private String msgtype;
    private MessageInfo text;

    public String getMsgtype() {
        return msgtype;
    }

    public void setMsgtype(String msgtype) {
        this.msgtype = msgtype;
    }

    public MessageInfo getText() {
        return text;
    }

    public void setText(MessageInfo text) {
        this.text = text;
    }
}

class MessageInfo {
    private String content;

    public MessageInfo(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

package com.leyou.common.vo;

import com.leyou.common.exception.LyException;
import lombok.Data;
import org.joda.time.DateTime;

@Data
public class ExceptionResult {
    private int status;
    private String massage;
    private String timestamp;

    public ExceptionResult(RuntimeException e) {
        this.status =500;
        this.massage = e.getMessage();
        this.timestamp= DateTime.now().toString("yyyy-MM-dd HH:ss:mm");
        if (e instanceof LyException){
            this.status =((LyException) e).getStatus();
        }
    }
}

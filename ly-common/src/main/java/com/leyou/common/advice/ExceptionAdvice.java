package com.leyou.common.advice;

import com.leyou.common.exception.LyException;
import com.leyou.common.vo.ExceptionResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionAdvice {
    @ExceptionHandler(LyException.class)
    public ResponseEntity<ExceptionResult> HandleException(LyException e) {
        //返回结果
        return ResponseEntity.status(e.getStatus()).body(new ExceptionResult(e));
    }
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ExceptionResult> HandleRuntimeException(RuntimeException e) {
        //返回结果
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ExceptionResult(e));
    }



}

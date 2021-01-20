package com.leyou.common.exception.controller;

import com.leyou.common.exception.pojo.ExceptionResult;
import com.leyou.common.exception.pojo.LyException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 统一异常处理类
 *   重点： @ControllerAdvice + @ExceptionHandler
 */
@ControllerAdvice // 定义当前类为异常处理类，作用：Controller所有抛出异常都会转到该类的方法上处理
public class LyExceptionController {

    /**
     * 定义异常处理方法
     */
//    @ExceptionHandler(LyException.class)   //定义该方法处理哪种异常
//    public ResponseEntity<LyException> resolveException(LyException e){
//        return ResponseEntity.status(e.getStatus()).body(e);
//    }

    /*表示当前处理器，只处理LyException异常*/
    @ExceptionHandler(LyException.class)
    public ResponseEntity<ExceptionResult> handlerLyException(LyException e){
        return ResponseEntity.status(e.getStatus()).body(new ExceptionResult(e));
    }


}

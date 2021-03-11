package tk.fishfish.dataflow.config;

import org.springframework.core.annotation.Order;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tk.fishfish.rest.ApiResult;

import java.util.stream.Collectors;

/**
 * 统一异常处理
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Order(90)
@RestControllerAdvice
public class ApplicationGlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResult<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult result = e.getBindingResult();
        String msg = result.getAllErrors().stream()
                .map(err -> {
                    if (err instanceof FieldError) {
                        FieldError fieldError = (FieldError) err;
                        return String.format("字段[%s]校验失败: %s", fieldError.getField(), fieldError.getDefaultMessage());
                    } else {
                        return String.format("字段[%s]校验失败: %s", err.getCode(), err.getDefaultMessage());
                    }
                })
                .collect(Collectors.joining("; "));
        return ApiResult.fail(400, msg);
    }

}

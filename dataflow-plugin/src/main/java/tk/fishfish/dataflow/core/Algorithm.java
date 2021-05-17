package tk.fishfish.dataflow.core;

import org.springframework.core.env.Environment;

/**
 * 算法
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
public interface Algorithm extends Transformer {

    @Override
    default void transform(Argument argument) {
        compute(argument);
    }

    /**
     * 计算
     *
     * @param argument 参数
     */
    void compute(Argument argument);

    @Override
    default void setEnv(Environment env) {
    }

}

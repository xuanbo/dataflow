package tk.fishfish.dataflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.fishfish.mybatis.enums.EnableEnumTypes;

/**
 * 入口
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@EnableEnumTypes
@SpringBootApplication
public class Bootstrap {

    public static void main(String[] args) {
        SpringApplication.run(Bootstrap.class, args);
    }

}

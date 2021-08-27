package tk.fishfish.dataflow.sdk.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 查询
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Data
public class Query<C> implements Serializable {

    /**
     * 查询条件
     */
    private C condition;

    /**
     * 分页
     */
    private PageRequest page;

    @Data
    public static class PageRequest {

        public static final Integer DEFAULT_PAGE = 1;
        public static final Integer DEFAULT_SIZE = 10;

        private Integer page;
        private Integer size;
        private Sort[] sorts;

        public PageRequest() {
            this(DEFAULT_PAGE, DEFAULT_SIZE);
        }

        public PageRequest(Integer page, Integer size) {
            this(page, size, null);
        }

        public PageRequest(Integer page, Integer size, Sort[] sorts) {
            this.page = page;
            this.size = size;
            this.sorts = sorts;
        }

    }

    @Data
    public static class Sort {

        private String name;
        private Order order;

        public static Sort asc(String name) {
            Sort sort = new Sort();
            sort.setName(name);
            sort.setOrder(Order.ASC);
            return sort;
        }

        public static Sort desc(String name) {
            Sort sort = new Sort();
            sort.setName(name);
            sort.setOrder(Order.DESC);
            return sort;
        }

    }

    public enum Order {
        ASC,
        DESC;
    }

}

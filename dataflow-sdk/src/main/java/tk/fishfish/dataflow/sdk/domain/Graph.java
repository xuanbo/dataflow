package tk.fishfish.dataflow.sdk.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 图
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Graph {

    private List<Node> nodes;

    private List<Edge> edges;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Node {

        private String id;

        private String name;

        private String text;

        private Argument argument;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Edge {

        private String from;

        private String to;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Argument {

        private Map<String, Object> input;

        private Map<String, Object> output;

    }

}

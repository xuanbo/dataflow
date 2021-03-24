package tk.fishfish.dataflow.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tk.fishfish.dataflow.entity.enums.ComponentGroup;
import tk.fishfish.json.util.JSON;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 组件
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/component")
public class ComponentController implements CommandLineRunner {

    private List<Map<String, Object>> tree;

    @GetMapping("/tree")
    public List<Map<String, Object>> tree() {
        return tree;
    }

    @Override
    public void run(String... args) throws Exception {
        String locationPattern = "classpath*:META-INF/components/*.json";
        log.info("扫描组件: {}", locationPattern);
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(locationPattern);
        Map<ComponentGroup, List<Map<String, Object>>> tree = new HashMap<>(8);
        for (Resource resource : resources) {
            log.info("组件: {}", resource.getFilename());
            byte[] bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
            Map<String, Object> component = JSON.readMap(new String(bytes));
            String groupName = component.getOrDefault("group", ComponentGroup.OTHER.name()).toString();
            ComponentGroup group = ComponentGroup.valueOf(groupName);
            List<Map<String, Object>> list = tree.computeIfAbsent(group, (key) -> new ArrayList<>());
            list.add(component);
        }
        this.tree = tree.entrySet().stream().map(entry -> {
            Map<String, Object> map = new HashMap<>(4);
            map.put("group", entry.getKey());
            map.put("children", entry.getValue());
            return map;
        }).collect(Collectors.toList());
    }

}

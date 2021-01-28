package cn.beinet.codegenerate.service;

import cn.beinet.codegenerate.util.StringHelper;
import org.springframework.stereotype.Component;

@Component
public class ControllerGenerater {

    String generate(String table, String keyType, String packageName) {
        StringBuilder sb = new StringBuilder();
        // 头部的package和import
        sb.append(getHead(table, packageName));

        // class的定义
        sb.append("@RestController\n")
                .append("public class ")
                .append(table).append("Controller {\n")
                .append(getClassBody(table, keyType))
                .append("}\n");
        return sb.toString();
    }

    private String getHead(String table, String packageName) {
        return "package " + packageName + ";\n\n" +
                "import " + packageName + ".model." + table + ";\n" +
                "import " + packageName + ".service." + table + "Service;\n" +
                "import org.springframework.web.bind.annotation.GetMapping;\n" +
                "import org.springframework.web.bind.annotation.PostMapping;\n" +
                "import org.springframework.web.bind.annotation.RequestBody;\n" +
                "import org.springframework.web.bind.annotation.RestController;\n\n" +
                "import java.util.List;\n\n";
    }

    private String getClassBody(String table, String keyType) {
        StringBuilder sb = new StringBuilder();
        String lowTable = StringHelper.lowFirstChar(table);

        // Service 层变量定义
        sb.append("    private final ").append(table).append("Service ").append(lowTable).append("Service;\n\n");

        // 构造函数定义
        sb.append("    public ").append(table).append("Controller(")
                .append(table).append("Service ").append(lowTable).append("Service")
                .append(") {\n")
                .append("        this.").append(lowTable).append("Service = ").append(lowTable).append("Service;\n")
                .append("    }\n\n");

        // findAll 方法定义
        sb.append("    @GetMapping(\"").append(lowTable).append("s\")\n")
                .append("    public List<").append(table).append("> findAll() {\n")
                .append("        return ").append(lowTable).append("Service.findAll();\n")
                .append("    }\n\n");

        // findById 方法定义
        sb.append("    @GetMapping(\"").append(lowTable).append("\")\n")
                .append("    public ").append(table).append(" findById(").append(keyType).append(" id) {\n")
                .append("        return ").append(lowTable).append("Service.findById(id);\n")
                .append("    }\n\n");

        // save 方法定义
        sb.append("    @PostMapping(\"").append(lowTable).append("\")\n")
                .append("    public ").append(table).append(" save(@RequestBody ").append(table).append(" item) {\n")
                .append("        if (item == null) {\n")
                .append("            return null;\n")
                .append("        }\n")
                .append("        return ").append(lowTable).append("Service.save(item);\n")
                .append("    }\n\n");

        return sb.toString();
    }
}

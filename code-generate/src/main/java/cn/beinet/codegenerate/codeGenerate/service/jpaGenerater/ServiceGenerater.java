package cn.beinet.codegenerate.codeGenerate.service.jpaGenerater;

import cn.beinet.codegenerate.codeGenerate.dto.GenerateDto;
import cn.beinet.codegenerate.codeGenerate.dto.GenerateResult;
import cn.beinet.codegenerate.codeGenerate.enums.GenerateType;
import cn.beinet.codegenerate.codeGenerate.service.commonGenerater.Generater;
import cn.beinet.codegenerate.model.ColumnDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ServiceGenerater implements Generater {
    @Override
    public GenerateType getType() {
        return GenerateType.JPA;
    }

    @Override
    public String getTemplateName(GenerateDto generateDto) {
        return "static/template/jpa-service.template";
    }

    @Override
    public String getTargetDirName() {
        return "service";
    }

    @Override
    public String getFullFileName(String entityName) {
        return getTargetDirName() + "/" + entityName + "Service.java";
    }

    @Override
    public GenerateResult generate(List<ColumnDto> columns, GenerateDto generateDto) {
        return null;
    }

    public String generate(String table, String keyType, String packageName) {
        StringBuilder sb = new StringBuilder();
        // 头部的package和import
        sb.append(getHead(table, packageName));

        // class的定义
        sb.append("@Service\n")
                .append("public class ")
                .append(table).append("Service {\n")
                .append(getClassBody(table, keyType))
                .append("}\n");
        return sb.toString();
    }

    private String getHead(String table, String packageName) {
        return "package " + packageName + ".service;\n\n" +
                "import " + packageName + ".model." + table + ";\n" +
                "import " + packageName + ".repository." + table + "Repository;\n" +
                "import org.springframework.stereotype.Service;\n\n" +
                "import java.util.List;\n\n";
    }

    private String getClassBody(String table, String keyType) {
        StringBuilder sb = new StringBuilder();
        String entityName = null;//getEntityName(table, dto);

        // 仓储层变量定义
        sb.append("    private final ").append(table).append("Repository ").append(entityName).append("Repository;\n\n");

        // 构造函数定义
        sb.append("    public ").append(table).append("Service(")
                .append(table).append("Repository ").append(entityName).append("Repository")
                .append(") {\n")
                .append("        this.").append(entityName).append("Repository = ").append(entityName).append("Repository;\n")
                .append("    }\n\n");

        // findAll 方法定义
        sb.append("    public List<").append(table).append("> findAll() {\n")
                .append("        return ").append(entityName).append("Repository.findAll();\n")
                .append("    }\n\n");

        // findById 方法定义
        sb.append("    public ").append(table).append(" findById(").append(keyType).append(" id) {\n")
                .append("        return ").append(entityName).append("Repository.findById(id).orElse(null);\n")
                .append("    }\n\n");

        // save 方法定义
        sb.append("    public ").append(table).append(" save(").append(table).append(" item) {\n")
                .append("        if (item == null) {\n")
                .append("            return null;\n")
                .append("        }\n")
                .append("        return ").append(entityName).append("Repository.save(item);\n")
                .append("    }\n\n");

        return sb.toString();
    }
}

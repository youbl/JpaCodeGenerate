package cn.beinet.codegenerate.codeGenerate.service.jpaGenerater;

import cn.beinet.codegenerate.codeGenerate.dto.GenerateDto;
import cn.beinet.codegenerate.codeGenerate.dto.GenerateResult;
import cn.beinet.codegenerate.codeGenerate.enums.GenerateType;
import cn.beinet.codegenerate.codeGenerate.service.commonGenerater.Generater;
import cn.beinet.codegenerate.model.ColumnDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RepositoryGenerater implements Generater {
    @Override
    public GenerateType getType() {
        return GenerateType.JPA;
    }

    @Override
    public String getTemplateName(GenerateDto generateDto) {
        return "static/template/jpa-repository.template";
    }

    @Override
    public String getTargetDirName(GenerateDto generateDto) {
        return "repository";
    }

    @Override
    public String getFullFileName(String entityName, GenerateDto generateDto) {
        return getTargetDirName(generateDto) + "/" + entityName + "Repository.java";
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
        sb.append("@Repository\n")
                .append("public interface ")
                .append(table).append("Repository ")
                .append("extends JpaRepository<").append(table).append(", ").append(keyType).append("> {\n}\n");
        return sb.toString();
    }

    private String getHead(String table, String packageName) {
        return "package " + packageName + ".repository;\n\n" +
                "import " + packageName + ".model." + table + ";\n" +
                "import org.springframework.data.jpa.repository.JpaRepository;\n" +
                "import org.springframework.stereotype.Repository;\n\n";
    }

}

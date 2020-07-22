package cn.beinet.codegenerate.service;

import cn.beinet.codegenerate.model.ColumnDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RepositoryGenerater {

    String generate(List<ColumnDto> columns, String packageName) {
        String table = columns.get(0).getTable();

        StringBuilder sb = new StringBuilder();
        // 头部的package和import
        sb.append(getHead(table, packageName));

        // class的定义
        sb.append("@Repository\n")
                .append("public interface ")
                .append(table).append("Repository ")
                .append("extends JpaRepository<").append(table).append(", ").append(getKeyType(columns)).append("> {\n}\n");
        return sb.toString();
    }

    private String getHead(String table, String packageName) {
        return "package " + packageName + ".repository;\n\n" +
                "import " + packageName + ".model." + table + ";\n" +
                "import org.springframework.data.jpa.repository.JpaRepository;\n" +
                "import org.springframework.stereotype.Repository;\n\n";
    }

    private String getKeyType(List<ColumnDto> columns) {
        for (ColumnDto column : columns) {
            if (column.isPrimaryKey()) {
                return column.getManagerType();
            }
        }
        return "Long";
    }
}

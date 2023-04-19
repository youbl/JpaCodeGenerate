package cn.beinet.codegenerate.codeGenerate.service.jpaGenerate;

import cn.beinet.codegenerate.model.ColumnDto;
import cn.beinet.codegenerate.util.StringHelper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Model的Dto类生成工具
 */
@Component
public class DtoGenerater implements Generater {

    public String generate(List<ColumnDto> columns, String packageName) {
        StringBuilder sb = new StringBuilder();
        // 头部的package和import
        sb.append(getHead(packageName));

        String table = columns.get(0).getTable();

        // class的定义
        sb.append("@Data\npublic class ").append(table).append("Dto {\n");

        // class的成员
        sb.append(getClassBody(columns));

        // mapTo Model方法
        sb.append(mapToModel(columns));

        sb.append("\n}");
        return sb.toString();
    }

    private String getClassBody(List<ColumnDto> columns) {
        StringBuilder sb = new StringBuilder();
        for (ColumnDto column : columns) {
            sb.append(getColumnDefine(column));
        }
        return sb.toString();
    }

    private String mapToModel(List<ColumnDto> columns) {
        String table = columns.get(0).getTable();

        StringBuilder sb = new StringBuilder();
        sb.append("    public ")
                .append(table)
                .append(" mapTo() {\n")
                .append("        ")
                .append(table)
                .append(" result = new ")
                .append(table)
                .append("();\n");
        for (ColumnDto column : columns) {
            String colName = StringHelper.upFirstChar(column.getColumn());
            sb.append("        ")
                    .append("result.set")
                    .append(colName)
                    .append("(")
                    .append("this.get")
                    .append(colName)
                    .append("());\n");
        }
        sb.append("        return result;\n    }");
        return sb.toString();
    }
}

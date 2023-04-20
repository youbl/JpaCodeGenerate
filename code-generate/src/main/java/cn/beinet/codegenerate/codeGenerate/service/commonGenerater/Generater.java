package cn.beinet.codegenerate.codeGenerate.service.commonGenerater;

import cn.beinet.codegenerate.codeGenerate.dto.GenerateDto;
import cn.beinet.codegenerate.codeGenerate.dto.GenerateResult;
import cn.beinet.codegenerate.codeGenerate.enums.GenerateType;
import cn.beinet.codegenerate.codeGenerate.enums.Vars;
import cn.beinet.codegenerate.model.ColumnDto;
import cn.beinet.codegenerate.util.FileHelper;
import cn.beinet.codegenerate.util.StringHelper;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Generater
 *
 * @author youbl
 * @version 1.0
 * @date 2020/11/6 16:54
 */
public interface Generater {
    /**
     * 实现类的生成类型
     *
     * @return 类型
     */
    GenerateType getType();

    /**
     * 返回当前生成器的模板文件名
     *
     * @return 文件名
     */
    String getTemplateName();

    /**
     * 返回当前生成器的目标文件写入目录
     *
     * @return 目录名
     */
    String getTargetDirName();

    /**
     * 返回目标文件的完整相对路径
     *
     * @param entityName 表名(首字母大写)
     * @return 文件名
     */
    String getFullFileName(String entityName);

    /**
     * 根据列，生成文件内容
     *
     * @param columns     表的列清单
     * @param generateDto 生成选项
     * @return 生成结果
     */
    GenerateResult generate(List<ColumnDto> columns, GenerateDto generateDto);

    /**
     * 根据表名，获取代码中使用的Entity名称
     *
     * @param table        表名
     * @param removePrefix 要移除的表名前缀
     * @return Entity名
     */
    default String getEntityName(String table, String removePrefix) {
        if (StringUtils.hasLength(removePrefix))
            table = table.replace(removePrefix, "");
        return StringHelper.upFirstChar(table);
    }

    /**
     * 获取程序包里的模板文件内容
     *
     * @return 内容
     */
    default String getTemplate() {
        String template = getTemplateName();
        if (StringUtils.hasLength(template)) {
            return FileHelper.readFileFromResources(getTemplateName());
        }
        return "";
    }

    default String getHead(String packageName) {
        return "package " + packageName + ".model;\n\n" +
                "import lombok.*;\n" +
                "import org.hibernate.annotations.DynamicInsert;\n" +  // 空值不加入生成sql
                "import org.hibernate.annotations.DynamicUpdate;\n\n";
    }


    /**
     * 获取字段在Model类里的field定义
     *
     * @param column 对应的列
     * @return 定义串
     */
    default String getColumnDefine(ColumnDto column) {
        String colName = StringHelper.lowFirstChar(column.getColumn());
        return "    private " +
                column.getEntityType() +
                ' ' +
                colName +
                ";\n\n";
    }

    default void replaceSymbol(StringBuilder sb, Vars symbol, String replace) {
        String oldStr = "{{" + symbol.getVal() + "}}";
        StringHelper.replaceAll(sb, oldStr, replace);
    }
}

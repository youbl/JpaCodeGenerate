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
 * @since 2020/11/6 16:54
 */
public interface Generater {
    /**
     * 是否需要生成
     *
     * @param generateDto 生成对象
     * @return 是否要生成
     */
    default boolean need(GenerateDto generateDto) {
        return true;
    }

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
    String getTemplateName(GenerateDto generateDto);

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
     * 根据表名，获取代码中使用的Entity名称.
     * Pascal格式返回
     *
     * @param table       表名
     * @param generateDto 要对表名做处理的条件
     * @return Entity名
     */
    default String getEntityName(String table, GenerateDto generateDto) {
        String removePrefix = generateDto.getRemovePrefix();
        if (StringUtils.hasLength(removePrefix))
            table = table.replace(removePrefix, "");

        String replaceOld = generateDto.getOldForReplace();
        String replaceNew = generateDto.getNewForReplace();
        if (StringUtils.hasLength(replaceOld) && StringUtils.hasLength(replaceNew)) {
            String regex = "[,;\\s]";
            List<String> oldArr = StringHelper.splitAndRemoveEmpty(replaceOld, regex);
            List<String> newArr = StringHelper.splitAndRemoveEmpty(replaceNew, regex);
            table = StringHelper.replaceBatch(table, oldArr, newArr);
        }
        return StringHelper.castToPascal(table);
    }

    /**
     * 根据表的列名，获取代码中使用的字段名称.
     * Camel格式返回
     *
     * @param column 列名
     * @return 字段名
     */
    default String getFieldName(String column, boolean isCamel) {
        if (isCamel)
            return StringHelper.castToCamel(column);
        return StringHelper.castToPascal(column);
    }

    /**
     * 返回第一个主键字段
     *
     * @param columns 字段列表
     * @param isCamel 返回Pascal还是camel格式
     * @return 主键字段名
     */
    default String getKeyName(List<ColumnDto> columns, boolean isCamel) {
        for (ColumnDto column : columns) {
            if (column.isPrimaryKey()) {
                return getFieldName(column.getColumn(), isCamel);
            }
        }
        return "";
    }

    /**
     * 获取程序包里的模板文件内容
     *
     * @return 内容
     */
    default String getTemplate(GenerateDto generateDto) {
        String template = getTemplateName(generateDto);
        if (StringUtils.hasLength(template)) {
            return FileHelper.readFileFromResources(template);
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
        String colName = getFieldName(column.getColumn(), true);
        return "    private " +
                column.getManagerType() +
                ' ' +
                colName +
                ";\n\n";
    }

    default void replaceSymbol(StringBuilder sb, Vars symbol, String replace) {
        if (!StringUtils.hasLength(replace))
            replace = "";
        String oldStr = "{{" + symbol.getVal() + "}}";
        StringHelper.replaceAll(sb, oldStr, replace);
    }

    /**
     * 替换模板里的开始到结束部分的内容
     *
     * @param builder 模板内容
     * @param symbol 占位符
     * @param replace 把占位符替换为什么内容
     */
    default void replaceSymbolAndInner(StringBuilder builder, Vars symbol, String replace) {
        if (!StringUtils.hasLength(replace))
            replace = "";
        String startStr = "{{start-" + symbol.getVal() + "}}";
        String endStr = "{{end-" + symbol.getVal() + "}}";
        while (StringHelper.replaceByStartAndEnd(builder, startStr, endStr, replace)) {
            // 循环到替换失败为止
        }
    }

    /**
     * 替换模板里的开始标志 和 结束标志，中间内容不处理
     *
     * @param builder 模板内容
     * @param symbol 占位符
     */
    default void removeSymbol(StringBuilder builder, Vars symbol) {
        String startStr = "{{start-" + symbol.getVal() + "}}";
        String endStr = "{{end-" + symbol.getVal() + "}}";
        StringHelper.replaceAll(builder, startStr, "");
        StringHelper.replaceAll(builder, endStr, "");
    }
}

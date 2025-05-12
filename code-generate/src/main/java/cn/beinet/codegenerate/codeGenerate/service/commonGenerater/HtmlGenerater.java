package cn.beinet.codegenerate.codeGenerate.service.commonGenerater;

import cn.beinet.codegenerate.codeGenerate.dto.GenerateDto;
import cn.beinet.codegenerate.codeGenerate.dto.GenerateResult;
import cn.beinet.codegenerate.codeGenerate.enums.GenerateType;
import cn.beinet.codegenerate.codeGenerate.enums.Vars;
import cn.beinet.codegenerate.model.ColumnDto;
import cn.beinet.codegenerate.util.StringHelper;
import cn.beinet.codegenerate.util.TimeHelper;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 基于VUE的前端编辑页面生成工具
 */
@Component
public class HtmlGenerater implements Generater {
    @Override
    public GenerateType getType() {
        return GenerateType.COMMON;
    }

    @Override
    public String getTemplateName(GenerateDto generateDto) {
        return "static/template/list_html.template";
    }

    @Override
    public String getTargetDirName(GenerateDto generateDto) {
        return "html";
    }

    @Override
    public String getFullFileName(String entityName, GenerateDto generateDto) {
        return getTargetDirName(generateDto) + "/" + StringHelper.lowFirstChar(entityName) + ".html";
    }

    @Override
    public GenerateResult generate(List<ColumnDto> columns, GenerateDto generateDto) {
        StringBuilder sb = new StringBuilder(getTemplate(generateDto));

        // 开启or关闭增删改代码
        if (generateDto.getModify() != null && !generateDto.getModify()) {
            replaceSymbolAndInner(sb, Vars.MODIFY_CONTENT, "");
        } else {
            removeSymbol(sb, Vars.MODIFY_CONTENT);
        }

        String now = TimeHelper.getNow();
        replaceSymbol(sb, Vars.DATE_TIME, now);

        String entityName = getEntityName(columns.get(0).getTable(), generateDto);
        String lowEntityName = StringHelper.lowFirstChar(entityName);
        replaceSymbol(sb, Vars.LOW_ENTITY_NAME, lowEntityName);

        String keyName = getKeyName(columns, true);
        replaceSymbol(sb, Vars.LOW_KEY_FIELD, keyName);

        SearchCond searchContent = getSearchContent(columns);
        replaceSymbol(sb, Vars.HTML_SEARCH_CONTENT, searchContent.getSearchHtmlContent());
        replaceSymbol(sb, Vars.HTML_SEARCH_DATE_VAR, searchContent.getSearchDateVueVar());
        replaceSymbol(sb, Vars.HTML_SEARCH_DATE_COMBINE, searchContent.getCombineSearchDateVar());

        replaceSymbol(sb, Vars.HTML_TABLE_CONTENT, getTableContent(columns));
        replaceSymbol(sb, Vars.HTML_EDIT_CONTENT, getEditContent(columns, keyName));

        replaceSymbol(sb, Vars.HTML_FIELDS, getFieldListForJsVar(columns));


        return new GenerateResult(getFullFileName(entityName, generateDto), sb.toString());
    }

    private String getFieldListForJsVar(List<ColumnDto> columns) {
        StringBuilder sb = new StringBuilder();
        for (ColumnDto dto : columns) {
            if (sb.length() > 0)
                sb.append(',');
            String colName = getFieldName(dto.getColumn(), true);
            sb.append("'").append(colName).append("'");
        }
        return sb.toString();
    }

    /**
     * 拼接html里的搜索条件区域内容
     *
     * @param columns 列数组
     * @return 搜索条件html
     */
    private SearchCond getSearchContent(List<ColumnDto> columns) {
        // html里搜索区域的html文本
        StringBuilder sbHtmlContent = new StringBuilder();
        // vue定义里，data区域的时间字段，用于收集el-date-picker的值
        StringBuilder sbSearchDateVueVar = new StringBuilder();
        // vue检索方法里，把data区域的时间字段，转换为搜索条件的代码
        StringBuilder sbCombineSearchDateVar = new StringBuilder();

        for (ColumnDto dto : columns) {
            if (!isSearchKey(dto.getColumn())) {
                continue;
            }
            if (sbHtmlContent.length() > 0) {
                sbHtmlContent.append("\n");
            }
            String colName = getFieldName(dto.getColumn(), true);
            String dom;
            if (dto.isLocalDateTime()) {
                String varName = "searchDates" + colName;
                if (sbSearchDateVueVar.length() > 0) {
                    sbSearchDateVueVar.append("                ");
                    sbCombineSearchDateVar.append("                ");
                }
                sbSearchDateVueVar.append(varName).append(": [],\n");
                sbCombineSearchDateVar
                        .append("if (this.")
                        .append(varName)
                        .append(" && this.")
                        .append(varName)
                        .append(".length && this.")
                        .append(varName)
                        .append(".length > 1) {\n                   ")
                        .append("this.searchCondition['")
                        .append(colName)
                        .append("Begin'] = this.")
                        .append(varName)
                        .append("[0];\n                   this.searchCondition['")
                        .append(colName)
                        .append("End'] = this.")
                        .append(varName)
                        .append("[1];\n               ")
                        .append("} else {\n                   ")

                        .append("this.searchCondition['")
                        .append(colName)
                        .append("Begin'] = null;\n                   this.searchCondition['")
                        .append(colName)
                        .append("End'] = null;\n")

                        .append("               }\n");

                dom = "            <el-date-picker size=\"mini\"\n" +
                        "                :picker-options=\"globalPickOptions\"\n" +
                        "                v-model=\"" + varName + "\"\n" +
                        "                format=\"yyyy-MM-dd HH:mm:ss\"\n" +
                        "                value-format=\"yyyy-MM-dd HH:mm:ss\"\n" +
                        "                type=\"datetimerange\"\n" +
                        "                range-separator=\"至\"\n" +
                        "                start-placeholder=\"开始日期\"\n" +
                        "                end-placeholder=\"结束日期\">\n" +
                        "            </el-date-picker>\n";
            } else if (dto.isBool()) {
                dom = "            <el-select clearable filterable placeholder=\"请选择\" v-model=\"searchCondition['" + colName + "']\" style=\"width:60px\">\n" +
                        "                <el-option label=\"是\" :value=\"1\"></el-option>\n" +
                        "                <el-option label=\"否\" :value=\"0\"></el-option>\n" +
                        "            </el-select>\n";
            } else {
                dom = "            <el-input placeholder=\"请输入\" v-model.trim=\"searchCondition['" + colName + "']\"></el-input>\n";
            }
            sbHtmlContent.append("        <el-form-item label=\"")
                    .append(colName)
                    .append("\">\n")
                    .append(dom)
                    .append("        </el-form-item>");
        }
        return new SearchCond()
                .setSearchHtmlContent(sbHtmlContent.toString())
                .setSearchDateVueVar(sbSearchDateVueVar.toString())
                .setCombineSearchDateVar(sbCombineSearchDateVar.toString());
    }

    /**
     * 是否要显示在搜索条件区域里
     *
     * @param fieldName 字段名
     * @return 是否显示
     */
    public static boolean isSearchKey(String fieldName) {
        // 在这里配置：搜索条件里不需要显示的列名
        return !("updateDate".equalsIgnoreCase(fieldName) ||
                "update_date".equalsIgnoreCase(fieldName) ||
                "updateTime".equalsIgnoreCase(fieldName) ||
                "update_time".equalsIgnoreCase(fieldName)
        );
    }

    /**
     * 拼接html里的表格展示区域内容
     *
     * @param columns 列数组
     * @return 表格列html
     */
    private String getTableContent(List<ColumnDto> columns) {
        StringBuilder sb = new StringBuilder();
        for (ColumnDto dto : columns) {
            if (!isTableShowKey(dto.getColumn())) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("\n");
            }
            String colName = getFieldName(dto.getColumn(), true);
            sb.append("        <el-table-column label=\"")
                    .append(colName)
                    .append("\" :width=\"flexColumnWidth('")
                    .append(colName)
                    .append("', dataList)\">\n")
                    .append("            <template slot-scope=\"scope\">\n")
                    .append("                <div :title=\"scope.row['")
                    .append(colName)
                    .append("']\">{{scope.row['")
                    .append(colName)
                    .append("'] ? scope.row['")
                    .append(colName)
                    .append("'] : '-'}}</div>\n")
                    .append("            </template>\n")
                    .append("        </el-table-column>");
        }
        return sb.toString();
    }

    /**
     * 是否要显示表格的列
     *
     * @param fieldName 字段名
     * @return 是否显示
     */
    private boolean isTableShowKey(String fieldName) {
        if (fieldName == null)
            return false;

        // 前端脱敏（正常要后端不返回数据）
        fieldName = fieldName.toLowerCase();
        if (fieldName.contains("password") ||
                fieldName.contains("secure"))
            return false;

        // 在这里配置：表格里不需要显示的列名
        return !("".equalsIgnoreCase(fieldName));
    }

    /**
     * 拼接html里的编辑区域内容
     *
     * @param columns 列数组
     * @param keyName 主键列名
     * @return 编辑列html
     */
    private String getEditContent(List<ColumnDto> columns, String keyName) {
        StringBuilder sb = new StringBuilder();
        for (ColumnDto dto : columns) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            String colName = getFieldName(dto.getColumn(), true);
            boolean disabled = isDisableEditKey(colName, keyName);
            // 主键在新建时，要隐藏
            String vif = dto.isPrimaryKey() ? " v-if=\"editRow['" + colName + "']\"" : "";
            String editContent;
            if (disabled) {
                editContent = "<span>{{editRow['" + colName + "']}}</span>";
            } else if (dto.isBool()) {
                editContent = getSelectEditContent(colName);
            } else if (dto.isLocalDateTime()) {
                editContent = getDateTimeEditContent(colName);
            } else {
                editContent = getInputEditContent(colName);
            }
            sb.append("            <el-form-item label=\"")
                    .append(colName)
                    .append("\" prop=\"")
                    .append(colName)
                    .append("\" label-width=\"150px\" style=\"text-align: left\"")
                    .append(vif)
                    .append(">\n")
                    .append("                ")
                    .append(editContent)
                    .append("\n")
                    .append("            </el-form-item>");
        }
        return sb.toString();
    }

    private String getSelectEditContent(String colName) {
        return "<el-select clearable filterable placeholder=\"请选择\" v-model=\"editRow['" + colName + "']\" style=\"width:60px\">\n" +
                "                <el-option label=\"是\" :value=\"1\"></el-option>\n" +
                "                <el-option label=\"否\" :value=\"0\"></el-option>\n" +
                "            </el-select>";
    }

    private String getDateTimeEditContent(String colName) {
        return "<el-date-picker size=\"mini\"\n" +
                "                            v-model=\"editRow['" + colName + "']\"\n" +
                "                            format=\"yyyy-MM-dd HH:mm:ss\"\n" +
                "                            value-format=\"yyyy-MM-dd HH:mm:ss\"\n" +
                "                            placeholder=\"请选择\"\n" +
                "                            type=\"datetime\">\n" +
                "            </el-date-picker>";
    }

    private String getInputEditContent(String colName) {
        return "<el-input placeholder=\"请输入\" v-model.trim=\"editRow['" +
                colName +
                "']\"></el-input>";
    }

    /**
     * 是否在编辑界面禁止编辑的列
     *
     * @param fieldName 字段名
     * @param keyName   主键字段名
     * @return 是否禁止编辑
     */
    private boolean isDisableEditKey(String fieldName, String keyName) {
        if (fieldName == null)
            return false;
        // 在这里配置：编辑窗口里需要显示，但是只读的列名
        return ((keyName != null && keyName.equalsIgnoreCase(fieldName)) ||
                "createDate".equalsIgnoreCase(fieldName) ||
                "updateDate".equalsIgnoreCase(fieldName) ||
                "create_date".equalsIgnoreCase(fieldName) ||
                "update_date".equalsIgnoreCase(fieldName) ||
                "createTime".equalsIgnoreCase(fieldName) ||
                "updateTime".equalsIgnoreCase(fieldName) ||
                "create_time".equalsIgnoreCase(fieldName) ||
                "update_time".equalsIgnoreCase(fieldName));
    }

    @Data
    @Accessors(chain = true)
    private static class SearchCond {
        private String searchHtmlContent;
        private String searchDateVueVar;
        private String combineSearchDateVar;
    }
}

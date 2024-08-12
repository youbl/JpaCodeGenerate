package cn.beinet.codegenerate.codeGenerate.service.commonGenerater;

import cn.beinet.codegenerate.codeGenerate.dto.GenerateDto;
import cn.beinet.codegenerate.codeGenerate.dto.GenerateResult;
import cn.beinet.codegenerate.codeGenerate.enums.GenerateType;
import cn.beinet.codegenerate.codeGenerate.enums.Vars;
import cn.beinet.codegenerate.model.ColumnDto;
import cn.beinet.codegenerate.util.StringHelper;
import cn.beinet.codegenerate.util.TimeHelper;
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
    public String getTargetDirName() {
        return "html";
    }

    @Override
    public String getFullFileName(String entityName) {
        return getTargetDirName() + "/" + StringHelper.lowFirstChar(entityName) + ".html";
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

        replaceSymbol(sb, Vars.HTML_SEARCH_CONTENT, getSearchContent(columns));
        replaceSymbol(sb, Vars.HTML_TABLE_CONTENT, getTableContent(columns));
        replaceSymbol(sb, Vars.HTML_EDIT_CONTENT, getEditContent(columns, keyName));

        replaceSymbol(sb, Vars.HTML_FIELDS, getFieldListForJsVar(columns));


        return new GenerateResult(getFullFileName(entityName), sb.toString());
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
    private String getSearchContent(List<ColumnDto> columns) {
        StringBuilder sb = new StringBuilder();
        for (ColumnDto dto : columns) {
            if (!isSearchKey(dto.getColumn())) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("\n");
            }
            String colName = getFieldName(dto.getColumn(), true);
            sb.append("        <el-form-item label=\"")
                    .append(colName)
                    .append("\">\n")
                    .append("            <el-input placeholder=\"请输入\" v-model.trim=\"searchCondition['")
                    .append(colName)
                    .append("']\"></el-input>\n")
                    .append("        </el-form-item>");
        }
        return sb.toString();
    }

    /**
     * 是否要显示在搜索条件区域里
     *
     * @param fieldName 字段名
     * @return 是否显示
     */
    private boolean isSearchKey(String fieldName) {
        // 在这里配置：搜索条件里不需要显示的列名
        return !("createDate".equalsIgnoreCase(fieldName) ||
                "updateDate".equalsIgnoreCase(fieldName) ||
                "create_date".equalsIgnoreCase(fieldName) ||
                "update_date".equalsIgnoreCase(fieldName) ||
                "createTime".equalsIgnoreCase(fieldName) ||
                "updateTime".equalsIgnoreCase(fieldName) ||
                "create_time".equalsIgnoreCase(fieldName) ||
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
                    .append("            <template slot-scope=\"scope\">{{scope.row['")
                    .append(colName)
                    .append("']}}</template>\n")
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
            String disable = isDisableEditKey(colName, keyName) ? " disabled" : "";
            // 主键在新建时，要隐藏
            String vif = dto.isPrimaryKey() ? " v-if=\"editRow['" + colName + "']\"" : "";
            sb.append("            <el-form-item label=\"")
                    .append(colName)
                    .append("\" label-width=\"150px\"")
                    .append(vif)
                    .append(">\n")
                    .append("                <el-input placeholder=\"请输入\" v-model.trim=\"editRow['")
                    .append(colName)
                    .append("']\"")
                    .append(disable)
                    .append("></el-input>\n")
                    .append("            </el-form-item>");
        }
        return sb.toString();
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
}

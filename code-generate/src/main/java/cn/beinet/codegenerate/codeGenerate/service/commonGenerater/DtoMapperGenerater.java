package cn.beinet.codegenerate.codeGenerate.service.commonGenerater;

import cn.beinet.codegenerate.codeGenerate.dto.GenerateDto;
import cn.beinet.codegenerate.codeGenerate.dto.GenerateResult;
import cn.beinet.codegenerate.codeGenerate.enums.GenerateType;
import cn.beinet.codegenerate.codeGenerate.enums.Vars;
import cn.beinet.codegenerate.model.ColumnDto;
import cn.beinet.codegenerate.util.TimeHelper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * mapstruct的Dto与Entity转换类生成工具
 */
@Component
public class DtoMapperGenerater implements Generater {
    @Override
    public GenerateType getType() {
        return GenerateType.COMMON;
    }

    @Override
    public String getTemplateName(GenerateDto generateDto) {
        return "static/template/dto_entity.template";
    }

    @Override
    public String getTargetDirName() {
        return "mapstruct";
    }

    @Override
    public String getFullFileName(String entityName) {
        return getTargetDirName() + "/" + entityName + "EntityMapper.java";
    }

    @Override
    public GenerateResult generate(List<ColumnDto> columns, GenerateDto generateDto) {
        StringBuilder sb = new StringBuilder(getTemplate(generateDto));
        replaceSymbol(sb, Vars.PACKAGE_NAME, generateDto.getPackageName());

        String now = TimeHelper.getNow();
        replaceSymbol(sb, Vars.DATE_TIME, now);

        String entityName = getEntityName(columns.get(0).getTable(), generateDto);
        replaceSymbol(sb, Vars.ENTITY_NAME, entityName);

        if (generateDto.getDtoUseTs() != null && generateDto.getDtoUseTs()) {
            // 选中了“DTO使用时间戳”，要添加相应时间字段的映射转换函数
            mapLocalDateTimeToTimestamp(sb, columns);
        } else {
            mapLocalDateTime(sb, columns);
        }

        return new GenerateResult(getFullFileName(entityName), sb.toString());
    }

    private void mapLocalDateTime(StringBuilder sb, List<ColumnDto> columns) {
        StringBuilder sbArea = new StringBuilder();
        for (ColumnDto column : columns) {
            addIgnoreTimeArea(sbArea, column);
        }

        replaceSymbol(sb, Vars.DTO_TIMESTAMP_TO_LOCALDATETIME, "");
        replaceSymbol(sb, Vars.DTO_LOCALDATETIME_TO_TIMESTAMP, sbArea.toString());
    }

    private void mapLocalDateTimeToTimestamp(StringBuilder sb, List<ColumnDto> columns) {
        StringBuilder ts2time = new StringBuilder();
        StringBuilder time2ts = new StringBuilder();

        for (ColumnDto column : columns) {
            if (column.isLocalDateTime()) {
                String colName = getFieldName(column.getColumn(), true);
                if (ts2time.length() > 0) {
                    ts2time.append("\r\n    ");
                    time2ts.append("\r\n    ");
                }
                // Dto转entity，要把timestamp转LocalDateTime
                ts2time.append("@Mapping(source = \"")
                        .append(colName)
                        .append("\", target = \"")
                        .append(colName)
                        .append("\", qualifiedByName = \"mapTime\")");
                // entity转Dto，要把LocalDateTime转timestamp
                time2ts.append("@Mapping(source = \"")
                        .append(colName)
                        .append("\", target = \"")
                        .append(colName)
                        .append("\", qualifiedByName = \"mapTimestamp\")");
            }

            addIgnoreTimeArea(time2ts, column);
        }
        replaceSymbol(sb, Vars.DTO_TIMESTAMP_TO_LOCALDATETIME, ts2time.toString());
        replaceSymbol(sb, Vars.DTO_LOCALDATETIME_TO_TIMESTAMP, time2ts.toString());
    }

    private void addIgnoreTimeArea(StringBuilder sb, ColumnDto column) {
        String colName = getFieldName(column.getColumn(), true);
        if (HtmlGenerater.isSearchKey(colName) && column.isLocalDateTime()) {
            // 时间搜索字段，还要忽略区间字段的转换，否则编译会有MapStruct的告警，如：
            // Unmapped target properties: "createTimeBegin, createTimeEnd".
            sb.append("\n    @Mapping(target = \"" + colName + "Begin\", ignore = true)");
            sb.append("\n    @Mapping(target = \"" + colName + "End\", ignore = true)");
        }
    }
}

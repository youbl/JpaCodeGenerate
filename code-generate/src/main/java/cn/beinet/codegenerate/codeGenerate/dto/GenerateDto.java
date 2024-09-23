package cn.beinet.codegenerate.codeGenerate.dto;

import cn.beinet.codegenerate.model.ColumnDto;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 新类
 *
 * @author youbl
 * @since 2023/4/20 10:13
 */
@Data
public class GenerateDto {
    private String packageName;
    private String database;
    private ColumnDto[] columnArr;
    private String removePrefix;
    private Boolean modify;
    private Boolean feignSdk;
    private Boolean dtoUseTs;
    private String packageResponseData;

    private String oldForReplace;
    private String newForReplace;
    private String jdkVer;

    public Map<String, List<ColumnDto>> getTableMap() {
        if (columnArr == null || columnArr.length == 0)
            return new HashMap<>();
        Map<String, List<ColumnDto>> ret = new HashMap<>();
        for (ColumnDto item : columnArr) {
            List<ColumnDto> tableCols = ret.get(item.getTable());
            if (tableCols == null) {
                tableCols = new ArrayList<>();
                ret.put(item.getTable(), tableCols);
            }
            tableCols.add(item);
        }
        return ret;
    }
}

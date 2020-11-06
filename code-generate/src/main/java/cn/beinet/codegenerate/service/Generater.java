package cn.beinet.codegenerate.service;

import cn.beinet.codegenerate.model.ColumnDto;

/**
 * Generater
 *
 * @author youbl
 * @version 1.0
 * @date 2020/11/6 16:54
 */
interface Generater {
    default String getHead(String packageName) {
        return "package " + packageName + ".model;\n\n" +
                "import lombok.Data;\n\n";
    }


    /**
     * 获取字段在Model类里的field定义
     *
     * @param column 对应的列
     * @return 定义串
     */
    default String getColumnDefine(ColumnDto column) {
        return "    private " +
                column.getFieldType() +
                ' ' +
                column.getColumn() +
                ";\n\n";
    }
}

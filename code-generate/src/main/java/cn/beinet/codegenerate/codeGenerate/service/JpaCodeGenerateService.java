package cn.beinet.codegenerate.codeGenerate.service;

import cn.beinet.codegenerate.codeGenerate.dto.GenerateDto;
import cn.beinet.codegenerate.codeGenerate.dto.GenerateResult;
import cn.beinet.codegenerate.codeGenerate.enums.GenerateType;
import cn.beinet.codegenerate.codeGenerate.service.commonGenerater.Generater;
import cn.beinet.codegenerate.model.ColumnDto;
import cn.beinet.codegenerate.util.FileHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class JpaCodeGenerateService {

    private final List<Generater> generaterList;
    private final String basePath = FileHelper.getResourceBasePath();

    public String generateCode(GenerateDto dto) throws IOException {
        Map<String, List<ColumnDto>> tableMap = dto.getTableMap();
        if (tableMap.isEmpty())
            return "";

        List<String> files = new ArrayList<>();
        for (Map.Entry<String, List<ColumnDto>> item : tableMap.entrySet()) {
            List<ColumnDto> columns = item.getValue();
            for (Generater generater : generaterList) {
                if (!generater.getType().equals(GenerateType.JPA) &&
                        !generater.getType().equals(GenerateType.COMMON))
                    continue;

                GenerateResult result = generater.generate(columns, dto);
                String file = saveFile(result.getFileName(), result.getContent());
                files.add(file);
            }
        }
        String zipFile = doZip(files);
        return hidePath(zipFile);
    }

    private String getKeyType(List<ColumnDto> columns) {
        for (ColumnDto column : columns) {
            if (column.isPrimaryKey()) {
                return column.getManagerType();
            }
        }
        // todo: 无主键？
        return "Long";
    }


    private String saveFile(String fileName, String content) throws IOException {
        String writeFileName = new File(basePath, fileName + ".java").getAbsolutePath();
        FileHelper.saveFile(writeFileName, content);
        return writeFileName;

    }

    private String doZip(List<String> files) throws IOException {
        File zipFile = new File(basePath, "model.zip");
        if (zipFile.exists()) {
            zipFile.delete();
        }
        zipFile.createNewFile();
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for (String file : files) {
                File itemFile = new File(file);
                try (FileInputStream fis = new FileInputStream(itemFile)) {
                    String inFileName = getFileNameWithLastDir(file);
                    zos.putNextEntry(new ZipEntry(inFileName)); // 带目录和文件名压缩
                    int len;
                    byte[] b = new byte[1024];
                    while ((len = fis.read(b)) > 0) {
                        zos.write(b, 0, len);
                    }
                }
            }
        }
        return zipFile.getAbsolutePath();
    }

    /**
     * 返回最后一级目录+文件名，如:
     * /aaa/bbb/ccc/ddd.txt 返回 ccc/ddd.txt
     *
     * @param file 原始文件名
     * @return 带最后一级目录的文件名
     */
    private String getFileNameWithLastDir(String file) {
        file = file.replaceAll("\\\\", "/");
        int idx = file.lastIndexOf('/');
        // Controller不需要目录
        if (!file.contains("Controller")) {
            idx = file.lastIndexOf('/', idx - 1);
        }
        return file.substring(idx + 1);
    }

    private String hidePath(String file) {
        file = file.replace(basePath, "");
        if (file.length() > 0 && (file.charAt(0) == '/' || file.charAt(0) == '\\')) {
            file = file.substring(1);
        }
        return file;
    }
}

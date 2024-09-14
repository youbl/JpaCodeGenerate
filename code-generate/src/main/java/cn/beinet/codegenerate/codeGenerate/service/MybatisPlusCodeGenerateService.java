package cn.beinet.codegenerate.codeGenerate.service;

import cn.beinet.codegenerate.codeGenerate.dto.GenerateDto;
import cn.beinet.codegenerate.codeGenerate.dto.GenerateResult;
import cn.beinet.codegenerate.codeGenerate.enums.GenerateType;
import cn.beinet.codegenerate.codeGenerate.service.commonGenerater.Generater;
import cn.beinet.codegenerate.model.ColumnDto;
import cn.beinet.codegenerate.util.FileHelper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class MybatisPlusCodeGenerateService {

    private final List<Generater> generaterList;

    private final String basePath = FileHelper.getResourceBasePath();

    public List<GenerateResult> generateCode(GenerateDto dto) {
        if (!StringUtils.hasLength(dto.getPackageResponseData())) {
            dto.setPackageResponseData(dto.getPackageName());
        }

        Map<String, List<ColumnDto>> tableMap = dto.getTableMap();
        if (tableMap.isEmpty())
            return new ArrayList<>();

        List<GenerateResult> ret = new ArrayList<>();
        for (Map.Entry<String, List<ColumnDto>> item : tableMap.entrySet()) {
            List<ColumnDto> columns = item.getValue();

            for (Generater generater : generaterList) {
                if (!generater.need(dto))
                    continue;

                if (!generater.getType().equals(GenerateType.MYBATIS) &&
                        !generater.getType().equals(GenerateType.COMMON))
                    continue;

                GenerateResult result = generater.generate(columns, dto);
                ret.add(result);
            }
        }
        return ret;
    }

    public String generateAndZip(GenerateDto dto) {
        List<GenerateResult> results = generateCode(dto);

        // 一次生成多个表时，ResponseData会出现重复，因此改用HashSet
        Set<String> files = new HashSet<>();
        for (GenerateResult item : results) {
            String file = saveFile(item.getFileName(), item.getContent());
            files.add(file);
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
        // todo:无主键？
        return "Long";
    }

    private String saveFile(String fileName, String content) {
        String writeFileName = new File(basePath, fileName).getAbsolutePath();
        FileHelper.saveFile(writeFileName, content);
        return writeFileName;
    }

    @SneakyThrows
    private String doZip(Collection<String> files) {
        File zipFile = new File(basePath, "model.zip");
        if (zipFile.exists()) {
            zipFile.delete();
        }
        zipFile.createNewFile();
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for (String file : files) {
                File itemFile = new File(file);
                try (FileInputStream fis = new FileInputStream(itemFile)) {
                    String inFileName = getFileNameWithLastDir(file, basePath);
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
     * 返回压缩目录路径
     *
     * @param file     原始文件名
     * @param basePath 基础目录
     * @return 用于压缩的带目录的文件名
     */
    private static String getFileNameWithLastDir(String file, String basePath) {
        file = file.replace(basePath, "");
        if (file.startsWith("\\"))
            file = file.substring(1);
        return file;
    }

    private String hidePath(String file) {
        file = file.replace(basePath, "");
        if (file.length() > 0 && (file.charAt(0) == '/' || file.charAt(0) == '\\')) {
            file = file.substring(1);
        }
        return file;
    }

}

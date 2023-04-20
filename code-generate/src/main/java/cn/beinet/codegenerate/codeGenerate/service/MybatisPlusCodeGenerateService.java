package cn.beinet.codegenerate.codeGenerate.service;

import cn.beinet.codegenerate.codeGenerate.dto.GenerateDto;
import cn.beinet.codegenerate.codeGenerate.dto.GenerateResult;
import cn.beinet.codegenerate.codeGenerate.enums.GenerateType;
import cn.beinet.codegenerate.codeGenerate.service.commonGenerater.Generater;
import cn.beinet.codegenerate.model.ColumnDto;
import cn.beinet.codegenerate.util.FileHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class MybatisPlusCodeGenerateService {

    private final List<Generater> generaterList;

    private final String basePath = FileHelper.getResourceBasePath();
    private final CodeDbService dbService;

    public List<GenerateResult> generateCode(GenerateDto dto) {
        if (!StringUtils.hasLength(dto.getDatabase()) ||
                dto.getTables() == null ||
                dto.getTables().length <= 0)
            return new ArrayList<>();

        List<GenerateResult> ret = new ArrayList<>();
        for (String item : dto.getTables()) {
            List<ColumnDto> columns = dbService.getFields(dto.getDatabase(), item);
            if (columns == null || columns.isEmpty())
                continue;

            for (Generater generater : generaterList) {
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

        List<String> files = new ArrayList<>();
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
                return column.getEntityType();
            }
        }
        // todo:无主键？
        return "Long";
    }


    private String saveFile(String fileName, String content) {
        String writeFileName = new File(basePath, fileName).getAbsolutePath();
        try {
            FileHelper.saveFile(writeFileName, content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return writeFileName;
    }

    private String doZip(List<String> files) {
        try {
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
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
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

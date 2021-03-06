package cn.beinet.codegenerate.service;

import cn.beinet.codegenerate.model.ColumnDto;
import cn.beinet.codegenerate.repository.ColumnRepository;
import cn.beinet.codegenerate.util.FileHelper;
import org.springframework.core.env.Environment;
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
public class CodeGenerateService {

    private final ModelGenerater modelGenerater;
    private final DtoGenerater dtoGenerater;
    private final RepositoryGenerater repositoryGenerater;
    private final ServiceGenerater serviceGenerater;
    private final ControllerGenerater controllerGenerater;
    private final Environment env;

    private final String basePath = FileHelper.getResourceBasePath();
    private final ColumnRepository columnRepository;

    public CodeGenerateService(Environment env,
                               ModelGenerater modelGenerater,
                               DtoGenerater dtoGenerater,
                               RepositoryGenerater repositoryGenerater,
                               ServiceGenerater serviceGenerater,
                               ControllerGenerater controllerGenerater) {
        this.env = env;
        this.columnRepository = new ColumnRepository(env, ColumnRepository.DbEnv.DEFAULT);

        this.modelGenerater = modelGenerater;
        this.dtoGenerater = dtoGenerater;
        this.repositoryGenerater = repositoryGenerater;
        this.serviceGenerater = serviceGenerater;
        this.controllerGenerater = controllerGenerater;
    }

    public List<String> getDatabases() {
        return columnRepository.findDatabases();
    }

    public List<String> getTables(String database) {
        if (StringUtils.isEmpty(database))
            return new ArrayList<>();
        return columnRepository.findTables(database);
    }

    public List<ColumnDto> getFields(String database, String[] tables) {
        if (StringUtils.isEmpty(database) || tables == null || tables.length <= 0)
            return new ArrayList<>();

        List<ColumnDto> ret = new ArrayList<>();
        for (String table : tables) {
            ret.addAll(columnRepository.findColumnByTable(database, table));
        }
        return ret;
    }

    public String generateCode(String database, String[] tables, String packageName) throws IOException {
        if (StringUtils.isEmpty(database) || tables == null || tables.length <= 0)
            return "";

        List<String> files = new ArrayList<>();
        for (String item : tables) {
            List<ColumnDto> columns = columnRepository.findColumnByTable(database, item);
            if (columns == null || columns.isEmpty())
                continue;

            // 创建dto文件
            files.add(generateDtoFile(columns, packageName));
            // 创建数据库model文件
            files.add(generateModelFile(columns, packageName));
            // 创建仓储层文件
            files.add(generateRepositoryFile(columns, packageName));
            // 创建Service层文件
            files.add(generateServiceFile(columns, packageName));
            // 创建Controller层文件
            files.add(generateControllerFile(columns, packageName));
        }
        String zipFile = doZip(files);
        return hidePath(zipFile);
    }

    /**
     * 生成Dto类文件
     *
     * @param columns     表的列清单
     * @param packageName 包名
     * @return 生成的文件名
     * @throws IOException 可能的异常
     */
    private String generateDtoFile(List<ColumnDto> columns, String packageName) throws IOException {
        String model = dtoGenerater.generate(columns, packageName);
        return saveFile("model/" + columns.get(0).getTable() + "Dto", model);
    }

    /**
     * 生成Model类文件
     *
     * @param columns     表的列清单
     * @param packageName 包名
     * @return 生成的文件名
     * @throws IOException 可能的异常
     */
    private String generateModelFile(List<ColumnDto> columns, String packageName) throws IOException {
        String model = modelGenerater.generate(columns, packageName);
        return saveFile("model/" + columns.get(0).getTable(), model);
    }

    /**
     * 生成仓储接口文件
     *
     * @param columns     表的列清单
     * @param packageName 包名
     * @return 生成的文件名
     * @throws IOException 可能的异常
     */
    private String generateRepositoryFile(List<ColumnDto> columns, String packageName) throws IOException {
        String table = columns.get(0).getTable();
        String keyType = getKeyType(columns);
        String content = repositoryGenerater.generate(table, keyType, packageName);
        return saveFile("repository/" + columns.get(0).getTable() + "Repository", content);
    }


    /**
     * 生成Service层文件
     *
     * @param columns     表的列清单
     * @param packageName 包名
     * @return 生成的文件名
     * @throws IOException 可能的异常
     */
    private String generateServiceFile(List<ColumnDto> columns, String packageName) throws IOException {
        String table = columns.get(0).getTable();
        String keyType = getKeyType(columns);
        String content = serviceGenerater.generate(table, keyType, packageName);
        return saveFile("service/" + table + "Service", content);
    }

    /**
     * 生成Controller层文件
     *
     * @param columns     表的列清单
     * @param packageName 包名
     * @return 生成的文件名
     * @throws IOException 可能的异常
     */
    private String generateControllerFile(List<ColumnDto> columns, String packageName) throws IOException {
        String table = columns.get(0).getTable();
        String keyType = getKeyType(columns);
        String content = controllerGenerater.generate(table, keyType, packageName);
        return saveFile(table + "Controller", content);
    }

    private String getKeyType(List<ColumnDto> columns) {
        for (ColumnDto column : columns) {
            if (column.isPrimaryKey()) {
                return column.getManagerType();
            }
        }
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

    /**
     * 拼接可以下载的文件路径返回
     *
     * @param file 相对路径
     * @return 绝对路径
     */
    public String getRealPath(String file) {
        return new File(basePath, file).getAbsolutePath();
    }
}

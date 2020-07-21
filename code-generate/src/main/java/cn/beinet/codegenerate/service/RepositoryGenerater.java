package cn.beinet.codegenerate.service;

import org.springframework.stereotype.Component;

@Component
public class RepositoryGenerater {

    String generate(String table, String packageName) {
        StringBuilder sb = new StringBuilder();
        // 头部的package和import
        sb.append(getHead(table, packageName));

        // class的定义
        sb.append("@Repository\n")
                .append("public interface ")
                .append(table).append("Repository ")
                .append("extends JpaRepository<").append(table).append(", Long> {\n}\n");
        return sb.toString();
    }

    private String getHead(String table, String packageName) {
        return "package " + packageName + ".repository;\n\n" +
                "import " + packageName + ".model." + table + ";\n" +
                "import org.springframework.data.jpa.repository.JpaRepository;\n" +
                "import org.springframework.stereotype.Repository;\n\n";
    }

}

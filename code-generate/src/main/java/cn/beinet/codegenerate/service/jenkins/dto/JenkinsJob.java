package cn.beinet.codegenerate.service.jenkins.dto;

import lombok.Data;

import java.util.List;

/**
 * 新类
 *
 * @author youbl
 * @since 2023/8/25 11:23
 */
@Data
public class JenkinsJob {
    private String type;
    private String name;
    private String url;

    private List<JenkinsJob> subJobs;
}

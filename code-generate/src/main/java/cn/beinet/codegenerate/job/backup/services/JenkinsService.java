package cn.beinet.codegenerate.job.backup.services;

import cn.beinet.codegenerate.job.backup.services.dto.JenkinsJob;
import cn.beinet.codegenerate.util.HttpHelper;
import org.springframework.util.StringUtils;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JenkinsService {
    private String jenkinsUrl;
    Map<String, String> headers;

    public JenkinsService(String jenkinsUrl, String user, String pwd) {
        this.jenkinsUrl = jenkinsUrl;

        headers = new HashMap<>();
        headers.put("Authorization", getAuthorization(user, pwd));
    }

    /**
     * 获取所有的job名称列表
     */
    public List<JenkinsJob> getAllJobName() {
        return getJobsByUrl(jenkinsUrl);
    }

    public String getJobConfig(JenkinsJob job) {
        //String url = jenkinsUrl + "job/" + URLEncoder.encode(jobname) + "/config.xml";
        String url = job.getUrl() + "/config.xml";
        String xml = sendToJenkins(url);
        return xml;
    }

    private String sendToJenkins(String url) {
        HttpHelper.Config config = new HttpHelper.Config();
        config.setHeaders(headers);
        String xml = HttpHelper.GetPage(url, config);
        return xml;
    }

    private String getAuthorization(String user, String pwd) {
        String userPwd = user + ":" + pwd;
        String base64 = Base64.getEncoder().encodeToString(userPwd.getBytes());
        return "Basic " + base64;
    }

    private List<JenkinsJob> getJobsByUrl(String url) {
        String apiUrl = url + "api/xml";
        //if (!string.IsNullOrEmpty(jobname))
        //    apiUrl += "?xpath=/hudson/job[name=%27" + Uri.EscapeDataString(jobname) + "%27]";
        String xml = sendToJenkins(apiUrl);
        return parseXml(xml);
    }

    private List<JenkinsJob> parseXml(String xml) {
        List<JenkinsJob> jobs = new JenkinsXmlParser().xmlToJob(xml);
        for (JenkinsJob job : jobs) {
            processJob(job);
            if ("com.cloudbees.hudson.plugins.folder.Folder".equals(job.getType())) {
                List<JenkinsJob> subJobs = getJobsByUrl(job.getUrl());
                job.setSubJobs(subJobs);
            }
        }
        return jobs;
    }

    // xml里返回的url，域名可能是127.0.0.1，要替换掉
    private void processJob(JenkinsJob job) {
        String url = job.getUrl();
        if (!StringUtils.hasLength(url))
            return;
        int idx = url.indexOf("/", "https://".length() + 1); // 查找http协议之后的第一个斜杠
        if (idx > 0) {
            if (jenkinsUrl.endsWith("/"))
                idx++; // 防止下面的拼接出现2个斜杠
            url = url.substring(idx);
            url = jenkinsUrl + url;
        }
        job.setUrl(url);
    }
}

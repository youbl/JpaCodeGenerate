package cn.beinet.codegenerate.service.jenkins;

import cn.beinet.codegenerate.service.jenkins.dto.JenkinsJob;
import lombok.SneakyThrows;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 新类
 *
 * @author youbl
 * @since 2023/8/25 10:59
 */
public class JenkinsXmlParser {
    @SneakyThrows
    public List<JenkinsJob> xmlToJob(String xmlStr) {
        SAXReader xmlReader = new SAXReader();
        try (StringReader reader = new StringReader(xmlStr)) {
            Document document = xmlReader.read(reader);

            Element element = document.getRootElement();
            return xmlToJob(element);
        }
    }

    private List<JenkinsJob> xmlToJob(Element element) {
        List<JenkinsJob> ret = new ArrayList<>();

        List<Element> elements = element.elements();
        for (Element child : elements) {
            if ("/hudson/job".equals(child.getPath()) || // 首层job
                    "/folder/job".equals(child.getPath())) { // 文件夹下的job
                ret.add(parseJob(child));
            }
        }
        return ret;
    }

    private JenkinsJob parseJob(Element child) {
        JenkinsJob item = new JenkinsJob();

        List<Attribute> attributes = child.attributes();
        for (Attribute attribute : attributes) {
            if ("_class".equals(attribute.getName())) {
                item.setType(attribute.getText());
            }
        }
        List<Element> elements = child.elements();
        for (Element element : elements) {
            if ("name".equals(element.getName())) {
                item.setName(element.getStringValue());
            } else if ("url".equals(element.getName())) {
                item.setUrl(element.getStringValue());
            }
        }
        return item;
    }
}

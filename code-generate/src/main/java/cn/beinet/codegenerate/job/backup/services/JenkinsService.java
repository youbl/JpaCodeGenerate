package cn.beinet.codegenerate.job.backup.services;

import cn.beinet.codegenerate.util.HttpHelper;
import lombok.Data;
import lombok.Getter;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.*;

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
    public List<String> getAllJobName() {
        String url = jenkinsUrl + "api/xml";
        //if (!string.IsNullOrEmpty(jobname))
        //    url += "?xpath=/hudson/job[name=%27" + Uri.EscapeDataString(jobname) + "%27]";
        String xml = sendToJenkins(url);
        return parseJobNames(xml);
    }

    public String getJobConfig(String jobname) {
        String url = jenkinsUrl + "job/" + URLEncoder.encode(jobname) + "/config.xml";
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

    private List<String> parseJobNames(String xml) {
        try {
            return parseXml(xml);
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
    }

    private List<String> parseXml(String xml) throws ParserConfigurationException, SAXException, IOException {
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        JenkinsJobNameHandler handler = new JenkinsJobNameHandler();
        try (StringReader stringReader = new StringReader(xml)) {
            InputSource source = new InputSource(stringReader);
            parser.parse(source, handler);
        }
        return handler.getJobNames();
    }


    // 自定义的JenkinsJob游标解析器
    private class JenkinsJobNameHandler extends DefaultHandler {
        @Getter
        private List<String> jobNames = new ArrayList<>();
        private String currentXmlPath;

        @Override
        public void startDocument() throws SAXException {
            //System.out.println("SAX解析XML开始");
            currentXmlPath = "";
        }

        /**
         * xml元素起始
         */
        @Override
        public void startElement(String uri, String localName, String qName,
                                 Attributes attributes) throws SAXException {
            if (currentXmlPath.length() > 0)
                currentXmlPath += ".";
            currentXmlPath += qName;
        }

        @Override
        public void characters(char ch[], int start, int length)
                throws SAXException {
            if (length <= 0)
                return;
            if ("hudson.job.name".equals(currentXmlPath)) {
                String str = new String(ch, start, length);
                jobNames.add(str);
            }
        }

        /**
         * xml元素结束
         */
        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            String end = "." + qName;
            if (currentXmlPath.endsWith(end)) {
                currentXmlPath = currentXmlPath.substring(0, currentXmlPath.lastIndexOf("."));
            }
        }
    }


    // 自定义的Xml游标解析器
    private class MyXmlHandler extends DefaultHandler {
        @Getter
        private XmlNode rootNode;

        private XmlNode currentNode;
        private String currentXmlPath;

        @Override
        public void startDocument() throws SAXException {
            //System.out.println("SAX解析XML开始");
            currentXmlPath = "";
        }

        @Override
        public void endDocument() throws SAXException {
            //System.out.println("SAX解析XML结束: " + currentXmlPath);
        }

        /**
         * xml元素起始
         */
        @Override
        public void startElement(String uri, String localName, String qName,
                                 Attributes attributes) throws SAXException {
            XmlNode node = new XmlNode();
            if (rootNode == null) {
                rootNode = node;
            } else {
                currentNode.children.add(node);
            }

            node.parent = currentNode;
            currentNode = node;

            if (currentXmlPath.length() > 0)
                currentXmlPath += ".";
            currentXmlPath += qName;
            node.currentXmlPath = currentXmlPath;
            node.name = qName;

            for (int i = 0, j = attributes.getLength(); i < j; i++) {
                String attName = attributes.getQName(i);
                String attVal = attributes.getValue(i);
                node.attrList.put(attName, attVal);
            }

            //System.out.println(currentXmlPath + "开始");
        }

        @Override
        public void characters(char ch[], int start, int length)
                throws SAXException {
            if (length <= 0)
                return;
            if (currentNode == null) {
                System.out.println("当前节点为空？？？");
            }
            String str = new String(ch, start, length);
            currentNode.innerText = str;
        }

        /**
         * xml元素结束
         */
        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            currentNode = currentNode.parent;
            //System.out.println(currentXmlPath + "结束");

            String end = "." + qName;
            if (currentXmlPath.endsWith(end)) {
                currentXmlPath = currentXmlPath.substring(0, currentXmlPath.lastIndexOf("."));
            }
            //System.out.println(currentXmlPath);
        }
    }

    @Data
    public class XmlNode {
        private String name;
        private String innerText;
        private Map<String, String> attrList = new HashMap<>();

        private XmlNode parent;
        private List<XmlNode> children = new ArrayList<>();

        private String currentXmlPath;

        @Override
        public String toString() {
            return currentXmlPath;
        }
    }
}

package com.util.net.http.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



/**
 * 加载servlet配置xml
 */
public class HttpClazzConfigLoader {
	
	private static final Logger logger = LoggerFactory.getLogger(HttpClazzConfigLoader.class);

	public static List<HttpClassConfig> load(String file) {
		List<HttpClassConfig> ret = new ArrayList<HttpClassConfig>();
		InputStream in = null;
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			in = new FileInputStream(file);
			Document doc = builder.parse(in);
			NodeList list = doc.getElementsByTagName("servlet");

			HttpClassConfig config = new HttpClassConfig();
			if (list.getLength() > 0) {
				Node node = list.item(0);
				NodeList childs = node.getChildNodes();

				for (int j = 0; j < childs.getLength(); j++) {
					if ("path".equals(childs.item(j).getNodeName())) {
						config.setPath(childs.item(j).getTextContent());
					} else if ("clazz".equals(childs.item(j).getNodeName()))
						config.setClzss(childs.item(j).getTextContent());
				}
				ret.add(config);
			}

			logger.info("load config !");
			return ret;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
		return ret;
	}
}

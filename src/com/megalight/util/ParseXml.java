/** 

* @Title: ParseXml.java

* @Package com.megalight.util

* @Description: TODO(用一句话描述该文件做什么)

* @author hulikaimen@gmail.com

* @date 2016-8-15 上午10:24:06

* @version V1.0 

*/ 
package com.megalight.util;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * @author PipiLu
 * @version 创建时间：2016-8-15 上午10:24:06
 * 类说明
 */
public class ParseXml {
    
    private String filePath;
 
    private Document document = null; 
     
    public ParseXml(String filePath) {     
        this.filePath = filePath;
        this.load(this.filePath);
    }  
     
    private void load(String filePath){
    	
		try{
			SAXReader reader = new SAXReader();
			//从classpath下加载
			document = reader.read(ParseXml.class.getClassLoader().getResourceAsStream("config.xml"));
		}catch(DocumentException e){
			e.printStackTrace();
			System.out.println("文件加载异常：" + filePath);
		}
//        File file = new File(filePath);
//        if (file.exists()) {
//            SAXReader saxReader = new SAXReader();
//            try {
//                document = saxReader.read(file);
//            } catch (DocumentException e) {    
//                System.out.println("文件加载异常：" + filePath);              
//            }
//        } else{
//            System.out.println("文件不存在 : " + filePath);
//        }          
    }  
     
    public Element getElementObject(String elementPath) {
        return (Element) document.selectSingleNode(elementPath);
    }  
     
    @SuppressWarnings("unchecked")
    public List<Element> getElementObjects(String elementPath) {
        return document.selectNodes(elementPath);
    }
     
    @SuppressWarnings("unchecked")
    public Map<String, String> getChildrenInfoByElement(Element element){
        Map<String, String> map = new HashMap<String, String>();
        List<Element> children = element.elements();
        for (Element e : children) {
            map.put(e.getName(), e.getText());
        }
        return map;
    }
     
    public boolean isExist(String elementPath){
        boolean flag = false;
        Element element = this.getElementObject(elementPath);
        if(element != null) flag = true;
        return flag;
    }
 
    public String getElementText(String elementPath) {
        Element element = this.getElementObject(elementPath);
        if(element != null){
            return element.getText().trim();
        }else{
            return null;
        }      
    }
     
//    public static void main(String[] args) {
//         
//        ParseXml px = new ParseXml("config.xml");
//        Element el = px.getElementObject("/config/jdbcUrl");
//        System.out.println("jdbcUrl:" + el.getText());
//        
//        List<Element> elements = px.getElementObjects("/config/paths/path");
//        for(Element e : elements){
//        	System.out.println(e.getText());
//        }
//    }
     
}

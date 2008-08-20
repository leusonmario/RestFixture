package smartrics.rest.fitnesse.fixture.support;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public final class Tools {

	private Tools(){

	}

	public static NodeList extractXPath(String xpathExpression, String content){
		// throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		// Build a Document using the cached String response
		try{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(getInputStreamFromString(content));

			// Use the java Xpath API to return a NodeList to the caller so they can
			// iterate through
			XPathFactory xpathFactory = XPathFactory.newInstance();
			XPath xpath = xpathFactory.newXPath();
			XPathExpression expr = xpath.compile(xpathExpression);
			return (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			throw new IllegalArgumentException("xPath expression is incorrect", e);
		} catch (ParserConfigurationException e) {
			throw new IllegalArgumentException("parser for last response body caused an error", e);
		} catch (SAXException e) {
			throw new IllegalArgumentException("last response body cannot be parsed", e);
		} catch (IOException e) {
			throw new IllegalArgumentException("IO Exception when reading the document", e);
		}

	}

	public static String getStringFromInputStream(InputStream is) {
		String line = null;
		if(is==null) return "";
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		try{
			while ((line = in.readLine()) != null) {
				sb.append(line);
			}
		} catch(IOException e){
			throw new IllegalArgumentException("Unable to read from stream", e);
		}
		return sb.toString();
	}

	public static InputStream getInputStreamFromString(String string) {
		if(string==null)
			throw new IllegalArgumentException("null input");
		byte[] byteArray = string.getBytes();
		return new ByteArrayInputStream(byteArray);
	}

	public static String convertMapToString(Map<String, String> map, String nvSep, String entrySep){
		StringBuffer sb = new StringBuffer();
		if(map != null){
			for(String el : map.keySet()){
				sb.append(convertEntryToString(el, map.get(el), nvSep)).append(entrySep);
			}
		}
		String repr = sb.toString();
		int pos = repr.lastIndexOf(entrySep);
		return repr.substring(0, pos);
	}

	public static String convertEntryToString(String name, String value, String nvSep){
		return String.format("%s%s%s",name, nvSep, value);
	}

	public static boolean regex(String text, String expr) {
		try{
			Pattern p = Pattern.compile(expr);
			boolean find = p.matcher(text).find();
			return find;
		} catch(PatternSyntaxException e){
			throw new IllegalArgumentException("Invalid regex " + expr);
		}
	}

	public static Map<String, String> convertStringToMap(final String expStr, final String nvSep, final String entrySep) {
		String[] nvpArray = expStr.split(entrySep);
		Map<String, String> ret = new HashMap<String, String>();
		for(String nvp : nvpArray){
			try{
				int pos = nvp.indexOf(nvSep);
				String v = "";
				String k = nvp;
				if(pos!=-1){
					int pos2 = pos + nvSep.length();
					v = nvp.substring(pos2).trim();
					k = nvp.substring(0, pos).trim();
				}
				ret.put(k, v);
			} catch(RuntimeException e){
				throw new IllegalArgumentException("Each entry in the must be separated by '" + entrySep +
						"' and each entry must be expressed as a name" + nvSep + "value");
			}
		}
		return ret;
	}

	public static String toHtml(String text) {
		// if(text.trim().startsWith("<"))
		// return "<textarea cols='180' rows='10' readonly='true'>" +
		// prettyPrint(text).replaceAll("&gt;", "&gt;\n") + "</textarea>";
		return text.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll(
				System.getProperty("line.separator"), "<br/>").replaceAll(" ",
				"&nbsp;");
	}

	public static String fromHtml(String text) {
		String ls = System.getProperty("line.separator");
		return text.replaceAll("<br[\\s]*/>", ls).replaceAll("<BR[\\s]*/>", ls)
				.replaceAll("<pre>", "").replaceAll("</pre>", "").replaceAll("&nbsp;", " ")
				.replaceAll("&gt;", ">").replaceAll("&lt;", "<").replaceAll("&nbsp;", " ");
	}

}
package io.gonzo.middleware.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlUtils {

    public static String getTagValue(String tag, Element eElement) {
        NodeList nlList = eElement.getElementsByTagName(tag).item(0).getChildNodes();
        Node nValue = (Node) nlList.item(0);
        if (nValue == null)
            return null;
        return nValue.getNodeValue();
    }

    public static void resultCodeByException(Document doc) {
        String resultCode = doc.getElementsByTagName("resultCode")
                .item(0)
                .getChildNodes()
                .item(0)
                .getNodeValue();

        if (resultCode.equals("99")) {
            throw new NullPointerException();
        }

    }

}

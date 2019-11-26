import reader.XmlReader;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;

public class XmlParser {
    public static void main(String[] args) throws FileNotFoundException, XMLStreamException {
        File resourcesDirectory = new File("src/main/resources/sms-20191110120028.xml");
        XmlReader.parse(resourcesDirectory);
    }
}

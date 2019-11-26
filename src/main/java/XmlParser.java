import reader.XmlReader;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;

public class XmlParser {
    public static void main(String[] args) throws FileNotFoundException, XMLStreamException, ParseException {
        File resourcesDirectory = new File("src/main/resources/sms-20191110120028.xml");
        XmlReader.parse(resourcesDirectory);
    }
}

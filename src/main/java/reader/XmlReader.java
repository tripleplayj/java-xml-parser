package reader;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.Map.Entry.comparingByKey;
import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;

public class XmlReader {

    public static void parse(File file) throws FileNotFoundException, XMLStreamException, ParseException {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLEventReader eventReader = factory.createXMLEventReader(new FileReader(file));

        int cToJMsgCount = 0;
        int jToCMsgCount = 0;

        Map<String, Integer> cToJWordMap = new HashMap<>();
        Map<String, Integer> jToCWordMap = new HashMap<>();
        Map<String, Integer> textsByDateMap = new HashMap<>();

        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextEvent();

            if (event.isStartElement()) {
                StartElement element = (StartElement) event;

                if (element.getName().toString().equalsIgnoreCase("sms")) {
                    String contactName = element.getAttributeByName(new QName("", "contact_name")).getValue();
                    String message = element.getAttributeByName(new QName("", "body")).getValue();
                    String type = element.getAttributeByName(new QName("", "type")).getValue();
                    String date = element.getAttributeByName(new QName("", "readable_date")).getValue();
                    Map<String, Integer> currMap = new HashMap<String, Integer>();
                    //TODO: readd appropriate contact name here
                    if (contactName.equals("")) {
                        //TODO: I was too tired to parse dates here, but wanted to see if it would work
                        String[] splitDate = date.split(" ");
                            String stupidParsedDate = splitDate[0] + splitDate[1] + splitDate[2];
                            if(textsByDateMap.get(stupidParsedDate) == null) {
                                textsByDateMap.put(stupidParsedDate, 1);
                            }
                            else {
                                int oldValue = textsByDateMap.get(stupidParsedDate);
                                oldValue++;
                                textsByDateMap.put(stupidParsedDate, oldValue);
                            }

                        if(type.equals("1")) {
                            cToJMsgCount++;
                            currMap = cToJWordMap;
                        }
                        else if(type.equals("2")) {
                            jToCMsgCount++;
                            currMap = jToCWordMap;
                        }
                        String[] messageArray = message.split(" ");
                        for (int i = 0; i < messageArray.length; i++) {
                            if(currMap.get(messageArray[i].toLowerCase()) == null) {
                                currMap.put(messageArray[i].toLowerCase(), 1);
                            }
                            else {
                                int oldValue = currMap.get(messageArray[i].toLowerCase());
                                oldValue++;
                                currMap.put(messageArray[i].toLowerCase(), oldValue);
                            }
                        }
                    }
                }
            }
        }
        System.out.println("C to J " + cToJMsgCount);
        System.out.println("J to C " + jToCMsgCount);
        System.out.println("Total texts " + (cToJMsgCount + jToCMsgCount));

        Map<String, Integer> cToJWordMapSorted = cToJWordMap
                                                .entrySet()
                                                .stream()
                                                .sorted(Collections.reverseOrder(comparingByValue())).collect(toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2, LinkedHashMap::new));

        Map<String, Integer> jToCWordMapSorted = jToCWordMap
                                                .entrySet()
                                                .stream()
                                                .sorted(Collections.reverseOrder(comparingByValue())).collect(toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2, LinkedHashMap::new));

        Map<String, Integer> dateMapSorted = textsByDateMap
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(comparingByValue())).collect(toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2, LinkedHashMap::new));

        System.out.println();

        System.out.println();
    }
}

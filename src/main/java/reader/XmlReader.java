package reader;

import org.joda.time.DateTime;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.ParseException;
import java.util.*;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;

public class XmlReader {

    public void parse(File file) throws FileNotFoundException, XMLStreamException, ParseException {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLEventReader eventReader = factory.createXMLEventReader(new FileReader(file));

        int cToJMsgCount = 0;
        int jToCMsgCount = 0;

        Map<String, Integer> cToJWordMap = new HashMap<>();
        Map<String, Integer> jToCWordMap = new HashMap<>();
        Map<String, Integer> textsByDateMap = new HashMap<>();
        Map<String, Integer> textsByTimeMap = new HashMap<>();

        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextEvent();
            if (event.isStartElement()) {
                StartElement element = (StartElement) event;
                if (element.getName().toString().equalsIgnoreCase("sms")) {
                    String contactName = element.getAttributeByName(new QName("", "contact_name")).getValue();
                    String message = element.getAttributeByName(new QName("", "body")).getValue();
                    String type = element.getAttributeByName(new QName("", "type")).getValue();
                    String dateTimestamp = element.getAttributeByName(new QName("", "date")).getValue();
                    Map<String, Integer> currMap = new HashMap<String, Integer>();
                    //TODO: read appropriate contact name here
                    if (contactName.equals("")) {
                        DateTime dt = new DateTime( Long.parseLong( dateTimestamp ) ) ;
                        String dateTextSent = dt.getMonthOfYear() + "/" + dt.getDayOfMonth() + "/" + dt.getYear();
                        insertOrIncMapCount(textsByDateMap, dateTextSent);
                        String timeTextSent = dt.getHourOfDay() + ":";
                        timeTextSent += dt.getMinuteOfHour() < 10 ? "0" + dt.getMinuteOfHour() :   dt.getMinuteOfHour();
                        insertOrIncMapCount(textsByTimeMap, timeTextSent);

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

        Map<String, Integer> cToJWordMapSorted = sortMapDesc(cToJWordMap);
        Map<String, Integer> jToCWordMapSorted = sortMapDesc(jToCWordMap);
        Map<String, Integer> dateMapSorted = sortMapDesc(textsByDateMap);
        Map<String, Integer> timeMapSorted = sortMapDesc(textsByTimeMap);

        //get total word count
        Map<String, Integer> cWordMapCopy = new HashMap<>();
        cWordMapCopy.putAll(cToJWordMap);
        Map<String, Integer> jWordMapCopy = new HashMap<>();
        jWordMapCopy.putAll(jToCWordMap);
        cWordMapCopy.forEach((k, v) -> jWordMapCopy.merge(k, v, Integer::sum));
        sortMapDesc(jWordMapCopy);

        cToJWordMap.entrySet().forEach(entry->{
            System.out.println(entry.getKey() + " " + entry.getValue());
        });

        System.out.println();
    }

    private void insertOrIncMapCount( Map<String, Integer> map, String key) {
        if (map.get(key) == null) {
            map.put(key, 1);
        }
        else {
            int oldValue = map.get(key);
            oldValue++;
            map.put(key, oldValue);
        }
    }

    private Map<String, Integer> sortMapDesc(Map<String, Integer> map) {
        return  map
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(comparingByValue())).collect(toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2, LinkedHashMap::new));

    }
}

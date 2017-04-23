import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.util.*;

/**
 * Created by hkong on 4/22/17.
 */
public class JsonToCsv {
    private final String CSV_DELIMITER = ",";

    /**
     * reade json file and output JsonArray
     * @param fileName
     * @return
     */
    public JsonArray convertFileToJsonArray(String fileName) {
        JsonArray jarray = null;
        try {
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(new FileReader(fileName));
            if (jsonElement instanceof JsonArray) {
                jarray = jsonElement.getAsJsonArray();
            } else {
                System.out.print("The file doesn't contain an array of JSON object. Please check again.");
                return jarray;
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error : JSON source file not found, please make sure it's in " + System.getProperty("user.dir"));
        }

        return jarray;
    }

    /**
     * flattern jsonArray into a list of key/value
     * @param jsonArray
     * @return
     */
    public List<Map<String, String>> parse(JsonArray jsonArray) {
        List<Map<String, String>> flatJson = new ArrayList<>();
        for (JsonElement element : jsonArray) {
            JsonObject jsonObject = element.getAsJsonObject();
            Map<String, String> stringMap = flatten(jsonObject);
            flatJson.add(stringMap);
        }
        return flatJson;
    }


    /**
     * generate CSV from the list of key/value
     * @param flatJson
     * @param fileName
     */
    public void writeAsCSV(List<Map<String, String>> flatJson, String fileName) {
        Set<String> headers = collectHeaders(flatJson);
        String output = join(new ArrayList<>(headers), CSV_DELIMITER) + "\n";
        for (Map<String, String> map : flatJson) {
            output = output + getCommaSeperatedRow(headers, map) + "\n";
        }
        // DEBUG output
        System.out.println(output);
        writeToFile(output, fileName);
    }

    private Map<String, String> flatten(JsonObject jsonObject) {
        Map<String, String> flatJson = new HashMap<>();
        Set<Map.Entry<String, JsonElement>> entries = jsonObject.entrySet();
        for (Map.Entry<String, JsonElement> entry: entries) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            if (value instanceof JsonArray || value instanceof JsonObject) {
                System.out.print("Error : Nested JSON objects or arrays are not supported.");
            } else {
                flatJson.put(key, value.getAsString());
            }
        }
        return flatJson;
    }

    private void writeToFile(String output, String fileName) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(output);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(writer);
        }
    }

    private void close(BufferedWriter writer) {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getCommaSeperatedRow(Set<String> headers, Map<String, String> map) {
        List<String> items = new ArrayList<>();
        for (String header : headers) {
            if(map.containsKey(header)) {
                items.add(map.get(header));
            } else {
                items.add("");
            }
        }
        return join(items, CSV_DELIMITER);
    }

    private Set<String> collectHeaders(List<Map<String, String>> flatJson) {
        Set<String> headers = new HashSet<>();
        for (Map<String, String> map : flatJson) {
            headers.addAll(map.keySet());
        }
        return headers;
    }

    private String join(List<String> list, String delim) {
        StringBuilder sb = new StringBuilder();
        String loopDelim = "";
        for(String s : list) {
            sb.append(loopDelim);
            sb.append(s);
            loopDelim = delim;
        }

        return sb.toString();
    }

    /**
     * Main entry point
     * @param args
     */
    public static void main(String[] args) {
        if(args != null && args.length > 1) {
            String sourceFile = args[0];
            String targetFile = args[1];

            JsonToCsv jsonToCsv = new JsonToCsv();
            JsonArray jArray = jsonToCsv.convertFileToJsonArray(sourceFile);
            if(jArray != null && jArray.size() > 0) {
                List<Map<String, String>> flatJson = jsonToCsv.parse(jArray);
                jsonToCsv.writeAsCSV(flatJson, targetFile);
                System.out.println("\nRESULT -> A new CSV file created : " + targetFile + "\n");
            }
        } else {
            System.out.println("Arguments missed!");
            System.out.println("Please pass in JSON source filename and target CSV filename via argument. Make sure the file directory is in " + System.getProperty("user.dir"));
        }
    }
}

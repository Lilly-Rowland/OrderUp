package com.example.game;
import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonFileReader {
    public static void main(String[] args) throws IOException {
        System.out.println("==== JsonFileReader started ====");
        ObjectMapper mapper = new ObjectMapper();
        java.io.InputStream inputStream = JsonFileReader.class.getResourceAsStream("/menu_options.json");
        if (inputStream == null) {
            System.out.println("ERROR: Could not find menu_options.json");
            return;
        }
        JsonNode menuArray = mapper.readTree(inputStream);
        if (!menuArray.isArray() || menuArray.size() == 0) {
            System.out.println("ERROR: menu_options.json is not an array or is empty");
            return;
        }
        // Get the first item
        JsonNode menuNode = menuArray.get(0);
        String name = menuNode.get("name").asText();
        double price = menuNode.get("price").asDouble();
        double quality = menuNode.get("quality").asDouble();
        System.out.println("Name: " + name + ", Price: $" + price + ", Quality: " + quality);
        System.out.println("==== JsonFileReader finished ====");
    }
}

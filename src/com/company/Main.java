package com.company;

import org.json.*;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.*;
import java.util.Map;
import java.util.TreeMap;

/**
 * Отличия от предыдущей домашки: частотный словарь заменен на словарь: ключ - название города,
 * значение - экземпляр класса с информацией о городе (количество проживающих друзей в этом городе и процентное соотношение).
 * Добавлены методы преобразования словаря в файл .json
 */

public class Main {
    public static void main(String[] args) throws IOException, JSONException {
        //получение данных
        URL url = new URL("https://api.vk.com/method/friends.get?user_id=137795470&fields=city&access_token=6a6f00e00e32891b7922d7c1ccb908a78de2c0f6b2abfe7a66ac9b634338b5b8c02848d4a1fe8f309d553&v=5.126");
        URLConnection yc = url.openConnection();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        yc.getInputStream()));
        String inputLine = readAll(in);
        in.close();
        //чтение списка друзей и их количества
        JSONObject json = new JSONObject(inputLine);
        int count = (int) json.getJSONObject("response").get("count");
        JSONArray friends = json.getJSONObject("response").getJSONArray("items");
        //заполнение словаря
        Map<String, CityInfo> filledDict = fillDictionary(friends, count);
        System.out.println(jsonFromDict(filledDict).toString());
        createJSONFile(jsonFromDict(filledDict).toString());
    }

    private static Map<String, CityInfo> fillDictionary(JSONArray friends, int countFriends) {
        Map<String, CityInfo> result = new TreeMap<>();
        for (int i = 0; i < friends.length(); i++) {
            try {
                String city = friends.getJSONObject(i).getJSONObject("city").get("title").toString();
                if (result.containsKey(city)) {
                    result.get(city).incCount();
                }//обновление счетчика друзей в структуре информации о городе

                else //добавление нового объекта в словарь
                    result.put(city, new CityInfo(city, 1, countFriends));
            } catch (Exception ignored) {
            }
        }
        return result;
    }

    //чтение ответа api
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    //создание файла .json
    private static void createJSONFile(String data) throws IOException {
        Files.createFile(Paths.get("src/data.json"));
        FileWriter fw = new FileWriter("src/data.json");
        fw.write(data);
        fw.close();
    }

    public static JSONObject jsonFromDict(Map<String, CityInfo> cityDict) throws JSONException {
        JSONArray array = new JSONArray();
        String[] keyArray = cityDict.keySet().toArray(new String[0]);
        for (int i = 0; i < cityDict.size(); i++) {
            array.put(i, new JSONObject().put("City", cityDict.get(keyArray[i]).name())
                    .put("Amount", cityDict.get(keyArray[i]).count())
                    .put("Percent", cityDict.get(keyArray[i]).percent()));
        }

        return new JSONObject().put("Data", array);
    }
}

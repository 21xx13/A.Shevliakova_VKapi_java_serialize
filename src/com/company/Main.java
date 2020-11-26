package com.company;

import org.json.*;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.*;
import java.sql.*;
import java.util.Map;
import java.util.TreeMap;


public class Main {
    public static void main(String[] args) throws IOException, JSONException {
        //получение данных
        URL url = new URL("https://api.vk.com/method/friends.get?user_id=85225806&fields=city&access_token=f5493d1ef5493d1ef5493d1e03f53ddc23ff549f5493d1eaae58a467485251565e27d2a&v=5.126");
        URLConnection yc = url.openConnection();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        yc.getInputStream()));
        String inputLine = readAll(in);
        in.close();
        //чтение списка друзей и их количества
        JSONObject json = new JSONObject(inputLine);

        System.out.println(json);
        int count = (int) json.getJSONObject("response").get("count");
        JSONArray friends = json.getJSONObject("response").getJSONArray("items");
        //заполнение словаря данными
        Map<String, CityInfo> filledDict = fillDictionary(friends, count);
        System.out.println(jsonFromDict(filledDict).toString());
        createJSONFile(jsonFromDict(filledDict).toString());


        // ==== Чтение только что созданного файла и запись в БД ====
        JSONObject dataToDB = readJsonFile("src/data2.json");
        System.out.println(dataToDB.toString());
        JSONArray arr = (JSONArray) dataToDB.get("Data");  //извлечение массива с городами
        int id = 196901000;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
            Connection conn = getConnection(); // подключение к БД
            Statement statement = conn.createStatement();
            createTable(statement);  // создание новой таблицы
            String sqlInsert = "INSERT NewTable(CityName, CountFriends, IdUser) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sqlInsert); // шаблон запроса SQL
            // обход массива с данными из файла
            for (int i = 0; i < arr.length(); i++) {
                JSONObject item = (JSONObject) arr.get(i);
                //заполнение шаблона
                preparedStatement.setString(1, item.getString("City"));
                preparedStatement.setInt(2, item.getInt("Amount"));
                preparedStatement.setInt(3, id);
                // выполнение запроса
                preparedStatement.executeUpdate();
            }

            String sqlSelect = "SELECT * FROM City";
            Statement st = conn.createStatement();
            ResultSet result = st.executeQuery(sqlSelect);
            while (result.next()){
                System.out.println("City " + result.getString("CityName") + " Percent: "
                        + result.getString("Percent") + " Количество друзей " + result.getString("CountFriends"));

            }



            conn.close();
        } catch (Exception ignored) { ignored.printStackTrace();
        }
    }

    private static JSONObject readJsonFile(String path) throws IOException, JSONException {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            return new JSONObject(reader.readLine());
        }
    }


    private static void createTable(Statement statement) throws SQLException {
        String sqlCommand = "CREATE TABLE IF NOT EXISTS NewTable (Id INT PRIMARY KEY AUTO_INCREMENT, CityName VARCHAR(255), " +
                "CountFriends INT, IdUser INT)";
        statement.executeUpdate(sqlCommand);
    }


    public static Connection getConnection() throws SQLException {
        String[] connectData = new String[3];
        try { //чтение паролей, логинов для подключения к БД из отдельного файла, которого по понятным причинам в репозитории нет
            File file = new File("src/database_properties.txt");
            FileReader fr = new FileReader(file);
            BufferedReader reader = new BufferedReader(fr);
            for (int i = 0; i < 3; i++)
                connectData[i] = reader.readLine();
        } catch (Exception ignored) {
        }
        return DriverManager.getConnection(connectData[0], connectData[1], connectData[2]);
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

    //создание файла .json
    private static void createJSONFile(String data) throws IOException {
        Files.createFile(Paths.get("src/data2.json"));
        FileWriter fw = new FileWriter("src/data2.json");
        fw.write(data);
        fw.close();
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
}

package agenda;

import java.io.*;
import java.net.*;
import java.util.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class FirebaseService {

    private static final String BASE_URL = "https://agendasaap-default-rtdb.firebaseio.com/contatos.json";
    private static final Gson gson = new Gson();

    public static Map<String, Contato> listarContatos() throws IOException {
        URL url = new URL(BASE_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        Type tipo = new TypeToken<Map<String, Contato>>() {
        }.getType();
        Map<String, Contato> contatos = gson.fromJson(response.toString(), tipo);
        return contatos != null ? contatos : new HashMap<>();
    }

    public static void salvarContato(Contato c) throws IOException {
        URL url = new URL(BASE_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String json = gson.toJson(c);
        OutputStream os = conn.getOutputStream();
        os.write(json.getBytes());
        os.flush();
        os.close();

        conn.getResponseCode();
    }
    

    public static void excluirContato(String id) throws IOException {
        String urlStr = "https://agendasaap-default-rtdb.firebaseio.com/contatos/" + id + ".json";
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.getResponseCode();
    }

}

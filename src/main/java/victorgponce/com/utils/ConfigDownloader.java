package victorgponce.com.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class ConfigDownloader {

    public static void configDownloader(String fileUrl, String outputFilePath) throws IOException {
        // Crear objeto URL a partir de la dirección del archivo
        URL url = new URL(fileUrl);

        // Abrir la conexión HTTP a la URL
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        // Verificar si la conexión fue exitosa (código de estado HTTP 200)
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Error: No se pudo descargar el archivo. Código de respuesta HTTP: " + responseCode);
        }

        // Crear flujos de entrada y salida
        try (InputStream inputStream = connection.getInputStream();
             FileOutputStream fileOutputStream = new FileOutputStream(outputFilePath);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {

            // Leer los datos del InputStream y escribirlos en el archivo local
            byte[] buffer = new byte[4096]; // Buffer para leer datos
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                bufferedOutputStream.write(buffer, 0, bytesRead);
            }

            System.out.println("El archivo ZIP se descargó correctamente: " + outputFilePath);
        } catch (IOException e) {
            throw new IOException("Error al descargar el archivo: " + e.getMessage(), e);
        }
    }

}

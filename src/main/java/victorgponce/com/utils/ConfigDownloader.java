package victorgponce.com.utils;

import victorgponce.com.Launcher;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipFile;

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

            Launcher.getInstance().getLogger().info("El archivo ZIP se descargó correctamente: " + outputFilePath + " Procediendo a la descompresión");
        } catch (IOException e) {
            throw new IOException("Error al descargar el archivo: " + e.getMessage(), e);
        }
    }

    public static void descomprimir(String archivoZip, String carpetaDestino) throws IOException {
        // Crear un objeto ZipFile para abrir el archivo ZIP
        try (ZipFile zipFile = new ZipFile(archivoZip)) {
            // Iterar sobre todas las entradas del archivo ZIP
            zipFile.stream().forEach(entry -> {
                // Crear una ruta completa para el archivo extraído
                File archivoExtraido = new File(carpetaDestino, entry.getName());

                // Crear las carpetas necesarias para los archivos que están dentro del ZIP
                try {
                    if (entry.isDirectory()) {
                        archivoExtraido.mkdirs();
                    } else {
                        // Crear los directorios del archivo si no existen
                        archivoExtraido.getParentFile().mkdirs();

                        // Extraer el archivo
                        try (InputStream inputStream = zipFile.getInputStream(entry);
                             FileOutputStream fileOutputStream = new FileOutputStream(archivoExtraido);
                             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {

                            byte[] buffer = new byte[4096]; // Buffer de lectura
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                bufferedOutputStream.write(buffer, 0, bytesRead);
                            }
                        }
                    }
                } catch (IOException e) {
                    Launcher.getInstance().getLogger().err("Error al extraer el archivo: " + entry.getName());
                    e.printStackTrace();
                }
            });
        }
    }

}

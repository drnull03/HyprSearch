package HyprSearch;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class HttpClientUtil {

    public static byte[] postRaw(String urlString, String body) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);

        try {
            byte[] out = body.getBytes(StandardCharsets.UTF_8);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(out);
                os.flush();
            }

            int responseCode = conn.getResponseCode();

            if (responseCode != 200) {
                try (InputStream err = conn.getErrorStream()) {
                    if (err != null) {
                        System.err.println("Worker error response: " + new String(err.readAllBytes()));
                    }
                }
                return new byte[0];
            }

            try (InputStream in = conn.getInputStream()) {
                return in.readAllBytes();
            }
        } finally {
            conn.disconnect();
        }
    }
}

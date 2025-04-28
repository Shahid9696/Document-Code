import javax.net.ssl.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;

public class HttpsPostWithoutCertValidation {
    
    public static void main(String[] args) {
        try {
            // Disable SSL certificate validation
            disableSslValidation();
            
            // API endpoint URL
            String apiUrl = "https://api.example.com/endpoint";
            
            // JSON payload
            String jsonPayload = "{\"key1\":\"value1\",\"key2\":\"value2\"}";
            
            // Make the POST request
            String response = sendHttpsPostRequest(apiUrl, jsonPayload);
            
            // Print the response
            System.out.println("Response: " + response);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void disableSslValidation() throws Exception {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return null; }
                public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                public void checkServerTrusted(X509Certificate[] certs, String authType) { }
            }
        };
        
        // Install the all-trusting trust manager
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        
        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }
    
    private static String sendHttpsPostRequest(String apiUrl, String jsonPayload) throws IOException {
        URL url = new URL(apiUrl);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        
        try {
            // Configure the connection
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            
            // Send the request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // Get response code
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);
            
            // Read the response
            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    responseCode >= 200 && responseCode < 300 ? 
                    connection.getInputStream() : connection.getErrorStream(), 
                    StandardCharsets.UTF_8))) {
                
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } finally {
            connection.disconnect();
        }
    }
}
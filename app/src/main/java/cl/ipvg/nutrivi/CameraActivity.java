package cl.ipvg.nutrivi;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class CameraActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_camera);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, 100);
        } else {
            Toast.makeText(this, "No se encontró aplicación de cámara", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            if (data != null && data.getExtras() != null) {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                if (imageBitmap != null) {
                    Toast.makeText(this, "Analizando comida con IA...", Toast.LENGTH_LONG).show();
                    analizarComidaConIA(imageBitmap);
                }
            }
        } else {
            finish();
        }
    }

    private void analizarComidaConIA(Bitmap bitmap) {
        new Thread(() -> {
            try {
                java.io.ByteArrayOutputStream byteArrayOutputStream = new java.io.ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                String base64Image = Base64.encodeToString(byteArray, Base64.NO_WRAP);

                String openAiKey = BuildConfig.OPENAI_API_KEY;
                String geminiKey = BuildConfig.GEMINI_API_KEY;

                if (openAiKey != null && !openAiKey.trim().isEmpty()) {
                    analizarConOpenAi(base64Image, openAiKey);
                } else if (geminiKey != null && !geminiKey.trim().isEmpty()) {
                    analizarConGemini(base64Image, geminiKey);
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Configura OPENAI_API_KEY o GEMINI_API_KEY en local.properties", Toast.LENGTH_LONG).show();
                        finish();
                    });
                }
            } catch (Exception e) {
                Log.e("CameraActivity", "Excepción procesando imagen", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        }).start();
    }

    private void analizarConOpenAi(String base64Image, String apiKey) {
        try {
            URL url = new URL("https://api.openai.com/v1/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey.trim());
            conn.setDoOutput(true);

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("model", "gpt-4o-mini");
            jsonBody.put("max_tokens", 300);

            JSONArray messages = new JSONArray();
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");

            JSONArray contentArray = new JSONArray();

            JSONObject textObj = new JSONObject();
            textObj.put("type", "text");
            textObj.put("text", "Analiza esta imagen. Si contiene cualquier tipo de comida, bebida o alimento, identifica el nombre del plato o bebida y estima las calorías totales. Si no es comida ni bebida, indica 'Alimento o bebida no detectado'. Responde estrictamente en este formato exacto: Plato: [Nombre del plato o bebida] | Calorías: [Número] kcal.");
            contentArray.put(textObj);

            JSONObject imageObj = new JSONObject();
            imageObj.put("type", "image_url");
            JSONObject urlObj = new JSONObject();
            urlObj.put("url", "data:image/jpeg;base64," + base64Image);
            imageObj.put("image_url", urlObj);
            contentArray.put(imageObj);

            userMessage.put("content", contentArray);
            messages.put(userMessage);
            jsonBody.put("messages", messages);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray choices = jsonResponse.getJSONArray("choices");
                JSONObject firstChoice = choices.getJSONObject(0);
                JSONObject message = firstChoice.getJSONObject("message");
                String resultText = message.getString("content");

                runOnUiThread(() -> mostrarResultadoIA(resultText));
            } else {
                try (BufferedReader errIn = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder errSb = new StringBuilder();
                    String errLine;
                    while ((errLine = errIn.readLine()) != null) {
                        errSb.append(errLine);
                    }
                    Log.e("ChatGPTError", "Error OpenAI [" + responseCode + "]: " + errSb);
                } catch (Exception ignored) {}

                runOnUiThread(() -> {
                    Toast.makeText(this, "Error en ChatGPT (Código: " + responseCode + "). Revisa tu saldo o API Key de OpenAI.", Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        } catch (Exception e) {
            Log.e("ChatGPTError", "Excepción OpenAI", e);
            runOnUiThread(() -> {
                Toast.makeText(this, "Error conectando con ChatGPT: " + e.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            });
        }
    }

    private void analizarConGemini(String base64Image, String apiKey) {
        String[] endpoints = {
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=",
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=",
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent?key=",
            "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent?key="
        };

        try {
            JSONObject jsonBody = new JSONObject();
            JSONArray contentsArray = new JSONArray();
            JSONObject contentObj = new JSONObject();
            JSONArray partsArray = new JSONArray();

            JSONObject textPart = new JSONObject();
            textPart.put("text", "Analiza esta imagen. Si contiene cualquier tipo de comida, bebida o alimento, identifica el nombre del plato o bebida y estima las calorías totales. Si no es comida ni bebida, indica 'Alimento o bebida no detectado'. Responde estrictamente en este formato exacto: Plato: [Nombre del plato o bebida] | Calorías: [Número] kcal.");
            partsArray.put(textPart);

            JSONObject imagePart = new JSONObject();
            JSONObject inlineData = new JSONObject();
            inlineData.put("mime_type", "image/jpeg");
            inlineData.put("data", base64Image);
            imagePart.put("inline_data", inlineData);
            partsArray.put(imagePart);

            contentObj.put("parts", partsArray);
            contentsArray.put(contentObj);
            jsonBody.put("contents", contentsArray);

            boolean exito = false;
            int ultimoCodigo = 404;

            for (String baseEndpoint : endpoints) {
                try {
                    String urlString = baseEndpoint + apiKey.trim();
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);

                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = jsonBody.toString().getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }

                    int responseCode = conn.getResponseCode();
                    ultimoCodigo = responseCode;

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                        StringBuilder response = new StringBuilder();
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();

                        JSONObject jsonResponse = new JSONObject(response.toString());
                        JSONArray candidates = jsonResponse.getJSONArray("candidates");
                        JSONObject firstCandidate = candidates.getJSONObject(0);
                        JSONObject content = firstCandidate.getJSONObject("content");
                        JSONArray parts = content.getJSONArray("parts");
                        String resultText = parts.getJSONObject(0).getString("text");

                        exito = true;
                        runOnUiThread(() -> mostrarResultadoIA(resultText));
                        break;
                    }
                } catch (Exception e) {
                    Log.e("GeminiError", "Error intentando endpoint: " + baseEndpoint, e);
                }
            }

            if (!exito) {
                final int code = ultimoCodigo;
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error en Gemini (Código: " + code + ")", Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        } catch (Exception e) {
            Log.e("GeminiError", "Excepción Gemini", e);
            runOnUiThread(() -> {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            });
        }
    }

    private void mostrarResultadoIA(String resultado) {
        new AlertDialog.Builder(this)
                .setTitle("Resultado del Análisis IA")
                .setMessage(resultado)
                .setPositiveButton("Registrar", (dialog, which) -> {
                    Toast.makeText(this, "¡Calorías registradas con éxito!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Repetir foto", (dialog, which) -> {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePictureIntent, 100);
                })
                .setCancelable(false)
                .show();
    }
}

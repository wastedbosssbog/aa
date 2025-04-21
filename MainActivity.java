package com.example.autotyper;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {

    private EditText tokenInput, channelInput, messageInput, delayInput;
    private boolean running = false;
    private Thread autoThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tokenInput = findViewById(R.id.tokenInput);
        channelInput = findViewById(R.id.channelInput);
        messageInput = findViewById(R.id.messageInput);
        delayInput = findViewById(R.id.delayInput);
        Button startButton = findViewById(R.id.startButton);
        Button stopButton = findViewById(R.id.stopButton);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String userToken = tokenInput.getText().toString().trim();
                final String channelId = channelInput.getText().toString().trim();
                final String message = messageInput.getText().toString().trim();
                final String delayStr = delayInput.getText().toString().trim();

                if (userToken.isEmpty() || channelId.isEmpty() || message.isEmpty() || delayStr.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Completează toate câmpurile!", Toast.LENGTH_SHORT).show();
                    return;
                }

                final int delay = Integer.parseInt(delayStr) * 1000;
                running = true;

                autoThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (running) {
                            sendDiscordMessage(userToken, channelId, message);
                            try {
                                Thread.sleep(delay);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                autoThread.start();
                Toast.makeText(MainActivity.this, "Autotyper pornit!", Toast.LENGTH_SHORT).show();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                running = false;
                if (autoThread != null && autoThread.isAlive()) {
                    autoThread.interrupt();
                }
                Toast.makeText(MainActivity.this, "Autotyper oprit!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendDiscordMessage(String token, String channelId, String mesaj) {
        try {
            URL url = new URL("https://discord.com/api/v9/channels/" + channelId + "/messages");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", token);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonPayload = "{\"content\":\"" + mesaj + "\"}";
            OutputStream os = conn.getOutputStream();
            os.write(jsonPayload.getBytes("UTF-8"));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            System.out.println("Discord response: " + responseCode);

            conn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

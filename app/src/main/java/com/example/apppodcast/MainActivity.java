package com.example.apppodcast;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    MqttAndroidClient client;

    String serverURL = "tcp://test.mosquitto.org:1883";
    String topic = "mqtt/topic";
    String sTopic = "mqtt/sensorData";

    EditText textToSend,txt_topic,txt_sub;
    Button btn_send,btn_sub;

    PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        textToSend=findViewById(R.id.mess);
        txt_sub=findViewById(R.id.sub);
        txt_topic=findViewById(R.id.topic);
        btn_sub=findViewById(R.id.btn_sub);
        btn_send=findViewById(R.id.btn_send);

        txt_topic.setText("Nova 3i");
        textToSend.setText("test bgsgg");
        txt_sub.setText("Nova 3i");

        sTopic=txt_sub.getText().toString();
        topic=txt_topic.getText().toString();

        createPendingIntent();
        connect_mqtt();

        btn_sub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                topic=txt_topic.getText().toString().trim();
                sTopic=txt_sub.getText().toString().trim();
                subscribeToTopic(sTopic);
            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                topic=txt_topic.getText().toString().trim();
                sTopic=txt_sub.getText().toString().trim();
                sendMessage(topic);
            }
        });
    }
    private void createPendingIntent() {
        if (pendingIntent == null) {

            Activity activity = MainActivity.this;
            Intent intent = new Intent(activity, activity.getClass());

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                pendingIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_IMMUTABLE);
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                pendingIntent = PendingIntent.getActivity(activity, 0, intent, 0);
            }
        }
    }

    public void connect_mqtt(){
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), serverURL, clientId);

        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    subscribeToTopic(sTopic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    void sendMessage(String topic) {
        String payload = textToSend.getText().toString();
        byte[] encodedPayload;
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            client.publish(topic, message);
            Toast.makeText(getApplicationContext(), "Sent", Toast.LENGTH_SHORT).show();
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribeToTopic(String topic) {
        try {
            if (client.isConnected()) {
                client.subscribe(topic, 0);
                Toast.makeText(getApplicationContext(), "Subscribed", Toast.LENGTH_SHORT).show();
                client.setCallback(new MqttCallback() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void connectionLost(Throwable cause) {
                        Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        Toast.makeText(getApplicationContext(), topic+": "+message, Toast.LENGTH_SHORT).show();
                        Log.d("AAA",topic+": "+message);
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                    }
                });
            }
        } catch (Exception ignored) {
        }
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (connectionFlag) {
//            try {
//                IMqttToken disconnectToken = client.disconnect();
//                disconnectToken.setActionCallback(new IMqttActionListener() {
//                    @Override
//                    public void onSuccess(IMqttToken asyncActionToken) {
//                        Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT).show();
//                        finish();
//                    }
//
//                    @Override
//                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                        finish();
//                    }
//                });
//            } catch (MqttException e) {
//                e.printStackTrace();
//            }
//            connectionFlag = false;
//        }
//    }

}
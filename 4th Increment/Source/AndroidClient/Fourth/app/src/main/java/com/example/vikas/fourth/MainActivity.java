package com.example.vikas.fourth;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.awt.*;
import javax.imageio.*;


import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.Bitmap.*;
import android.graphics.Bitmap.*;


public class MainActivity extends Activity {

    TextView info, infoip, msg;
    EditText txtSpeechInput;
    Button send;
    String message = "", testString = "";
    ServerSocket serverSocket;
    private ImageButton btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private final int REQ_CODE_CAMERA=200;
    boolean checkUpdate = true;
    Button cameraClick;
    ImageView displayImage;
    Bitmap bmp;
    byte[] array;
    int start=0;
    int len =0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        info = (TextView) findViewById(R.id.info);
        infoip = (TextView) findViewById(R.id.infoip);
        msg = (TextView) findViewById(R.id.msg);
        send = (Button) findViewById(R.id.send);
        txtSpeechInput = (EditText) findViewById(R.id.testData);

        displayImage = (ImageView) findViewById(R.id.imageView);
        cameraClick = (Button) findViewById(R.id.camclick);
        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });

        cameraClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQ_CODE_CAMERA);
            }
        });

        infoip.setText(getIpAddress());

        send.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                testString = txtSpeechInput.getText().toString();
                
                ContextWrapper cw = new ContextWrapper(getApplicationContext());
                // path to /data/data/yourapp/app_data/imageDir
                File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
                // Create imageDir
                File mypath=new File(directory,"profile.jpg");

                FileOutputStream fos = null;
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                try {
                    fos = new FileOutputStream(mypath);
                    // Use the compress method on the BitMap object to write image to the OutputStream
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    //bmp.compress(CompressFormat.JPEG, 70, stream);


                    bmp.compress(Bitmap.CompressFormat.PNG, 20, stream);
                    array = stream.toByteArray();
                    len = array.length;
                    Toast.makeText(getApplicationContext(), "image stored", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        Thread socketServerThread = new Thread(new SocketServerThread());
        socketServerThread.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txtSpeechInput.setText(result.get(0));
                }
                break;
            }

            case REQ_CODE_CAMERA: {

                bmp = (Bitmap) data.getExtras().get("data");
                displayImage.setImageBitmap(bmp);
                break;
            }

        }
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private class SocketServerThread extends Thread {

        static final int SocketServerPORT = 9999;
        int count = 0;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(SocketServerPORT);
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        info.setText("I'm waiting here: "
                                + serverSocket.getLocalPort());
                    }
                });

                while (true) {
                    Socket socket = serverSocket.accept();
                    count++;

                    message += "#" + count + " from " + socket.getInetAddress()
                            + ":" + socket.getPort() + "\n";

                    MainActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            msg.setText(message);
                        }
                    });

                    SocketServerReplyThread socketServerReplyThread = new SocketServerReplyThread(
                            socket, count);
                    socketServerReplyThread.run();

                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    private class SocketServerReplyThread extends Thread {

        private Socket hostThreadSocket;
        int cnt;

        SocketServerReplyThread(Socket socket, int c) {
            hostThreadSocket = socket;
            cnt = c;
        }

        @Override
        public void run() {

            if (checkUpdate) {

                OutputStream outputStream;


                try {
                    outputStream = hostThreadSocket.getOutputStream();
                   // PrintStream printStream = new PrintStream(outputStream);
                    DataOutputStream dos = new DataOutputStream(outputStream);
                    //bmp.compress(Bitmap.CompressFormat.PNG, 100, dos);

                   // printStream.print(testString);
                    //printStream.close();
                    dos.writeInt(len);
                    if (len > 0) {
                        dos.write(array, start, len);
                    }


                    message += "replayed: " + testString + "\n";
                    testString = "";
                    MainActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            msg.setText(message);
                        }
                    });

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    message += "Something wrong! " + e.toString() + "\n";
                }

                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        msg.setText(message);
                    }
                });
            }
        }

    }

    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "SiteLocalAddress: "
                                + inetAddress.getHostAddress() + "\n";
                    }

                }

            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }
}

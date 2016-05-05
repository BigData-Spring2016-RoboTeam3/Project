package com.example.vikas.androidfinal;

import android.app.Activity;

import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NavUtils;
import android.telephony.SmsManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;




public class MainActivity extends Activity {

    TextView info, info2, infoip, msg;
    EditText txtSpeechInput;
    Button send;
    Button send2;
    String message = "", testString = "";
    ServerSocket serverSocket, serverSocket2, serverSocket22, serverSocket23;
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
    String res2 = null;
    String res3 = null;
    String res4 = null;
    TextToSpeech t1;
    SmsManager sms = SmsManager.getDefault();
    String SENT = "SMS_SENT";
    String DELIVERED = "SMS_DELIVERED";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        info = (TextView) findViewById(R.id.info);
        info2 = (TextView) findViewById(R.id.info2);
        infoip = (TextView) findViewById(R.id.infoip);
        msg = (TextView) findViewById(R.id.msg);
        send = (Button) findViewById(R.id.send);
        send2 = (Button) findViewById(R.id.send2);
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

        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {

                    t1.setLanguage(Locale.US);
                }
            }
        });

        infoip.setText(getIpAddress());

        send2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub


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


                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    array = stream.toByteArray();
                    len = array.length;
                    Toast.makeText(getApplicationContext(), "image stored", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        send.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub


                testString = txtSpeechInput.getText().toString();

            }
        });
        Thread socketServerThread = new Thread(new SocketServerThread());
        socketServerThread.start();
        Thread socketServerThread2 = new Thread(new SocketServerThread2());
        socketServerThread2.start();
        Thread socketServerThread3 = new Thread(new SocketServerThread3());
        socketServerThread3.start();
        Thread socketServerThread4 = new Thread(new SocketServerThread4());
        socketServerThread4.start();
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



    private class SocketServerThread4 extends Thread {


        static final int SocketServerPORT4 = 9996;
        int count = 0;
        public DataInputStream din;


        @Override
        public void run() {
            try {

                serverSocket23 = new ServerSocket(SocketServerPORT4);
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                      /*  info.setText("I'm waiting here: "
                                + serverSocket.getLocalPort());*/
                    }
                });

                while (true) {

                    Socket socket2 = serverSocket23.accept();
                    count++;



                    try {
                        din = new DataInputStream(socket2.getInputStream());
                        res4 = din.readLine();

                    }
                    catch(IOException e){
                        e.printStackTrace();
                    }
                    MainActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            //msg.setText(message);
                            if (res4!=null){
                                Toast.makeText(getApplicationContext(), res4, Toast.LENGTH_LONG).show();
                                sms.sendTextMessage("8167453142", null, "your friend is in "+res4+" mood", null, null);
                            }
                        }
                    });



                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    private class SocketServerThread3 extends Thread {


        static final int SocketServerPORT2 = 9997;
        int count = 0;
        public DataInputStream din;


        @Override
        public void run() {
            try {

                serverSocket22 = new ServerSocket(SocketServerPORT2);
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                      /*  info.setText("I'm waiting here: "
                                + serverSocket.getLocalPort());*/
                    }
                });

                while (true) {

                    Socket socket2 = serverSocket22.accept();
                    count++;



                    try {
                        din = new DataInputStream(socket2.getInputStream());
                        res3 = din.readLine();

                    }
                    catch(IOException e){
                        e.printStackTrace();
                    }
                    MainActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            //msg.setText(message);
                            if (res3!=null){
                                Toast.makeText(getApplicationContext(), res3, Toast.LENGTH_LONG).show();
                                sms.sendTextMessage("8167453142", null, "your friend is in "+res3+" mood", null, null);
                            }
                        }
                    });



                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    private class SocketServerThread2 extends Thread {

        static final int SocketServerPORT = 9998;

        int count = 0;
        public DataInputStream din;
        OutputStream os;
        int test = 0;
        PrintStream printStream;
        Socket mr;

        @Override
        public void run() {
            try {
                serverSocket2 = new ServerSocket(SocketServerPORT);

                //DataOutputStream dos = new DataOutputStream(outputStream);
                //bmp.compress(Bitmap.CompressFormat.PNG, 100, dos);




                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        info2.setText("I'm waiting here: "
                                + serverSocket2.getLocalPort());
                    }
                });

                while (true) {
                    Socket socket = serverSocket2.accept();

                    count++;

                    message += "#" + count + " from " + socket.getInetAddress()
                            + ":" + socket.getPort() + "\n";
                    System.out.println(message);
                    try {
                        din = new DataInputStream(socket.getInputStream());
                        res2 = din.readUTF();

                    }
                    catch(IOException e){
                        e.printStackTrace();
                    }

                    MainActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            //msg.setText(message);
                            if (res2.equals("angry")||res2.equals("happy")){
                                if(res2.equals("happy")){
                                Toast.makeText(getApplicationContext(), res2, Toast.LENGTH_LONG).show();
                                sms.sendTextMessage("8167453142", null, "your friend is "+res2, null, null);

                                }


                                else{
                                    Toast.makeText(getApplicationContext(), "Sad", Toast.LENGTH_LONG).show();
                                    sms.sendTextMessage("8167453142", null, "your friend is sad", null, null);
                                }

                            }
                        }
                    });
                    if(res2.equals("happy")) {
                        try {
                            mr = new Socket("10.151.7.5", 9994);
                            os = mr.getOutputStream();
                            printStream = new PrintStream(os);
                            test = 1;
                            printStream.print(res2);
                            System.out.println(res2);
                            printStream.close();
                            test = 0;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }


                    SocketServerReplyThread2 socketServerReplyThread2 = new SocketServerReplyThread2(
                            socket, count);
                    socketServerReplyThread2.run();

                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }
    private class SocketServerThread extends Thread {

        static final int SocketServerPORT = 9999;
        static final int SocketServerPORT2 = 9997;
        int count = 0;
        public DataInputStream din;


        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(SocketServerPORT);
               //serverSocket22 = new ServerSocket(SocketServerPORT2);
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        info.setText("I'm waiting here: "
                                + serverSocket.getLocalPort());
                    }
                });

                while (true) {
                    Socket socket = serverSocket.accept();
                    //Socket socket2 = serverSocket22.accept();
                    count++;

                    message += "#" + count + " from " + socket.getInetAddress()
                            + ":" + socket.getPort() + "\n";

                   /* try {
                        din = new DataInputStream(socket2.getInputStream());
                        res = din.readUTF();

                    }
                    catch(IOException e){
                        e.printStackTrace();
                    }*/
                    MainActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                           // msg.setText(message);
                            /*if (res.equals("angry")||res.equals("happy")){
                                Toast.makeText(getApplicationContext(), res, Toast.LENGTH_LONG).show();
                                sms.sendTextMessage("9528553045", null, "your friend is"+res, null, null);
                            }*/
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



    private class SocketServerReplyThread2 extends Thread {

        private Socket hostThreadSocket;
        int cnt;

        SocketServerReplyThread2(Socket socket, int c) {
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
                    //dos.writeInt(len);
                    int flag = 0;
                    if (len > 0) {
                        dos.write(array, start, len);
                        flag =1;
                    }
                    if (flag == 1)
                        System.out.println("image transferred");

                    message += "replayed: " + testString + "\n";
                    testString = "";
                    MainActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            //msg.setText(message);


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
                        //msg.setText(message);
                    }
                });
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
                     PrintStream printStream = new PrintStream(outputStream);
                    //DataOutputStream dos = new DataOutputStream(outputStream);
                    //bmp.compress(Bitmap.CompressFormat.PNG, 100, dos);

                     printStream.print(testString);
                    System.out.println(testString);
                    printStream.close();



                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    message += "Something wrong! " + e.toString() + "\n";
                }

                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                       // msg.setText(message);
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


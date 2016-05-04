/**
 * Created by vikas on 5/2/2016.
 */

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class SocketConn {

    public Socket s;
    public DataInputStream din;
    public DataOutputStream dout;

    public void initialize(){
        try {
            s=new Socket("192.168.1.182",9999);
            System.out.println(s);
            din=new DataInputStream(s.getInputStream());
            dout=new DataOutputStream(s.getOutputStream());
            dout.writeUTF("Hello");
            SocketServerReplyThread t = new SocketServerReplyThread();
            t.run();
        }catch(Exception e)
        {
            System.out.println(e);
        }
    }
    public void receiveImage(){


    }

    private class SocketServerReplyThread extends Thread{

        int i;

        public void run() {
            try {
                File file = new File("image2.png");
                FileOutputStream fout = new FileOutputStream(file);
                //receive and save image from client
                byte[] readData = new byte[1024];
                int ct = 0;
                while ((i = din.read(readData)) != -1) {
                    fout.write(readData, 0, i);
                    System.out.println("image writing");
                }
                fout.flush();
                fout.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

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
            s=new Socket("10.151.5.55",9998);
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
    public void initialize2(String res){

        try {
            s=new Socket("10.151.5.55",9998);
            System.out.println(s);

            dout=new DataOutputStream(s.getOutputStream());
            dout.writeUTF(res);

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

                File file = new File("data/test2","image2.jpg");
                FileOutputStream fout = new FileOutputStream(file);
                //receive and save image from client
                byte[] readData = new byte[1000000];
                int ct = 0;
                while ((i = din.read(readData)) != -1) {
                    fout.write(readData, 0, i);
                    System.out.println("image writing");

                }
                fout.flush();
                fout.close();
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

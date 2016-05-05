package com.umkc;

/**
 * Created by sowmy on 5/4/2016.
 */
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class SocketConn {

    public ServerSocket s;
    public DataInputStream din;
    public DataOutputStream dout;
    String op;

    public String initialize(){
        try {
            s=new ServerSocket(9998);
            Socket s1 = s.accept();
            System.out.println(s);
            din=new DataInputStream(s1.getInputStream());
             op = din.readLine();


        }catch(Exception e)
        {
            System.out.println(e);
        }
        return op;
    }


}

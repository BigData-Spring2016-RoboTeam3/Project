/**
  * Created by sowmy on 5/4/2016.
  */
package com.umkc

import java.io._
import java.net._

/**
  * Created by Mayanka on 20-Jul-15.
  */
object socket {
  def findIpAdd():String =
  {
    val localhost = InetAddress.getLocalHost
    val localIpAddress = localhost.getHostAddress

    return localIpAddress
  }
  def sendCommandToRobot(string: String)
  {
    // Simple server

    try {


      lazy val address: Array[Byte] = Array(10.toByte, 151.toByte, 5.toByte, 55.toByte)
      val ia = InetAddress.getByAddress(address)
      val socket = new Socket(ia, 9996)
      val out = new PrintStream(socket.getOutputStream)
      //val in = new DataInputStream(socket.getInputStream())

      out.print(string)
      out.flush()

      out.close()
      //in.close()
      socket.close()
    }
    catch {
      case e: IOException =>
        e.printStackTrace()
    }
  }

}

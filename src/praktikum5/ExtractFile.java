/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package praktikum5;

/**
 *
 * @author s-nnguy1
 */
import java.io.*;
import java.util.*;
import java.net.*;

public class ExtractFile {
   public static void main(String args[]) throws IOException, URISyntaxException
   {
      URL url = new URL("https://moodle.haw-landshut.de/pluginfile.php/128095/mod_resource/content/1/ergebnis-mapservertest.txt");
      InputStream is = url.openStream();
      BufferedInputStream bis = new BufferedInputStream(is);
      bis.
   }
}

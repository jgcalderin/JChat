package com.bossae.chat.file;

import java.io.*;

public class FileHelper {

    public static byte[] fileToBytes(String path) throws IOException, FileNotFoundException
    {
        try
        {
            File f = new File(path);
            if (f.length() == 0) throw new IOException("File length is zero: " + path);

            byte[] bytecodes = new byte[(int) f.length()];
            new DataInputStream(new FileInputStream(f)).readFully(bytecodes);
            return bytecodes;

        }
        catch (IOException e){
            return ("Error: " + e.getMessage()).getBytes();
        }
    }
}

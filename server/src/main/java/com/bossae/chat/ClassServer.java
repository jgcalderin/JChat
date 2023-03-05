package com.bossae.chat;

import com.bossae.chat.command.Command;
import com.bossae.chat.net.SocketHelper;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import java.io.*;
import java.net.*;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ClassServer implements Runnable {

    public static String docroot = "/home/pantu/Documentos"; //TODO: extract to properties file or something
    private static int DefaultServerPort = 2001;
    private static int port = DefaultServerPort;
    private static String socketType = "plain"; //TODO: Enumeration here: plain|SSL|TSL

    private ServerSocket serverSocket = null;
    private Stack<Long> stackT = new Stack<Long>();

    private static List<ChatClientPojo> clients = new ArrayList<ChatClientPojo>();

    public static void main(String[] args)
    {
        if (args.length >= 1) port = Integer.parseInt(args[0]);
        if (args.length >= 2) socketType = args[1];
        try {
            ServerSocketFactory ssf = SocketHelper.getServerSocketFactory(socketType);
            ServerSocket ss = ssf.createServerSocket(port);
            if (args.length >= 3 && args[2].equals("true"))
                ((SSLServerSocket)ss).setNeedClientAuth(true);

            new ClassServer(ss);
        } catch (IOException | UnrecoverableKeyException | CertificateException | NoSuchAlgorithmException |
                 KeyStoreException | KeyManagementException e) {
            System.out.println("Unable to start ClassServer: " + e.getMessage());
            e.printStackTrace();
        }
    }
    protected ClassServer(ServerSocket ss) {
        serverSocket = ss;
        (new Thread(this)).start();
    }

    public void run() {
        Socket socket = null;
        PrintWriter outbound = null;
        ChatClientPojo client = null;

        System.out.println("Server listening on port " + serverSocket.getLocalPort());
        System.out.println("socket type: " + ClassServer.socketType);
        if (serverSocket instanceof  SSLServerSocket)
            System.out.println("client auth required? " + ((SSLServerSocket)serverSocket).getNeedClientAuth());
        System.out.println("INIT: new Thread: " + Thread.currentThread().hashCode());
        System.out.println("INIT: Total threads: " + Thread.activeCount());

        try {
            socket = serverSocket.accept();
            (new Thread(this)).start(); //for next client

            OutputStream rawOut = socket.getOutputStream();
            outbound = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    rawOut)));

            client = registerClient(socket,outbound);
            welcome(client);
            cast(client.getPort() + " joined the chat" , client, true);
            System.out.println("(New Client Joined: " + client.getPort() + ")");

            BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));

            while (true) {
                String line = in.readLine(); //blocks thread
                if (line == null) throw new ClientClosedConnection();

                Command c = Command.getCommand(line);
                if (c == null)
                {
                    cast(line, client);
                    continue;
                }
                String sResponse = c.execute();
                //outbound.print("server says >\r\n\t");
                //outbound.flush();
                rawOut.write("server>".getBytes());
                rawOut.write(sResponse.getBytes());
                rawOut.write("\r\n".getBytes()); //TODO: rawOut, what is it for?
                rawOut.flush();
                //out.write(64);//@
                //out.write(127);//DEL
                //out.write(64);//@
                //outbound.print("\r\n");
                //outbound.flush();
            }
        } catch (IOException ex) {
            System.out.println("error writing response: " + ex.getMessage());
            ex.printStackTrace();

        } catch (ClientClosedConnection e) {
            System.out.println("Client closed connection.");
        } finally {
            try {
                System.out.println("END: closing socket: " +
                        socket.getLocalPort() + ", " +
                        socket.getPort() + ", " +
                        Thread.currentThread().hashCode());
                socket.close();
                unregisterClient(client);
                System.out.println("Total threads: " + Thread.activeCount());
                System.out.println("Clients connected: " + clients.size());
            } catch (IOException e) {
                System.out.println("Error on finally: " + e.getMessage());
            }
        }
    }

    private synchronized void welcome(ChatClientPojo client) {
        client.getWriter().write("server> Welcome " + client.getClientId()+ "\r\n");
        client.getWriter().flush();
    }
    private void queue() {
        stackT.push(Thread.currentThread().threadId());
    }

    private synchronized void dequeue()
    {
        Stack<Long> st = new Stack<Long>();
        while (!stackT.isEmpty()) {
            st.push(stackT.pop());
        }
        st.pop();//element dequeued.
        //restore rest of stack:
        while (!st.isEmpty()) {
            stackT.push(st.pop());
        }
    }

    private synchronized boolean isMyTurn()
    {
        Stack<Long> st = new Stack<Long>();
        Stack<Long> stackTClone = (Stack<Long>) stackT.clone();
        while (!stackTClone.isEmpty()) {
            st.push(stackTClone.pop());
        }
        if (st.pop() == Thread.currentThread().threadId())
            return true;
        else
            return false;
    }
    private void cast(String msg, ChatClientPojo source) {
        cast(msg,source,false);
    }
    private void cast(String msg, ChatClientPojo source, boolean sourceIsServer)  {
        queue();
        while (!isMyTurn())
            Thread.yield();
         synchronized(this)
         {
             for (ChatClientPojo client: clients)
             {
                 String whosays = sourceIsServer?"server":Integer.toString(source.getPort());
                 if (!whosays.equalsIgnoreCase("server"))
                    if (source != null && client.hashCode() == source.hashCode()) continue; //skip myself

                 PrintWriter writer = client.getWriter();
                 writer.println(
                         whosays + " says> " +
                         msg);
                 writer.flush();
             }
             dequeue();
         }
    }

    private synchronized ChatClientPojo registerClient(Socket socket, PrintWriter out)
    {
        ChatClientPojo client = new ChatClientPojo(socket, out);
        clients.add(client);
        return client;
    }
    private void unregisterClient(ChatClientPojo client)
    {
        clients.remove(client);
        System.out.println("Client disconnected: " + client.getPort());
        cast("Client disconnected: " + client.getPort(),client,true);
    }
}

;
package org.uma.jmetal.util;

/*
 * Copyright (c) 2019 École Polytechnique
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Author(s): Arnab Sinha <arnab dot sinha at polytechnique dot edu>
 *
 * Description: This java code provides the socket client for communicating with the ZMQ socket server on the python side
 *
 */

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class SocClient {

    private String clientName;
    private String ipAddress;
    private int port;

    private Socket socket;
    private PrintStream outsocket;
    private Scanner insocket;

    private DataInputStream din;

    //private final static Logger LOGGER = Logger
    //.getLogger(ZMQClient.class.getName());

    public SocClient(String subscriberName, String ipAddress, int port){

        this.clientName = subscriberName;
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public void putMessage(String topic, String message){
        try {
            System.out.println("[Trying to connect to server]");
            this.socket = new Socket(this.ipAddress, this.port);
            this.outsocket = new PrintStream(this.socket.getOutputStream());
            this.insocket = new Scanner(this.socket.getInputStream());
            System.out.println( "Socket Client " + this.clientName + ":" + this.port + " connected");

        } catch (IOException e) {
            try {
                this.port += 100;
                this.socket = new Socket(this.ipAddress, this.port);
                this.outsocket = new PrintStream(this.socket.getOutputStream());
                this.insocket = new Scanner(this.socket.getInputStream());
                System.out.println( "Socket Client " + this.clientName + ":" + this.port + " connected");
            } catch (IOException ee) {

            }
        }

        this.outsocket.print(topic + " " + message);
        this.outsocket.flush();
    }

    public String getMessage(){
        String msg = this.insocket.nextLine();
        insocket.close();
        outsocket.close();
        return msg;
    }

    public String parseTopic(String receivedMessage){
        return receivedMessage.substring(0, receivedMessage.indexOf(' ')-1);
    }

    public String parseMessage(String receivedMessage){
        return receivedMessage.substring(receivedMessage.indexOf(' ')+1);
    }
}

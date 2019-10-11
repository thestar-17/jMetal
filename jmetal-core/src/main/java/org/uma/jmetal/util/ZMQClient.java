/*
 * Copyright (c) 2019 École Polytechnique
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Author(s): Arnab Sinha <arnab dot sinha at polytechnique dot edu>
 *
 * Description: This java code provides the ZMQ client for communicating with the ZMQ server on the python side
 *
 */

package org.uma.jmetal.util;

import org.zeromq.ZMQ;

public class ZMQClient {

    private String clientName;
    private String ipAddress;
    private int port;

    ZMQ.Context context;
    ZMQ.Socket client;

    //private final static Logger LOGGER = Logger
    //.getLogger(ZMQClient.class.getName());

    public ZMQClient(String subscriberName, String ipAddress, int port){
        this.clientName = subscriberName;
        this.ipAddress = ipAddress;
        this.port = port;
        //LOGGER.log(Level.INFO, "0MQ Client " + this.clientName + ":" + this.port + " subscribing");
        context = ZMQ.context(1);
        this.client = context.socket(ZMQ.REQ);
        this.client.connect("tcp://" + this.ipAddress + ":" + this.port);
        System.out.println( "0MQ Client " + this.clientName + ":" + this.port + " subscribed");

    }

    public void putMessage(String topic, String message){
        if(!Thread.currentThread().isInterrupted()) {
            this.client.send(topic + " " + message);
            System.out.println( "0MQ Client: " + this.clientName + " MSG sent: " + topic + " " + message);
        }
    }

    public String getMessage(){
        String msg = this.client.recvStr();
        System.out.println( "0MQ Client: " + this.clientName + " MSG received: " + msg);
        return msg;
    }

    public String parseTopic(String receivedMessage){
        return receivedMessage.substring(0, receivedMessage.indexOf(' ')-1);
    }

    public String parseMessage(String receivedMessage){
        return receivedMessage.substring(receivedMessage.indexOf(' ')+1);
    }
}

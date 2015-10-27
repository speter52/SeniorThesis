package com.Network;

import com.MessageHandler.Message;
import com.MessageHandler.MessagePasser;

import java.util.ArrayList;
import java.util.Map;

/**
 * Class to represent one node. Listens for and sends messages from/to other nodes while doing work.
 */
public abstract class GenericNode extends Thread
{
    /**
     * ID of this node.
     */
    protected int selfID;

    /**
     * Dictionary of data in node.
     */
    protected Map data;

    /**
     * The iteration number that this node is currently on.
     */
    protected int iterationNumber = 1;

    /**
     * Max number of iterations the algorithm should run
     */
    private int iterationMax = 1000;

    /**
     * List of this node's neighbors.
     */
    protected ArrayList<Integer> neighbors;

    /**
     * Holds the array of message queues for each node in the network. Nodes receive messages by reading its queue and
     * send messages by adding to the recipient node's queue.
     */
    private MessagePasser messagePasser;

    protected abstract void startNode();

    protected abstract void processResponse(Message incomingMessage);

    /**
     * Process messages received by node
     * @param messageString
     */
    private void processMessage(String messageString)
    {
        Message incomingMessage = new Message(messageString);

        String messageType = incomingMessage.getData("Type");

        switch (messageType)
        {
            case "Start":
                startNode();
                break;

            case "Response":
                processResponse(incomingMessage);
                break;
        }
    }

    /**
     * Getter for node ID
     * @return selfID
     */
    public int getSelfID()
    {
        return selfID;
    }

    /**
     * Primary Constructor.
     * @param messagePasser Array of message queues used for node communication
     */
    public GenericNode(int nodeID, MessagePasser messagePasser, ArrayList neighbors,
                       Map data)
    {
        this.selfID = nodeID;

        this.messagePasser = messagePasser;

        this.neighbors = neighbors;

        this.data = data;
    }

    /**
     * Send a message to the specified node by calling the messagePasser.
     * @param receiverID ID of the node that will the message is being sent to
     * @param message Message content string
     */
    public void sendMessage(int receiverID, Message message)
    {
        message.addData("receiverID", Integer.toString(receiverID));

        messagePasser.sendMessage(receiverID, message);
    }

    /**
     * Send message to all neighbors of this node.
     * @param message Message content string
     */
    public void sendMessageToNeighbors(Message message)
    {
        for (int neighbor : neighbors)
        {
            sendMessage(neighbor, message);
        }
    }

    /**
     * Send this node's values to all its neighbors.
     */
    public void sendValuesToNeighbors()
    {
        Message outgoingMessage = new Message();

        outgoingMessage.addData("Type", "Response");

        //TODO: Specify type of data map
        //TODO: Seperate data values from message parameters
        for(Object dataEntry: data.entrySet())
        {
            Map.Entry entry = (Map.Entry)dataEntry;

            outgoingMessage.addData(entry.getKey().toString(), entry.getValue().toString());
        }

        outgoingMessage.addData("senderID", Integer.toString(selfID));

        sendMessageToNeighbors(outgoingMessage);
    }

    /**
     * Run method that processes messages from the message queue of this node.
     */
    public void run()
    {
        while(iterationNumber < iterationMax+1)
        {
            String incomingMessage = messagePasser.waitAndRetrieveMessage(selfID);

            processMessage(incomingMessage);
        }

        System.out.println("Node " + selfID + " finished.");
    }
}

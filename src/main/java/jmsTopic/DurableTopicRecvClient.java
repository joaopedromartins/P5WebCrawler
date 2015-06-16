package jmsTopic;

import java.io.IOException;
import java.util.Scanner;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSubscriber;
import javax.jms.TopicSession;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *  A JMS client example program that synchronously receives a message a Topic
 *     
 *  @author Scott.Stark@jboss.org
 *  @version $Revision: 1.9 $
 */
public class DurableTopicRecvClient implements MessageListener
{
    TopicConnection conn = null;
    TopicSession session = null;
    Topic topic = null;
    
    private void setupPubSub()
        throws JMSException, NamingException
    {
        InitialContext iniCtx = new InitialContext();
        Object tmp = iniCtx.lookup("jms/RemoteConnectionFactory");

        TopicConnectionFactory tcf = (TopicConnectionFactory) tmp;
        conn = tcf.createTopicConnection("user", "123");
        conn.setClientID("jms-ex1dtps");
        topic = (Topic) iniCtx.lookup("jms/topic/PlayTopic");

        session = conn.createTopicSession(false,
                                          TopicSession.AUTO_ACKNOWLEDGE); 
        conn.start();
    }
    
    @Override
    public void onMessage(Message msg) {
     TextMessage tmsg = (TextMessage) msg;
     try {
      System.out.println("Got message: " + tmsg.getText());
     } catch (JMSException e) {
      e.printStackTrace();
     }
    }
    
    private void recvAsync()
        throws JMSException, NamingException
    {
        System.out.println("Begin recvAsync");
        // Setup the pub/sub connection, session
        setupPubSub();
//        // Wait upto 5 seconds for the message
        TopicSubscriber recv = session.createDurableSubscriber(topic, "jms-ex1dtps");
        recv.setMessageListener(this);
//        Message msg = recv.receive(5000);
//        if (msg == null) {
//            System.out.println("Timed out waiting for msg");
//        } else {
//            System.out.println("DurableTopicRecvClient.recv, msgt=" + msg);
//        } 
    }
    
    private void stop() 
        throws JMSException
    {
        conn.stop();
        session.close();
        conn.close();
    }
    
    public void listen() throws JMSException, NamingException, IOException{
    	Scanner sc = new Scanner(System.in);
    	System.out.println("Begin DurableTopicRecvClient, now=" + 
    			System.currentTimeMillis());
    	DurableTopicRecvClient client = new DurableTopicRecvClient();
    	client.recvAsync();
    	System.out.println("Write exit to finish...");
    	while(!sc.nextLine().equalsIgnoreCase("exit")){
    		continue;
    	}
    	client.stop();
    	System.out.println("End DurableTopicRecvClient");
    	System.exit(0);
    }
    
    public static void main(String args[]) 
        throws Exception
    {
        System.out.println("Begin DurableTopicRecvClient, now=" + 
                           System.currentTimeMillis());
        DurableTopicRecvClient client = new DurableTopicRecvClient();
        client.recvAsync();
        System.out.println("Press enter to finish...");
        System.in.read();
        client.stop();
        System.out.println("End DurableTopicRecvClient");
        System.exit(0);
    }
    
}

package jmsTopic;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
//import javax.jms.TopicSlistubscriber;
import javax.jms.TopicSession;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/** 
 *  A JMS client example program that sends a TextMessage to a Topic
 *    
 *  @author Scott.Stark@jboss.org
 *  @version $Revision: 1.9 $
 */
public class TopicSendClient
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
        topic = (Topic) iniCtx.lookup("jms/topic/PlayTopic");
        session = conn.createTopicSession(false,
                                          TopicSession.AUTO_ACKNOWLEDGE);
        conn.start();
    }
    
    private void sendAsync(String text)
        throws JMSException, NamingException
    {
        System.out.println("Begin sendAsync");
        // Setup the pub/sub connection, session
        setupPubSub();
        // Send a text msg
        TopicPublisher send = session.createPublisher(topic);
        TextMessage tm = session.createTextMessage(text);
        send.publish(tm);
//        System.out.println("sendAsync, sent text=" +  tm.getText());
        send.close();
        System.out.println("End sendAsync");
    }
    
    private void stop() 
        throws JMSException
    {
        conn.stop();
        session.close();
        conn.close();
    }
    
    public void send (String msg) throws JMSException, NamingException{
    	System.out.println("Begin TopicSendClient, now=" + 
    			System.currentTimeMillis());
//    	TopicSendClient client = new TopicSendClient();
    	sendAsync(msg);
    	stop();
    	System.out.println("End TopicSendClient");
    }
    
//    public static void main(String args[], String msg) 
//        throws Exception
//    {
//        System.out.println("Begin TopicSendClient, now=" + 
//		                   System.currentTimeMillis());
//        TopicSendClient client = new TopicSendClient();
//	    client.sendAsync(msg);
//        client.stop();
//        System.out.println("End TopicSendClient");
//        System.exit(0);
//    }
    
}

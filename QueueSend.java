package examples.jms.queue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/** This example shows how to establish a connection
 * and send messages to the JMS queue. The classes in this
 * package operate on the same JMS queue. Run the classes together to
 * witness messages being sent and received, and to browse the queue
 * for messages. The class is used to send messages to the queue.
 *
 * @author Copyright (c) 1999,2014, Oracle and/or its affiliates. All Rights Reserved.
 */
public class QueueSend
{
  // Defines the JNDI context factory.
  public final static String JNDI_FACTORY="weblogic.jndi.WLInitialContextFactory";

  // Defines the JMS context factory.
  public final static String JMS_FACTORY="weblogic.examples.jms.QueueConnectionFactory";

  // Defines the queue.
  public final static String QUEUE="weblogic.examples.jms.exampleQueue";

  private QueueConnectionFactory qconFactory;
  private QueueConnection qcon;
  private QueueSession qsession;
  private QueueSender qsender;
  private Queue queue;
  private TextMessage msg;

  /**
   * Creates all the necessary objects for sending
   * messages to a JMS queue.
   *
   * @param ctx JNDI initial context
   * @param queueName name of queue
   * @exception NamingException if operation cannot be performed
   * @exception JMSException if JMS fails to initialize due to internal error
   */
  public void init(Context ctx, String queueName)
    throws NamingException, JMSException
  {
    qconFactory = (QueueConnectionFactory) ctx.lookup(JMS_FACTORY);
    qcon = qconFactory.createQueueConnection();
    qsession = qcon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
    queue = (Queue) ctx.lookup(queueName);
    qsender = qsession.createSender(queue);
    msg = qsession.createTextMessage();
    qcon.start();
  }

  /**
   * Sends a message to a JMS queue.
   *
   * @param message  message to be sent
   * @exception JMSException if JMS fails to send message due to internal error
   */
  public void send(String message) throws JMSException {
    msg.setText(message);
    qsender.send(msg);
  }

  /**
   * Closes JMS objects.
   * @exception JMSException if JMS fails to close objects due to internal error
   */
  public void close() throws JMSException {
    qsender.close();
    qsession.close();
    qcon.close();
  }
 /** main() method.
  *
  * @param args WebLogic Server URL
  * @exception Exception if operation fails
  */
  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.out.println("Usage: java examples.jms.queue.QueueSend WebLogicURL");
      return;
    }
    InitialContext ic = getInitialContext(args[0]);
    QueueSend qs = new QueueSend();
    qs.init(ic, QUEUE);
    readAndSend(qs);
    qs.close();
  }

  private static void readAndSend(QueueSend qs)
    throws IOException, JMSException
  {
    BufferedReader msgStream = new BufferedReader(new InputStreamReader(System.in));
    String line=null;
    boolean quitNow = false;
    do {
      System.out.print("Enter message (\"quit\" to quit): \n");
      line = msgStream.readLine();
      if (line != null && line.trim().length() != 0) {
        qs.send(line);
        System.out.println("JMS Message Sent: "+line+"\n");
        quitNow = line.equalsIgnoreCase("quit");
      }
    } while (! quitNow);

  }

  private static InitialContext getInitialContext(String url)
    throws NamingException
  {
    Hashtable<String,String> env = new Hashtable<String,String>();
    env.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
    env.put(Context.PROVIDER_URL, url);
    return new InitialContext(env);
  }

}

/ * test from branch * /
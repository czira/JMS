package examples.jms.queue;

import java.util.Hashtable;
import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;

/**
 * This example shows how to establish a connection to
 * and receive messages from a JMS queue in a client-demarcated
 * transaction.  The classes in this package operate on the same
 * JMS queue. Run the classes together to witness messages being
 * sent and received, and to browse the queue for messages.
 *
 * @author Copyright (c) 1999,2014, Oracle and/or its affiliates. All Rights Reserved.
 */
public class QueueReceiveInTx
{
 // Defines the JNDI context factory.
  public final static String JNDI_FACTORY="weblogic.jndi.WLInitialContextFactory";

  // Defines the JMS connection factory for the queue.
  public final static String JMS_FACTORY="weblogic.examples.jms.QueueConnectionFactory";

  // Defines the queue.
  public final static String QUEUE="weblogic.examples.jms.exampleQueue";

  private QueueConnectionFactory qconFactory;
  private QueueConnection qcon;
  private QueueSession qsession;
  private QueueReceiver qreceiver;
  private Queue queue;
  private UserTransaction utx;

 /**
  * Receives message interface.
  */
  public void receiveMessages() throws Exception {
    Message msg = null;
    String msgText = "";

    try {
      // Set transaction timeout to 30 minutes.
      utx.setTransactionTimeout(1800);
      utx.begin();
      System.out.println("TRANSACTION BEGUN");
      do {
        msg = qreceiver.receive();
        if (msg != null) {
          if (msg instanceof TextMessage) {
            msgText = ((TextMessage)msg).getText();
          } else {
            msgText = msg.toString();
          }
          System.out.println("Message Received: "+ msgText );
          if (msgText.equalsIgnoreCase("quit")) {
            utx.commit();
            System.out.println("TRANSACTION COMMITTED");
          }
        }
      } while(msg != null && ! msgText.equalsIgnoreCase("quit"));
    } catch (JMSException jmse) {
      System.out.println("Error receiving JMS message: "+jmse);
      System.err.println("An exception occurred: "+jmse.getMessage());
      throw jmse;
    } catch (javax.transaction.NotSupportedException nse) {
      System.out.println("TRANSACTION COULD NOT BEGIN DUE TO: "+ nse);
      throw nse;
    } catch (javax.transaction.RollbackException rbe) {
      System.out.println("TRANSACTION ROLLED BACK DUE TO: "+rbe);
      throw rbe;
    } catch (javax.transaction.HeuristicRollbackException hre) {
      System.out.println("TRANSACTION ROLLED BACK DUE TO: "+hre);
      throw hre;
    } catch (javax.transaction.HeuristicMixedException hme) {
      System.out.println("TRANSACTION ROLLED BACK DUE TO: "+hme);
      throw hme;
    } catch (javax.transaction.SystemException se) {
      System.out.println("TRANSACTION EXCEPTION: "+se);
      throw se;
    }
  }

  /**
   * Creates all the necessary objects for receiving
   * messages from a JMS queue.
   *
   * @param   ctx	JNDI initial context
   * @param	queueName	name of queue
   * @exception NamingException operation cannot be performed
   * @exception JMSException if JMS fails to initialize due to internal error
   */
  public void init(Context ctx, String queueName)
    throws NamingException, JMSException
  {
    qconFactory = (QueueConnectionFactory) ctx.lookup(JMS_FACTORY);
    qcon = qconFactory.createQueueConnection();
    qsession = qcon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
    utx = (UserTransaction) ctx.lookup("javax.transaction.UserTransaction");
    queue = (Queue) ctx.lookup(queueName);
    qreceiver = qsession.createReceiver(queue);
    qcon.start();
  }

  /**
   * Closes JMS objects.
   * @exception JMSException if JMS fails to close objects due to internal error
   */
  public void close() throws JMSException {
    qreceiver.close();
    qsession.close();
    qcon.close();
  }

 /**
  * main() method.
  *
  * @param args  WebLogic Server URL
  * @exception  Exception if execution fails
  */
  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.out.println("Usage: java examples.jms.queue.QueueReceiveInTx WebLogicURL");
      return;
    }
    InitialContext ic = getInitialContext(args[0]);
    QueueReceiveInTx qr = new QueueReceiveInTx();
    qr.init(ic, QUEUE);

    System.out.println("JMS Ready To Receive Messages (To quit, send a \"quit\" message).");

    qr.receiveMessages();
    qr.close();
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





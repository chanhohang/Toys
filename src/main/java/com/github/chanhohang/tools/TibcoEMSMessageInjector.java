package com.github.chanhohang.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class TibcoEMSMessageInjector implements MessageListener {

  private String jndiContextFactory = "com.tibco.tibjms.naming.TibjmsInitialContextFactory";
  private String connectionStr = "tcp://localhost:7222";
  private String jmsConnectionFactory = "QueueConnectionFactory";
  private String userName = "test";
  private String password = "test";
  private String outputQueueName = "output.message.queue";
  private String inputQueueName = "input.message.queue";

  private String filePath = "D:\\temp\\FIX_CLEAN.log";

  private boolean enableProducer = true;
  private boolean enableConsumer = true;
  private boolean perforanceMode = true;

  private Logger logger = LoggerFactory.getLogger(getClass());

  private Connection connection;
  private Session outputSession;
  private Queue outputQueue;
  private MessageProducer producer;
  private Session inputSession;
  private Queue inputQueue;
  private MessageConsumer consumer;

  public static void main(String[] args) throws IOException {
    TibcoEMSMessageInjector injector = new TibcoEMSMessageInjector();
    try {
      injector.start();
      injector.readFile();
      try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
        br.readLine();
      } ;
    } finally {
      injector.stop();
    }
  }

  private void readFile() {
    logger.info("Send Started.");

    try (BufferedReader br = new BufferedReader(new FileReader(new File(filePath)))) {
      String line = null;
      while ((line = br.readLine()) != null) {
        TextMessage sendmsg = outputSession.createTextMessage();
        sendmsg.setText(line);
        if (!perforanceMode) {
          logger.info("Sending Message {}", line);
        }
        producer.send(sendmsg);
      }

    } catch (Exception exception) {
      logger.error("File Operation Exception.", exception);
    } finally {
      logger.info("Send Finished.");
    }

  }

  public Context initJndiContext() throws NamingException {
    Hashtable<String, Object> env = new Hashtable<String, Object>();
    env.put(Context.INITIAL_CONTEXT_FACTORY, jndiContextFactory);
    env.put(Context.PROVIDER_URL, connectionStr);

    String userID = userName;
    String password = this.password;
    if (userID != null && userID.length() > 0) {
      env.put(Context.SECURITY_PRINCIPAL, userID);
      env.put(Context.SECURITY_CREDENTIALS, password);
    }
    Context context = new InitialContext(env);
    return context;

  }

  public ConnectionFactory createJMSConnectionFactory() throws NamingException {
    Context jndiContext = initJndiContext();
    Object obj = jndiContext.lookup(jmsConnectionFactory);
    return (ConnectionFactory) obj;
  }

  public Connection createConnection() throws JMSException, NamingException {
    ConnectionFactory connfactory = createJMSConnectionFactory();
    Connection connection = connfactory.createConnection();
    logger.info("Connection created");
    return connection;
  }

  public void start() {
    try {
      connection = createConnection();

      if (enableProducer) {
        outputSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        outputQueue = outputSession.createQueue(outputQueueName);
        producer = outputSession.createProducer(outputQueue);
        logger.info("Producer created.");
      }

      if (enableConsumer) {
        inputSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        inputQueue = inputSession.createQueue(inputQueueName);

        consumer = inputSession.createConsumer(inputQueue);
        consumer.setMessageListener(this);
        logger.info("Consumer created.");
      }

      connection.start();
      logger.info("Connection started.");
    } catch (Exception exception) {
      logger.error("Start failure.", exception);
    }
  }

  public void stop() {
    try {
      if (enableConsumer) {
        inputSession.close();
      }

      if (enableProducer) {
        producer.close();
        outputSession.close();
      }
      connection.stop();
      logger.info("Connection stopped.");
    } catch (JMSException exception) {
      logger.error("Stop failure.", exception);
    }
  }

  public void sendMessage(String message) {

  }

  @Override
  public void onMessage(Message message) {
    try {
      if (message instanceof TextMessage) {
        onMsg((TextMessage) message);
      } else if (message instanceof BytesMessage) {
        onMsg((BytesMessage) message);
      } else {
        logger.warn("Message(ID=" + message.getJMSMessageID() + ") ignored because message type is "
            + message.getClass() + " but only TextMessage and BytesMessage are supported : "
            + message.toString());
        return;
      }
    } catch (Exception exception) {
      logger.error("Unexpected exception when processing message.", exception);
    }
  }

  private void onMsg(BytesMessage message) {
    logger.info("Receicivng ByteMessage: {}", message);
  }

  private void onMsg(TextMessage message) throws JMSException {
    logger.info("Receicivng TextMessage: {}", message.getText());
  }
}

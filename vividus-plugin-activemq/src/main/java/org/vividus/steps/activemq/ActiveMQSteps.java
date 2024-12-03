package org.vividus.steps.activemq;

import jakarta.jms.Connection;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.jbehave.core.annotations.When;

public class ActiveMQSteps
{
    private String username = "admin";
    private String password = "admin";
    private String brokerUrl = "tcp://0.0.0.0:61616";

    @When("I send message to activeMQ $type with name `$name` and payload:$payload")
    public void sendMessageToActiveMQ(MessageType type, String name, String payload) throws JMSException
    {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        try(Connection connection = connectionFactory.createConnection(username, password))
        {
            connection.start();
            try(Session session = connection.createSession())
            {
                Destination destination = type == MessageType.QUEUE ? session.createQueue(name)
                        : session.createTopic(name);
                MessageProducer producer = session.createProducer(destination);
                producer.send(session.createTextMessage(payload));
            }
        }
    }
}

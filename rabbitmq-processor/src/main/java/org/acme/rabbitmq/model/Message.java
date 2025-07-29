package org.acme.rabbitmq.model;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.runtime.annotations.RegisterForReflection;

@Entity
@Table(name = "messages")
@RegisterForReflection
public class Message extends PanacheEntityBase {

    @Id
    public String id;

    public String sender;
    public String status;
    public String recipient;
    public String messageContent;
    public String timeSent;
    public String timeDelivered;
    public int deliveryAttemtps;

    /**
    * Default constructor required for Jackson serializer and Hibernate to use reflection to instantiate objects
    */
    public Message() { }

    public Message(String id, String sender, String status, String recipient, String messageContent, String timeSent, String timeDelivered, int deliveryAttemtps) {
        this.id = id;
        this.sender = sender;
        this.status = status;
        this.recipient = recipient;
        this.messageContent = messageContent;
        this.timeSent = timeSent;
        this.timeDelivered = timeDelivered;
        this.deliveryAttemtps = deliveryAttemtps;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", sender=" + sender +
                ", status=" + status +
                ", recipient='" + recipient + '\'' +
                ", messageContent='" + messageContent + '\'' +
                ", timeSent=" + timeSent +
                ", timeDelivered=" + timeDelivered +
                ", deliveryAttemtps=" + deliveryAttemtps +
                '}';
    }
}
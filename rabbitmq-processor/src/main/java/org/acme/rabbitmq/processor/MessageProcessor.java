package org.acme.rabbitmq.processor;
import java.time.Instant;
import java.util.Random;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.rabbitmq.model.Message;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import io.smallrye.reactive.messaging.annotations.Blocking;
import io.vertx.core.json.JsonObject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import io.vertx.core.json.Json;

/**
 * A bean consuming data from the "message-requests" RabbitMQ queue and giving out a random status(DELIVERED/FAILED).
 * The result is pushed to the "messages" RabbitMQ exchange.
 */
@ApplicationScoped
public class MessageProcessor {

    private Random random = new Random();

    @Incoming("requests")       
    @Outgoing("messages")         
    @Blocking     
    @Transactional              
    public Message process(JsonObject json) throws InterruptedException {
        try {
            // simulate some hard-working task
            Thread.sleep(1000);

            Message message = json.mapTo(Message.class);
            message.deliveryAttemtps+=1;
            message.status = random.nextBoolean() ? "DELIVERED" : "FAILED";
            Instant timeNow = Instant.now();
            String timeNowStr=timeNow.toString();
            if ("FAILED".equalsIgnoreCase(message.status)){
                message.timeDelivered = null;
            }else{
                message.timeDelivered = timeNowStr;
            }

            // Persist message to DB
            try {
                message.persist();
            } catch (Exception dbEx) {
                System.err.println("DB persistence failed for message " + message.id + ": " + dbEx.getMessage());
                dbEx.printStackTrace();
            }
            
            // Notify producer via internal callback
            try {
                String jsonmsg = Json.encode(message); // Converts Message object to proper JSON
                System.out.println("Serialized JSON: " + jsonmsg); // Debug logs
            
                Client client = ClientBuilder.newClient();
                Response response = client
                    .target("http://producer:8080/messages/callback")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(jsonmsg)); 
            
                System.out.println("Callback response: " + response.getStatus());                
            
                response.close();
                client.close();
            } catch (Exception callbackEx) {
                System.err.println("Callback notification failed for message " + message.id + ": " + callbackEx.getMessage());
                callbackEx.printStackTrace();
            }

            return message;
        }  catch (Exception ex) {
            System.err.println("Unhandled error during message processing: " + ex.getMessage());
            ex.printStackTrace();
            throw ex; // triggers NACK -NEGATIVE acknowledgment
        }
    }
}
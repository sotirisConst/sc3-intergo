package org.acme.rabbitmq.producer;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.acme.rabbitmq.model.Message;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import io.smallrye.mutiny.Multi;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.annotation.security.RolesAllowed;


@Path("/messages")
public class MessagesResource {
    private static final int MAX_DELIVERY_ATTEMPTS = 3;
    
    @Channel("message-requests") Emitter<Message> messageRequestEmitter; 
    /**
     * Endpoint to generate a new message request object and send it to "message-requests" channel (which
     * maps to the "message-requests" RabbitMQ exchange) using the emitter.
     */
    @POST
    @Path("/request") 
    @RolesAllowed({"admin", "user"})   
    @Produces(MediaType.APPLICATION_JSON)
    public String createRequest(@QueryParam("sender") String sender,
                            @QueryParam("recipient") String recipient,
                            @QueryParam("messageContent") String messageContent) {
        try{
            // Validate sender and recipient
            if (sender == null || !sender.matches("^\\+?\\d{8,15}$")) {
                return "ERROR: Invalid sender format. Use + and digits only. 8–15 digits.";
            }
            if (recipient == null || !recipient.matches("^\\+?\\d{8,15}$")) {
                return "ERROR: Invalid recipient format. Use + and digits only. 8–15 digits.";
            }
            // Validate message content
            if (messageContent == null || messageContent.trim().isEmpty()) {
                return "ERROR: Message content is required.";
            }
            if (messageContent.length() > 200) {
                return "ERROR: Message content exceeds 200 characters.";
            }

            String id = UUID.randomUUID().toString();
            Instant timeNow = Instant.now();
            String timeNowStr=timeNow.toString();
            Message msg = new Message(id, sender, "Pending", recipient, messageContent, timeNowStr, null, 0);
            messageRequestEmitter.send(msg);
            return id;
        }  catch (Exception e) {
            System.err.println("Failed to create message request: " + e.getMessage());
            e.printStackTrace();
            return "ERROR: Failed to enqueue message";
        }
    }

    @Channel("messages") Multi<Message> messages;    
    /**
     * Endpoint retrieving the "messages" queue and sending the items to a server sent event(in real-time as they arrive). 
     * Consumes messages from a channel (incoming stream)
     */
    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS) 
    public Multi<Message> stream() {
        return messages; 
    }

    /**
     * Retrieves all messages stored in the database. 
     * API restricted to users with the "admin" role
     * Example request:
     *   GET http://localhost:8080/messages/all
     */
    @GET
    @Path("/all")
    @RolesAllowed("admin")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Message> getAllMessages() {
        return Message.listAll(); // Uses Panache to fetch all Message entities from the DB
    }

    /**
     * Retrieves all messages sent by a specific sender.
     * API restricted to users with the "admin" role.     
     * Example request:
     *   GET http://localhost:8080/messages/by-sender?sender=%2B35799111111
     */
    @GET
    @Path("/by-sender")
    @RolesAllowed("admin")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMessagesBySender(@QueryParam("sender") String sender) {
        try {
            if (sender == null || sender.isBlank() || !sender.matches("^\\+?\\d{8,15}$")) {
                String error = "Invalid sender. Must be a phone number with optional +, 8–15 digits.";
                System.err.println("Error: " + error + " Input: " + sender);
                return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
            }
    
            List<Message> messages = Message.list("sender", sender);
            return Response.ok(messages).build();
    
        } catch (Exception e) {
            System.err.println("Failed to fetch messages by sender: " + e.getMessage());
            e.printStackTrace();
            return Response.serverError().entity("Internal error fetching messages.").build();
        }
    }

    @Channel("messages") Emitter<Message> messageEmitter;
    /**
     * Emitter used to push processed messages to the "messages" channel, which is consumed by the frontend via SSE
     * Callback endpoint that receives a message processing result from the processor service.
     *
     * If the message status is "FAILED" and the number of delivery attempts is less than 3,
     * a retry is triggered:
     *   - A new message is created with a new UUID and current timestamp.
     *   - The message is sent back to the "message-requests" channel for reprocessing.
     *   - It is also emitted to the "messages" channel to update the frontend via SSE.
     *
     * This mechanism provides a retry logic and ensures the UI remains in sync with backend updates.
     */
    @POST
    @Path("/callback")
    public Response handleCallback(Message message) {
        System.out.println("Received callback: " + message);
        if ("FAILED".equalsIgnoreCase(message.status) && (message.deliveryAttemtps < MAX_DELIVERY_ATTEMPTS)) {
            try {
                String id = UUID.randomUUID().toString();
                Instant timeNow = Instant.now();
                String timeNowStr=timeNow.toString();
                Message msg = new Message(id, message.sender, message.status, message.recipient, message.messageContent, timeNowStr, null, message.deliveryAttemtps);
                messageRequestEmitter.send(msg); // feed RabbitMQ
                messageEmitter.send(msg);       // notify frontend
                System.out.println("Retry message sent: " + message.id);
            } catch (Exception e) {
                System.err.println("Error during callback processing: " + e.getMessage());
                e.printStackTrace();
                return Response.serverError().entity("Callback processing failed").build();
            }
        }

        return Response.ok().build();
    }
}
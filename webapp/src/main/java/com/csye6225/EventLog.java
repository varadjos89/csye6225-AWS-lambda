package com.csye6225;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;

import java.util.*;


public class EventLog implements RequestHandler<SNSEvent, Object> {
    static DynamoDB dynamoDb;
    private String tableName = "csye6225_DynamoDB";
    private Regions region = Regions.US_EAST_1;
    public String from = "";
    static final String subject = "Recipe Links";
    static String htmlBody;
    private static String textBody="abc";
    private String body = "";
    static String token;
    static String username;
    List<String> myList;
    private long now;
    private long ttl;
    private long totalttl;

    public Object handleRequest(SNSEvent request, Context context) {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());
        String Domain = System.getenv("domain_name");
        context.getLogger().log("Domain name is :" + Domain);
        from = "noreply@" + Domain;

        //Creating ttl
        context.getLogger().log("Invocation started : " + timeStamp);
        now = Calendar.getInstance().getTimeInMillis() / 1000; // unix time
        ttl = 60 * Integer.parseInt(System.getenv("ttl")); // ttl set to 30 min
        totalttl = ttl + now;

        try {
            //Function Excecution for sending the email
            myList = new ArrayList<String>(Arrays.asList(request.getRecords().get(0).getSNS().getMessage().split(",")));
            username = myList.get(myList.size() - 1);
            myList.remove(myList.size() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        token = UUID.randomUUID().toString();
        context.getLogger().log("Invocation completed : " + timeStamp);

        try {
            initDynamoDbClient();
            long ttlDbValue = 0;
            context.getLogger().log("Current timestamp " + timeStamp);
            if (this.dynamoDb.getTable(tableName).getItem("id",username)!=null) {
                Item item = this.dynamoDb.getTable(tableName).getItem("id", username);
                ttlDbValue = item.getLong("ttl");

                context.getLogger().log("ttldbvalue : " + ttlDbValue);
                context.getLogger().log("current ttl value : " + now);
                if (ttlDbValue <= now && ttlDbValue != 0) {
                     emailSender(context);
                } else {
                    context.getLogger().log("ttl is not expired. New request is not processed for the user: " + username);
                }
            }
            else{
                context.getLogger().log("inside else");
                emailSender(context);
            }
        } catch (Exception ex) {
            context.getLogger().log("Email was not sent. Error message: " + ex.getMessage());
            emailSender(context);
        }
        return null;
    }

    private void emailSender(Context context) {
        context.getLogger().log("Checking for valid ttl");
        context.getLogger().log("ttl expired, creating new token and sending email");
        this.dynamoDb.getTable(tableName)
                .putItem(
                        new PutItemSpec().withItem(new Item()
                                .withString("id", username)
                                .withString("token", token)
                                .withLong("ttl", totalttl)));

        for (String recipe : myList) {
            body = body + "<p>" + "https://prod.varadjoshi89.xyz/v1/recipe/"+recipe + "</p>";
            context.getLogger().log(recipe);
        }
        htmlBody = "<h2>Email sent from Amazon SES</h2>"
                + body;
        context.getLogger().log("This is HTML body: " + htmlBody);

        //Sending email using Amazon SES client
        AmazonSimpleEmailService clients = AmazonSimpleEmailServiceClientBuilder.standard()
                .withRegion(region).build();
        SendEmailRequest emailRequest = new SendEmailRequest()
                .withDestination(
                        new Destination().withToAddresses(username))
                .withMessage(new Message()
                        .withBody(new Body()
                                .withHtml(new Content()
                                        .withCharset("UTF-8").withData(htmlBody))
                                .withText(new Content()
                                        .withCharset("UTF-8").withData(textBody)))
                        .withSubject(new Content()
                                .withCharset("UTF-8").withData(subject)))
                .withSource(from);
        clients.sendEmail(emailRequest);
        context.getLogger().log("Email sent successfully to email id: " + username);
        body="";

    }

    //creating a DynamoDB Client
    private void initDynamoDbClient() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(region)

                .build();
        dynamoDb = new DynamoDB(client);
    }
}
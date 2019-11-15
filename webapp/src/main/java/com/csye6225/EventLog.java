package com.csye6225;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
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
    private String tableName = "csye6225_DynamoDBDevTwothree";
    private Regions region = Regions.US_EAST_1;
    public String from = "";
    static final String subject = "Recipe Links";
    static String htmlBody;
    private static String textBody;
    private String body="";
    static String token;
    static String username;
    List<String> myList;

    public Object handleRequest(SNSEvent request, Context context) {
        
    }
}
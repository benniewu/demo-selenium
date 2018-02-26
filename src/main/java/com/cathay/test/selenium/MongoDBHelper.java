package com.cathay.test.selenium;

import com.mongodb.MongoClient;

public class MongoDBHelper {

    private static String mongodb_ip = "10.87.50.118";

    private static int mongodb_port = 27017;

    private static MongoClient mongoClient = new MongoClient(mongodb_ip, mongodb_port);

    public static MongoClient getMongoClient() {
        return mongoClient;
    }
}
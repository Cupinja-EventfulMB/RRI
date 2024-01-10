package com.mygdx.game.utils;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;

public class MongoDBManager {
    private MongoClient mongoClient;
    private MongoDatabase database;

    public MongoDBManager(String connectionString, String databaseName) {
        MongoClientURI uri = new MongoClientURI(connectionString);
        this.mongoClient = new MongoClient(uri);
        this.database = mongoClient.getDatabase(databaseName);
    }

    public boolean testConnection() {
        try {
            // Try to perform a simple query to check if data can be retrieved
            MongoCollection<Document> collection = database.getCollection("institutions");
            Document document = collection.find().first();

            // Check if the document is not null
            return document != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public MongoDatabase getDatabase() {
        return database;
    }

    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}
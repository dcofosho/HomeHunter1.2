package com.hhalpha.daniel.homehunter12;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Created by Daniel on 7/15/2016.
 */
@DynamoDBTable(tableName = "Showings")
public class Showing {

    private String address;
    private String infoString;


    @DynamoDBHashKey(attributeName = "Address")
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @DynamoDBRangeKey(attributeName = "InfoString")
    public String getInfoString() {
        return infoString;
    }

    public void setInfoString(String infoString) {
        this.infoString = infoString;
    }


}
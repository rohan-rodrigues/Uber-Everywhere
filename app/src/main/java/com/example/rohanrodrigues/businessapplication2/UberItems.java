package com.example.rohanrodrigues.businessapplication2;

import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.uber.sdk.android.core.UberSdk;
import com.uber.sdk.android.rides.RideParameters;
import com.uber.sdk.android.rides.RideRequestButton;
import com.uber.sdk.android.rides.RideRequestButtonCallback;
import com.uber.sdk.core.auth.Scope;
import com.uber.sdk.rides.client.ServerTokenSession;
import com.uber.sdk.rides.client.SessionConfiguration;
import com.uber.sdk.rides.client.error.ApiError;

import org.w3c.dom.Document;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

/**
 * Created by rohanrodrigues on 1/26/17.
 */

public class UberItems extends Fragment {
    View rootview;

    private String currentLoc, desiredLoc;
    private String[] currentCoords, desiredCoords;
    private boolean savePressed = false;
    private SessionConfiguration config;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootview =  inflater.inflate(R.layout.items, container, false);

        config = new SessionConfiguration.Builder()
                // mandatory
                .setClientId("p3wTmFH2uKt62nUaEdZxd09jMyBvu-dQ")
                // required for enhanced button features
                .setServerToken("rBNuj__uCbdP1BOzohZuVVrQZnJJA_0o2Stisnl-")
                // required for implicit grant authentication
                //  .setRedirectUri("TEST://oauth/callback")
                // required scope for Ride Request Widget features
                .setScopes(Arrays.asList(Scope.RIDE_WIDGETS))
                // optional: set Sandbox as operating environment
                .setEnvironment(SessionConfiguration.Environment.SANDBOX)
                .build();
        UberSdk.initialize(config);
        final RideRequestButton requestButton = new RideRequestButton(getContext());
        // get your layout, for instance:
        RelativeLayout layout = (RelativeLayout) rootview.findViewById(R.id.layout_uber);
        layout.addView(requestButton);


        Button save = (Button) rootview.findViewById(R.id.save_hair);
        save.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                savePressed = true;
                EditText currentLocHair = (EditText) rootview.findViewById(R.id.current_locationHair);
                EditText desiredLocHair = (EditText) rootview.findViewById(R.id.desired_locationHair);
                currentLoc = String.valueOf(currentLocHair.getText());
                desiredLoc = String.valueOf(desiredLocHair.getText());

                try {
                    currentCoords = findCoordinates(currentLoc);
                    desiredCoords = findCoordinates(desiredLoc);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                setRideParameters(requestButton);
            }
        });


        return rootview;
    }

    public String[] findCoordinates(String address) throws Exception {
        int responseCode = 0;
        String api = "http://maps.googleapis.com/maps/api/geocode/xml?address=" + URLEncoder.encode(address, "UTF-8") + "&sensor=true";
        URL url = new URL(api);
        HttpURLConnection httpConnection = (HttpURLConnection)url.openConnection();
        httpConnection.connect();
        responseCode = httpConnection.getResponseCode();
        if(responseCode == 200)
        {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();;
            Document document = builder.parse(httpConnection.getInputStream());
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression expr = xpath.compile("/GeocodeResponse/status");
            String status = (String)expr.evaluate(document, XPathConstants.STRING);
            if(status.equals("OK"))
            {
                expr = xpath.compile("//geometry/location/lat");
                String latitude = (String)expr.evaluate(document, XPathConstants.STRING);
                expr = xpath.compile("//geometry/location/lng");
                String longitude = (String)expr.evaluate(document, XPathConstants.STRING);
                return new String[] {latitude, longitude};
            }
            else
            {
                throw new Exception("Error from the API - response status: "+status);
            }
        }
        return null;
    }


    public void setRideParameters(RideRequestButton requestButton) {
        final RideParameters rideParams = new RideParameters.Builder()
                // Optional product_id from /v1/products endpoint (e.g. UberX). If not provided, most cost-efficient product will be used
                .setProductId("a1111c8c-c720-46c3-8534-2fcdd730040d")
                // Required for price estimates; lat (Double), lng (Double), nickname (String), formatted address (String) of dropoff location
                .setDropoffLocation(
                        (double) 37.775304f, -122.417522, "Uber HQ", "1455 Market Street, San Francisco")
                // Required for pickup estimates; lat (Double), lng (Double), nickname (String), formatted address (String) of pickup location
                .setPickupLocation((double) 37.775304f, -122.417522, "Uber HQ", "1455 Market Street, San Francisco")
                .build();
        // set parameters for the RideRequestButton instance
        requestButton.setRideParameters(rideParams);

        ServerTokenSession session = new ServerTokenSession(config);
        requestButton.setSession(session);
        requestButton.loadRideInformation();

        RideRequestButtonCallback callback = new RideRequestButtonCallback() {

            @Override
            public void onRideInformationLoaded() {
                // react to the displayed estimates
            }

            @Override
            public void onError(ApiError apiError) {
                // API error details: /docs/riders/references/api#section-errors
            }

            @Override
            public void onError(Throwable throwable) {
                // Unexpected error, very likely an IOException
            }
        };
        requestButton.setCallback(callback);
    }
}

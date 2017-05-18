package com.hashtag.context.index;

import twitter4j.HashtagEntity;
import twitter4j.Status;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class MarkableExtractor {

    private static final String endpoint = "http://192.168.171.132:3000/?tweet=";
    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String NOUN = "NN";
    private static final String VERB = "VB";
    private static final String ENTITY_EXTERNAL = "B-ENTITY";
    private static final String ENTITY_INTERNAL = "I-ENTITY";

    public static Markable extractMarkables(Status status) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, KeyManagementException, URISyntaxException {
        Markable markable = new Markable();

        String tagged = MarkableExtractor.markEntities(status.getText());

        StringBuilder entityBuilder = new StringBuilder();
        boolean entityConstructionMode = false;

        for (String tag : tagged.split(" ")) {

            if (tag.startsWith("#")) {
                continue;
            }

            String pos = tag.substring(tag.lastIndexOf("/") + 1);
            tag = tag.substring(0, tag.lastIndexOf("/"));
            String entity = tag.substring(tag.lastIndexOf("/") + 1);
            tag = tag.substring(0, tag.lastIndexOf("/")).toLowerCase();

            if (entityConstructionMode) {
                if (entity.contains(ENTITY_INTERNAL)) {
                    entityBuilder.append(" " + tag);
                    continue;
                } else {
                    String ne = entityBuilder.toString();

                    markable.appendEntity(ne.trim().toLowerCase());
                    entityBuilder = new StringBuilder();
                    entityConstructionMode = false;
                }
            }

            if (entity.contains(ENTITY_EXTERNAL)) {
                entityBuilder.append(" " + tag);
                entityConstructionMode = true;
                continue;
            }

            if (pos.contains(NOUN)) {
                markable.appendNoun(tag);
            } else if (pos.contains(VERB)) {
                markable.appendVerb(tag);
            }
        }

        return markable;
    }

    private static String markEntities(String tweet) throws NullPointerException, IOException {
        tweet = tweet.replaceAll("\n", " ").replaceAll("\\s+", " ").trim();
        if (tweet.isEmpty()) {
            return null;
        }

        URL url = new URL(endpoint + URLEncoder.encode(tweet, "UTF-8"));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", USER_AGENT);

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuffer response = new StringBuffer();
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        in.close();

        return response.toString();
    }

}


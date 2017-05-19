package com.hashtag.context.stream;

import com.hashtag.context.index.HashtagIndex;
import com.hashtag.context.utils.Twokenize;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class Twitter {

    public static HashMap<String, Integer> HASHTAGS = new HashMap();

    public static Stream<Status> stream() throws IOException, TwitterException {
        List<String> tweets = FileUtils.readLines(new File("sample.json"));
        List<Status> statuses = new ArrayList<>();
        for (String tweet : tweets) {
            Status status = TwitterObjectFactory.createStatus(tweet);
            statuses.add(status);
        }

        return statuses.stream();
    }

    private static String CONSUMER_KEY = "nUo03wsfWcMpF2GAfNNwnvszW";
    private static String CONSUMER_SECRET = "uAd08Ez8UWxko27B5oYh7DDt84g9QLnU68aQqH4bdMsvuvGziS";
    private static String ACCESS_TOKEN = "2976106171-q4EA8InfDPSAuQ9NK4mR4x3xfIVzP2QzfTPlL5l";
    private static String ACCESS_SECRET = "CQCu7gfdPKsbLKiN1lxFC1Yy1X1LYoCslFRaA1k8FjJbH";

    private static HashtagIndex index;

    public static void main(String args[]) throws IOException {

        index = new HashtagIndex();

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder
                .setOAuthConsumerKey(CONSUMER_KEY)
                .setOAuthConsumerSecret(CONSUMER_SECRET)
                .setOAuthAccessToken(ACCESS_TOKEN)
                .setOAuthAccessTokenSecret(ACCESS_SECRET);

        TwitterStream twitterStream = new TwitterStreamFactory(configurationBuilder.build()).getInstance();
        StatusListener listener = (new StatusListener() {
            @SuppressWarnings("Duplicates")
            @Override
            public void onStatus(Status status) {

                try {
                    for (String token : Twokenize.tokenizeRawTweetText(status.getText().replaceAll("\n", "").toLowerCase())) {
                        if (token.startsWith("@") || token.startsWith("#") || token.startsWith("http")) {
                            continue;
                        }
                        token = token.replaceAll("\n", "");
                        token = token.toLowerCase();
                        token = token.trim();

                        index.appendDocument(token);
                    }

                    for (HashtagEntity entity : status.getHashtagEntities()) {
                        System.out.println(((Runtime.getRuntime().freeMemory() + Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory()) / 1024) + "\t#" + entity.getText() + ": " + index.contextFor(entity.getText().toLowerCase()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

            }

            @Override
            public void onTrackLimitationNotice(int i) {

            }

            @Override
            public void onScrubGeo(long l1, long l2) {

            }

            @Override
            public void onStallWarning(StallWarning stallWarning) {

            }

            @Override
            public void onException(Exception e) {

            }
        });

        twitterStream.addListener(listener);
        twitterStream.sample("en");

    }


}

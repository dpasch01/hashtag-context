package com.hashtag.context.stream;

import com.hashtag.context.utils.Utils;
import org.apache.commons.io.FileUtils;
import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

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

    public static void main(String[] args) throws IOException, TwitterException {
        Twitter.stream().forEach(status -> {
            for (HashtagEntity he : status.getHashtagEntities()) {
                String normalized = he.getText().toLowerCase();
                if (HASHTAGS.containsKey(normalized)) {
                    int temp = HASHTAGS.get(normalized);
                    temp += 1;
                    HASHTAGS.put(normalized, temp);
                } else {
                    HASHTAGS.put(normalized, 1);
                }
            }
        });

        for (String hashtag : Utils.sortByValue(HASHTAGS).keySet()) {
            System.out.println(hashtag + ": " + HASHTAGS.get(hashtag));
        }
    }

}

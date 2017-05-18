package com.hashtag.context.main;

import com.hashtag.context.index.HashtagIndex;
import com.hashtag.context.stream.Twitter;
import com.hashtag.context.utils.Twokenize;
import org.apache.lucene.queryparser.classic.ParseException;
import twitter4j.HashtagEntity;
import twitter4j.TwitterException;

import java.io.IOException;

public class Main {

    public static void main(String args[]) throws IOException, InterruptedException, ParseException, TwitterException {
        HashtagIndex index = new HashtagIndex();
        Twitter.stream().forEach(status -> {
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
        });
    }

}

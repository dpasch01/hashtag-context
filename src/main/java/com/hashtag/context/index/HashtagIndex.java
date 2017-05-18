package com.hashtag.context.index;

import com.hashtag.context.stream.Twitter;
import com.hashtag.context.utils.Twokenize;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.TrackingIndexWriter;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import twitter4j.HashtagEntity;
import twitter4j.TwitterException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class HashtagIndex {
    private FSDirectory index;
    private IndexWriter indexWriter;
    private IndexWriterConfig indexConfig;
    private Analyzer analyzer;

    private TrackingIndexWriter trackingIndexWriter;
    private ReferenceManager<IndexSearcher> searcherManager;
    private ControlledRealTimeReopenThread<IndexSearcher> nrtReopenThread;

    public HashtagIndex() throws IOException {
        File t = new File("hashtags.lcn");
        if (t.exists()) {
            t.delete();
        }

        this.analyzer = new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String s) {
                return new TokenStreamComponents(new NGramTokenizer(1, 4));
            }
        };
        this.indexConfig = new IndexWriterConfig(this.analyzer);
        this.index = FSDirectory.open(Paths.get(new File("hashtags.lcn").toURI()));
        this.indexWriter = new IndexWriter(this.index, this.indexConfig);
        this.trackingIndexWriter = new TrackingIndexWriter(indexWriter);
        this.searcherManager = new SearcherManager(indexWriter, true, null);
        this.nrtReopenThread = new ControlledRealTimeReopenThread<>(trackingIndexWriter, searcherManager, 1.0, 0.1);

        nrtReopenThread.setName("NRT Reopen Thread");
        nrtReopenThread.setPriority(Math.min(Thread.currentThread().getPriority() + 2, Thread.MAX_PRIORITY));
        nrtReopenThread.setDaemon(true);
        nrtReopenThread.start();

    }

    public void appendDocument(String term) throws IOException {
        Document document = new Document();
        document.add(new TextField("term", term, TextField.Store.YES));
        this.indexWriter.addDocument(document);
    }

    public FSDirectory getIndex() {
        return index;
    }

    public Set<String> contextFor(String hashtag) throws IOException, ParseException {
        IndexSearcher searcher = searcherManager.acquire();
        QueryParser qp = new QueryParser("term", this.analyzer);
        Query query = qp.parse(qp.escape(hashtag));

        TopDocs docs = searcher.search(query, 5);

        Set<String> terms = new HashSet<>();
        for (ScoreDoc doc : docs.scoreDocs) {
            terms.add(searcher.doc(doc.doc).get("term").toLowerCase());
        }
        searcherManager.release(searcher);

        if (terms.size() == 1) {
            String[] extras = hashtag.toLowerCase().split(terms.toArray()[0].toString());
            terms.addAll(Arrays.asList(extras));
        }
        return terms;
    }

}

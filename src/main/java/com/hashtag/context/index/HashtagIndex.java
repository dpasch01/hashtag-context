package com.hashtag.context.index;

import com.hashtag.context.stream.Twitter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.TrackingIndexWriter;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import twitter4j.TwitterException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashSet;
import java.util.Set;

public class HashtagIndex {
    private FSDirectory index;
    private IndexWriter indexWriter;
    private IndexWriterConfig indexConfig;
    private StandardAnalyzer analyzer;

    private TrackingIndexWriter trackingIndexWriter;
    private ReferenceManager<IndexSearcher> searcherManager;
    private ControlledRealTimeReopenThread<IndexSearcher> nrtReopenThread;

    public HashtagIndex() throws IOException {
        File t = new File("hashtags.lcn");
        if (t.exists()) {
            t.delete();
        }

        this.analyzer = new StandardAnalyzer();
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

    public void appendDocument(String keywords) throws IOException {
        Document document = new Document();
        document.add(new TextField("term", keywords, TextField.Store.YES));
        this.indexWriter.addDocument(document);
    }

    public FSDirectory getIndex() {
        return index;
    }

    public Set<String> contextFor(String hashtag) throws IOException, ParseException {
        IndexSearcher searcher = searcherManager.acquire();
        QueryParser qp = new QueryParser("term", this.analyzer);
        Query query = qp.parse(qp.escape(hashtag));

        TopDocs docs = searcher.search(query, 10);

        Set<String> terms = new HashSet<>();
        for (ScoreDoc doc : docs.scoreDocs) {
            terms.add(searcher.doc(doc.doc).get("term"));
        }
        searcherManager.release(searcher);

        return terms;
    }

    public static void main(String args[]) throws IOException, InterruptedException, ParseException {
        HashtagIndex index = new HashtagIndex();
        Thread writer = new Thread() {
            @Override
            public void run() {
                try {
                    Twitter.stream().forEach(status -> {
                        try {
                            Markable markable = MarkableExtractor.extractMarkables(status);
                            Set<String> tokens = new HashSet<>(markable.getNouns());
                            tokens.addAll(markable.getVerbs());
                            tokens.addAll(markable.getEntities());

                            for (String token : tokens) {
                                index.appendDocument(token.toLowerCase().trim());
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (CertificateException e) {
                            e.printStackTrace();
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        } catch (KeyStoreException e) {
                            e.printStackTrace();
                        } catch (UnrecoverableKeyException e) {
                            e.printStackTrace();
                        } catch (KeyManagementException e) {
                            e.printStackTrace();
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
            }
        };

        writer.start();
        Thread.sleep(60000);

        System.out.println(index.contextFor("brexit"));

    }

}

//package com.lucenetutorial.apps;

import org.apache.lucene.analysis.payloads.IntegerEncoder;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.blockterms.TermsIndexReaderBase;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.*;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This terminal application creates an Apache Lucene index in a folder and adds files into this index
 * based on the input of the user.
 */
public class ExtractKeywords {
    private static StandardAnalyzer analyzer = new StandardAnalyzer(/*Version.LUCENE_4_0_0*/);

    private IndexWriter writer;
    private ArrayList<File> queue = new ArrayList<File>();


    public static void main(String[] args) throws IOException {
//        System.out.println("Enter the path where the index will be created: (e.g. /tmp/index or c:\\temp\\index)");

        String indexLocation = null;
        BufferedReader br = new BufferedReader(
                new InputStreamReader(System.in));
//        String s = br.readLine();
        String s = "c:\\tmp\\ek1\\indx";

        ExtractKeywords indexer = null;
        try {
            indexLocation = s;
            indexer = new ExtractKeywords(s);
        } catch (Exception ex) {
            System.out.println("Cannot create index..." + ex.getMessage());
            System.exit(-1);
        }

        //===================================================
        //read input from user until he enters q for quit
        //===================================================
//        while (!s.equalsIgnoreCase("q")) {
            try {
//                System.out.println("Enter the full path to add into the index (q=quit): (e.g. /home/ron/mydir or c:\\Users\\ron\\mydir)");
//                System.out.println("[Acceptable file types: .xml, .html, .html, .txt]");
//                s = br.readLine();
                s = "c:\\tmp\\ek1\\files";
//                if (s.equalsIgnoreCase("q")) {
//                    break;
//                }

                //try to add file into the index
                indexer.indexFileOrDirectory(s);
            } catch (Exception e) {
                System.out.println("Error indexing " + s + " : " + e.getMessage());
            }
//        }

        //===================================================
        //after adding, we always have to call the
        //closeIndex, otherwise the index is not created
        //===================================================
        indexer.closeIndex();

        //=========================================================
        // Now search
        //=========================================================
        Path path = FileSystems.getDefault().getPath(indexLocation);
        IndexReader reader = DirectoryReader.open(FSDirectory.open(path));
        IndexSearcher searcher = new IndexSearcher(reader);

        //*** first!!!
        // http://stackoverflow.com/questions/14211974/how-can-i-read-and-print-lucene-index-4-0?rq=1
        /*
        String field = "content";
//        FieldsEnum fieldsiterator;
//To Simplify, you can rely on DefaultSimilarity to calculate tf and idf for you.
//numDocs and maxDoc are not the same thing:
//        int numDocs = reader.numDocs();
//        int maxDoc = reader.maxDoc();

        for (int i=0; i<1; i++) {
//            fieldsiterator = reader.getTermVectors(i).iterator();
            Fields ff = reader.getTermVectors(i);
            Terms terms = ff.terms(field);
            TermsEnum termsiterator;
            termsiterator = terms.iterator();
                while (   termsiterator.next() != null) {
                    //id = document id, field = field name
                    //String representations of the current term
                    String termtext = termsiterator.term().utf8ToString();
                    //Get idf, using docfreq from the reader.
                    //I haven't tested this, and I'm not quite 100% sure of the context of this method.
                    //If it doesn't work, idfalternate below should.
                    int idf = termsiterator.docFreq();
        DefaultSimilarity freqcalculator = new DefaultSimilarity();
                    float idfalternate = freqcalculator.idf(
                            reader.docFreq(
                            new Term("content",
                            termsiterator.term()
                            )
                            ), 1);

                        System.out.println("indexing " +
                                termtext +
                                ": "+ idf +
                                " - " + idfalternate);
                }
        }
        */

        //*** second !!!
        // https://github.com/C3Junior/c3-web-next/blob/d14d1452a286df80f8666ac96ad4ca9ad6a2fb6c/c3web-server/src/main/scala/com/ifunsoftware/c3web/annotation/indexator/LuceneSimpleIndexator.scala

//        Fields fields = MultiFields.getFields(reader);
//        Bits liveDocs = MultiFields.getLiveDocs(reader);
//        Integer k=0;
//        if (fields != null) {
//            Terms terms = fields.terms("content");
//            if (terms != null) {
//                TermsEnum termsEnum = terms.iterator();
//                BytesRef term = termsEnum.next();
//
//                while (term != null) {
//                    DocsEnum docsEnum = termsEnum.docs(liveDocs, null);
//
//                    String res = term.utf8ToString();
//                    Integer frequency = docsEnum.freq();
//                        System.out.println("indexing " +k+": "+ res + " - " + frequency);
//                    term = termsEnum.next();
//                }
//            }
//        }

        //*** third!!!
        // http://stackoverflow.com/questions/16847857/how-do-you-read-the-index-in-lucene-to-do-a-search

        Term t = new Term("content", "search");
        // Get the top 10 docs
        Query query = new TermQuery(t);
        TopDocs tops= searcher.search(query, 10);
        ScoreDoc[] scoreDoc = tops.scoreDocs;
        System.out.println(scoreDoc.length);
        for (ScoreDoc score : scoreDoc){
            System.out.println("DOC " + score.doc + " SCORE " + score.score);
        }
        int freq = reader.docFreq(t);
        System.out.println("FREQ " + freq);

        //***

        TopScoreDocCollector collector = TopScoreDocCollector.create(5, new ScoreDoc(5, 5));

        s = "";
        while (!s.equalsIgnoreCase("q")) {
            try {
                System.out.println("Enter the search query (q=quit):");
                s = br.readLine();
                if (s.equalsIgnoreCase("q")) {
                    break;
                }
                Query q = new QueryParser("contents", analyzer).parse(s);
                searcher.search(q, collector);
                ScoreDoc[] hits = collector.topDocs().scoreDocs;

                // 4. display results
                System.out.println("Found " + hits.length + " hits.");
                for(int i=0;i<hits.length;++i) {
                    int docId = hits[i].doc;
                    Document d = searcher.doc(docId);
                    System.out.println((i + 1) + ". " + d.get("path") + " score=" + hits[i].score);
                }

            } catch (Exception e) {
                System.out.println("Error searching " + s + " : " + e.getMessage());
            }
        }

    }

    /**
     * Constructor
     * @param indexDir the name of the folder in which the index should be created
     * @throws java.io.IOException when exception creating index.
     */
    ExtractKeywords(String indexDir) throws IOException {
        // the boolean true parameter means to create a new index everytime,
        // potentially overwriting any existing files there.
        Path path = FileSystems.getDefault().getPath(indexDir);
        FSDirectory dir = FSDirectory.open(path);


        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        writer = new IndexWriter(dir, config);
    }

    /**
     * Indexes a file or directory
     * @param fileName the name of a text file or a folder we wish to add to the index
     * @throws java.io.IOException when exception
     */
    public void indexFileOrDirectory(String fileName) throws IOException {
        //===================================================
        //gets the list of files in a folder (if user has submitted
        //the name of a folder) or gets a single file name (is user
        //has submitted only the file name)
        //===================================================
        addFiles(new File(fileName));

        int originalNumDocs = writer.numDocs();
        for (File f : queue) {
            FileReader fr = null;
            try {
                Document doc = new Document();

                //===================================================
                // add contents of file
                //===================================================
                fr = new FileReader(f);
                doc.add(new TextField("contents", fr));
//                doc.add(new StringField("path", f.getPath(), Field.Store.YES));
//                doc.add(new StringField("filename", f.getName(), Field.Store.YES));

                writer.addDocument(doc);
                System.out.println("Added: " + f);
            } catch (Exception e) {
                System.out.println("Could not add: " + f);
            } finally {
                fr.close();
            }
        }

        int newNumDocs = writer.numDocs();
        System.out.println("");
        System.out.println("************************");
        System.out.println((newNumDocs - originalNumDocs) + " documents added.");
        System.out.println("************************");

        queue.clear();
    }

    private void addFiles(File file) {

        if (!file.exists()) {
            System.out.println(file + " does not exist.");
        }
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                addFiles(f);
            }
        } else {
            String filename = file.getName().toLowerCase();
            //===================================================
            // Only index text files
            //===================================================
            if (filename.endsWith(".htm") || filename.endsWith(".html") ||
                    filename.endsWith(".xml") || filename.endsWith(".txt")) {
                queue.add(file);
            } else {
                System.out.println("Skipped " + filename);
            }
        }
    }

    /**
     * Close the index.
     * @throws java.io.IOException when exception closing
     */
    public void closeIndex() throws IOException {
        writer.close();
    }
}

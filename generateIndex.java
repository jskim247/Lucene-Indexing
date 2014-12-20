import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

class Index
{
	private String corpusPath;
	public void setCorpusPath(String path)
	{
		corpusPath = path;
	}
	
	private String extract(StringBuilder buf, String startTag, String endTag)
	{
		String stringBetweenTags = new String();
		int k1 = buf.indexOf(startTag);
		while(k1 > 0)    
		{
		   k1 += startTag.length();
		   int k2 = buf.indexOf(endTag,k1);
		      
		   if (k2>=0)
		   {
			   stringBetweenTags +=(" " + buf.substring(k1,k2).trim());  
		   }
		   
		   k1 = buf.indexOf(startTag, k2);
		}
		return stringBetweenTags;	  
	}
	
	private String readFile(String file) throws IOException 
	{
		file = corpusPath+"\\"+file;
		FileReader fileReader = new FileReader (file);
		BufferedReader reader = new BufferedReader(fileReader);
	    String         line = null;
	    StringBuilder  stringBuilder = new StringBuilder();
	    String         ls = System.getProperty("line.separator");

	    while((line = reader.readLine()) != null ) 
	    {
	        stringBuilder.append( line );
	        stringBuilder.append( ls );
	    }

	    return stringBuilder.toString();
	}
	
	public void makeIndex( File indexDir, File docDir) throws CorruptIndexException,LockObtainFailedException,
		IOException
	{
		Directory directory = FSDirectory.open(indexDir);

		Analyzer analyzer = new StandardAnalyzer();

		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_4_10_0, analyzer);

		iwc.setOpenMode(OpenMode.CREATE);

		IndexWriter indexWriter = new IndexWriter(directory, iwc);

		for (File f: docDir.listFiles())
		{
			String fileName = f.getName();
			String readBuffer = readFile(fileName);
			StringBuilder builder = new StringBuilder(readBuffer);
			
			String startDocTag = "<DOC>";
			String endDocTag = "</DOC>";
			int docStart = builder.indexOf(startDocTag);
			while(docStart != -1)    
			{
			   docStart += startDocTag.length();
			   int docEnd = builder.indexOf(endDocTag,docStart);
			   
			   if(docEnd > 0)
			   {
				   StringBuilder document = new StringBuilder(builder.substring(docStart,docEnd).trim());
				   
				   Document doc = new Document();
				   String docNo = extract(document,"<DOCNO>", "</DOCNO>");
				   String dateLine = extract(document,"<DATELINE>", "</DATELINE>");
				   String head = extract(document,"<HEAD>", "</HEAD>");
				   String byLine = extract(document,"<BYLINE>", "</BYLINE>");
				   String text = extract(document,"<TEXT>", "</TEXT>");

				   doc.add(new StringField("DOCNO", docNo, Field.Store.YES));
				   doc.add(new StringField("DATELINE", dateLine, Field.Store.YES));
				   doc.add(new TextField("HEAD",head, Field.Store.YES));
				   doc.add(new TextField("BYLINE",byLine, Field.Store.YES));
				   doc.add(new TextField("TEXT",text, Field.Store.YES));
				   indexWriter.addDocument(doc); 
			   }
			   
			   docStart = builder.indexOf(startDocTag, docEnd);
			}	   
		}
		indexWriter.forceMerge(1);
		indexWriter.commit();
		indexWriter.close();
	}
	
	void statisticalInformation(File indexDir) throws IOException,CorruptIndexException,LockObtainFailedException
	{
		IndexReader indexReader = DirectoryReader.open(FSDirectory.open(indexDir));
	
		System.out.println("Total Number of documents in Corpus is = " +indexReader.maxDoc());
		
		System.out.println("Number of documents containing the term\"new\" for field\"TEXT\": "+indexReader.docFreq(new Term("TEXT","new")));
		
		System.out.println("Number of occurences of\"new\" in the field\"TEXT\": "+indexReader.totalTermFreq(new Term("TEXT","new")));
		
		Terms vocabulary = MultiFields.getTerms(indexReader,"TEXT");
		
		System.out.println("Size of the vocabulary for TEXT field:"+vocabulary.size());
		
		System.out.println("Number of documents that have at least one term for TEXT field: " +vocabulary.getDocCount());
		
		System.out.println("Number of tokens for TEXT field:"+vocabulary.getSumTotalTermFreq());
		
		System.out.println("Number of postings for TEXT field:"+vocabulary.getSumDocFreq());
		
		TermsEnum iterator = vocabulary.iterator(null);
		BytesRef byteRef = null;
		System.out.println("\n*******Vocabulary-Start**********");
		
		while((byteRef = iterator.next()) !=null) 
		{
			String term = byteRef.utf8ToString();
			System.out.print(term+"\t");
		}
		
		System.out.println("\n*******Vocabulary-End**********");
		indexReader.close();
	}
}

public class generateIndex 
{
	public static void main(String args[]) throws IOException
	{
		File docDir = new File(args[0]);
		File indexDir = new File(args[1]);
		
		assert(docDir.exists());
		assert(indexDir.exists());
		
		Index index = new Index();
		index.setCorpusPath(args[0]);
		index.makeIndex(indexDir,docDir);
		index.statisticalInformation(indexDir);

	}
}

Lucene-Indexing
===============

Generating Lucene Index for Experiment Corpus (AP89)

Created index by using Lucene. 
From Lucene viewpoint,each document is a collection of pre-defined fields, where a field supplies a field name
and value. By using Lucene API (in Java), we can easily generate corpus index (inverted
index, and, then, we can calculate TF and IDF by using Lucene search API.
Lucene index with the following fields: 1.DOCNO, 2. HEAD (merge two < HEAD >), 3. <BYLINE> (merge two < HEAD >), 
4.<DATELINE>, and 5. <TEXT>.

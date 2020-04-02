package org.jabref.asv;

import org.jabref.logic.bibtex.BibEntryWriter;
import org.jabref.logic.bibtex.LatexFieldFormatter;
import org.jabref.logic.bibtex.LatexFieldFormatterPreferences;
import org.jabref.logic.importer.FetcherException;
import org.jabref.logic.importer.ImportFormatPreferences;
import org.jabref.logic.importer.fetcher.SpringerFetcher;
import org.jabref.logic.search.DatabaseSearcher;
import org.jabref.logic.search.SearchQuery;
import org.jabref.model.database.BibDatabase;
import org.jabref.model.database.BibDatabaseMode;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.BibtexEntryTypes;
import org.jabref.model.entry.FieldName;
import org.jabref.model.groups.AllEntriesGroup;
import org.jabref.model.groups.AutomaticKeywordGroup;
import org.jabref.model.groups.ExplicitGroup;
import org.jabref.model.groups.GroupHierarchyType;
import org.jabref.model.util.DummyFileUpdateMonitor;
import org.jabref.model.util.FileUpdateMonitor;
import org.jabref.testutils.category.AsvTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@AsvTest
public class AsvUnitTest
{
    private static ImportFormatPreferences importFormatPreferences;
    private BibEntryWriter writer;
    private final FileUpdateMonitor fileMonitor = new DummyFileUpdateMonitor();

    @BeforeEach
    public void setup()
    {
        importFormatPreferences = mock(ImportFormatPreferences.class, Answers.RETURNS_DEEP_STUBS);
        LatexFieldFormatterPreferences latexFieldFormatterPreferences = mock(LatexFieldFormatterPreferences.class, Answers.RETURNS_DEEP_STUBS);
        writer = new BibEntryWriter(new LatexFieldFormatter(latexFieldFormatterPreferences), true);
    }

    @AsvTest
    @Test
    public void testGenerateBibTex() throws IOException
    {
        StringWriter bibTexOutput = new StringWriter();

        // Create a library entry
        BibEntry entry = new BibEntry(BibtexEntryTypes.ARTICLE);
        entry.setField(FieldName.TITLE, "An Incredibly Interesting Article");
        entry.setField(FieldName.AUTHOR, "Mr. Know It All");
        entry.setField(FieldName.JOURNAL, "International Journal of Interesting Stuff");

        writer.write(entry, bibTexOutput, BibDatabaseMode.BIBTEX);

        String result = bibTexOutput.toString();

        String expected = "\n@Article{,\n" +
                "  author  = {Mr. Know It All},\n" +
                "  title   = {An Incredibly Interesting Article},\n" +
                "  journal = {International Journal of Interesting Stuff},\n" +
                "}\n";

        assertEquals(expected, result);
    }

    @AsvTest
    @Test
    public void testGetSpringerResults() throws FetcherException
    {
        SpringerFetcher sf = new SpringerFetcher();

        // Surely something should be written about quantum stuff...
        List<BibEntry> results = sf.performSearch("quantum");

        // Assert that there are more than 0 results
        assertTrue(results.size() > 0);

        // Iterate over the results and check if they are all actually filled, aka real results
        Iterator<BibEntry> i = results.iterator();
        while (i.hasNext()) {
            BibEntry be = i.next();

            assertNotNull(be.getField(FieldName.TITLE));
            assertNotNull(be.getField(FieldName.AUTHOR));
            assertNotNull(be.getField(FieldName.JOURNAL));
        }
    }

    @AsvTest
    @Test
    public void testGetLocalResults()
    {
        BibDatabase bdb = new BibDatabase();

        BibEntry entry1 = new BibEntry(BibtexEntryTypes.ARTICLE);
        entry1.setField(FieldName.TITLE, "An Incredibly Interesting Article");
        entry1.setField(FieldName.AUTHOR, "Mr. Know It All");
        entry1.setField(FieldName.JOURNAL, "International Journal of Interesting Stuff");

        BibEntry entry2 = new BibEntry(BibtexEntryTypes.ARTICLE);
        entry2.setField(FieldName.TITLE, "An Article About Software");
        entry2.setField(FieldName.AUTHOR, "John Doe");
        entry2.setField(FieldName.JOURNAL, "The Software Journal");

        BibEntry entry3 = new BibEntry(BibtexEntryTypes.ARTICLE);
        entry3.setField(FieldName.TITLE, "An Article About Cooking");
        entry3.setField(FieldName.AUTHOR, "Jane Doe");
        entry3.setField(FieldName.JOURNAL, "Cooking International");

        BibEntry entry4 = new BibEntry(BibtexEntryTypes.ARTICLE);
        entry4.setField(FieldName.TITLE, "Global Warming Explained");
        entry4.setField(FieldName.AUTHOR, "Donald J. Trump");
        entry4.setField(FieldName.JOURNAL, "International Journal for Environmental Research");

        // My brother wrote this one, no idea what it's about but sounds cool
        BibEntry entry5 = new BibEntry(BibtexEntryTypes.ARTICLE);
        entry5.setField(FieldName.TITLE, "CCMV-Based Enzymatic Nanoreactors");
        entry5.setField(FieldName.AUTHOR, "Ruiter, Mark V. and Putri, Rindia M. and Cornelissen, Jeroen J. L. M.");
        entry5.setField(FieldName.JOURNAL, "Virus-Derived Nanoparticles for Advanced Technologies");

        // Insert the entries into the library
        bdb.insertEntry(entry1);
        bdb.insertEntry(entry2);
        bdb.insertEntry(entry3);
        bdb.insertEntry(entry4);
        bdb.insertEntry(entry5);

        // Create a query that should result in entry2's title
        SearchQuery q = new SearchQuery("software", false, false);
        // Get the results
        List<BibEntry> results = new DatabaseSearcher(q, bdb).getMatches();
        // A result should be found
        assertTrue(results.size() > 0);
        // The result should be entry2, which is the only one that matches 'software'
        assertEquals(entry2, results.iterator().next());

        // Do a few more, searching by journal and author
        q = new SearchQuery("Ruiter Mark V.", false, false);
        results = new DatabaseSearcher(q, bdb).getMatches();
        assertTrue(results.size() > 0);
        assertEquals(entry5, results.iterator().next());

        q = new SearchQuery("Environmental", false, false);
        results = new DatabaseSearcher(q, bdb).getMatches();
        assertTrue(results.size() > 0);
        assertEquals(entry4, results.iterator().next());
    }

    @AsvTest
    @Test
    public void testGrouping()
    {
        BibEntry entry1 = new BibEntry(BibtexEntryTypes.ARTICLE);
        entry1.setField(FieldName.TITLE, "An Incredibly Interesting Article");
        entry1.setField(FieldName.AUTHOR, "Mr. Know It All");
        entry1.setField(FieldName.JOURNAL, "International Journal of Interesting Stuff");

        // create a basic group
        ExplicitGroup group = new ExplicitGroup("My group", GroupHierarchyType.INDEPENDENT, ",");
        group.add(entry1);

        // Check if the group does indeed contain the entry
        assertTrue(group.contains(entry1));
    }
}

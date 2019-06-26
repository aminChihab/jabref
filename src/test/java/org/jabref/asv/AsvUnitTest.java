package org.jabref.asv;

import org.jabref.logic.bibtex.BibEntryWriter;
import org.jabref.logic.bibtex.LatexFieldFormatter;
import org.jabref.logic.bibtex.LatexFieldFormatterPreferences;
import org.jabref.logic.importer.FetcherException;
import org.jabref.logic.importer.ImportFormatPreferences;
import org.jabref.logic.importer.fetcher.SpringerFetcher;
import org.jabref.model.database.BibDatabaseMode;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.BibtexEntryTypes;
import org.jabref.model.entry.FieldName;
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
}

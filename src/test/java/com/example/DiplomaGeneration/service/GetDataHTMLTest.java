package com.example.DiplomaGeneration.service;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GetDataHTMLTest {
    @InjectMocks
    private GetDataHTML getDataHTML;

    @Mock
    private MultipartFile fileMock;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        getDataHTML.pageSize = 0;
        getDataHTML.place.clear();
        getDataHTML.teams.clear();
        getDataHTML.tasks.clear();
    }

    @Test
    public void testMergeElements() {
        Elements elements1 = new Elements();
        elements1.add(new Element("div").text("Element1"));
        elements1.add(new Element("div").text("Element2"));
        Elements elements2 = new Elements();
        elements2.add(new Element("div").text("Element3"));
        Elements merged = getDataHTML.mergeElements(elements1, elements2);
        assertEquals(3, merged.size());
        assertEquals("Element1", merged.get(0).text());
        assertEquals("Element3", merged.get(1).text());
        assertEquals("Element2", merged.get(2).text());
    }

    @Test
    public void testGetDataWithFile() throws IOException {
        String html = "<table class=\"standings\">\n<tr participantid=\"193847392\">\n" +
                "    <td class=\"dark left\">\n" +
                "        \n" +
                "            1\n" +
                "    </td>\n" +
                "\n" +
                "    <td class=\"contestant-cell dark\" style=\"text-align:left;padding-left:1em;\">\n" +
                "        Kazan FU A: A, B, C\n" +
                "    </td>\n" +
                "\n" +
                "\n" +
                "    <td class=\"dark\">12</td>\n</tr></tbody></table>";
        ByteArrayInputStream input = new ByteArrayInputStream(html.getBytes());
        when(fileMock.getInputStream()).thenReturn(input);
        when(fileMock.isEmpty()).thenReturn(false);
        getDataHTML.getData("", fileMock);
        assertEquals(1, getDataHTML.pageSize);
        assertEquals(1, getDataHTML.place.size());
        assertEquals(12, getDataHTML.tasks.get(0));
        assertEquals("Kazan FU A", getDataHTML.teams.get(0));
    }
}


package nl.grand.news.text;

import nl.grand.news.translate.DeepLTranslateService;
import nl.grand.news.translate.TranslateService;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NewsHandlerTest {
    @org.junit.jupiter.api.Test
    void translate (){
        TranslateService service = new TranslateService();
        String translated = service.translateText("Hello, how are you?", "en", "ru");
        System.out.println(translated);
    }


    @org.junit.jupiter.api.Test
    void getDutchNews1() throws IOException {
        // Мокируем Jsoup и его поведение
        NewsHandler newsFetcher = new NewsHandler(new TranslateService(),new DeepLTranslateService());

        // Мокируем соединение и ответ от Jsoup
        Connection mockedConnection = mock(Connection.class);
        Document mockedDocument = mock(Document.class);
        Elements mockedElements = mock(Elements.class);

        // Мокируем ответ, который вернется при вызове .get() на Jsoup.connect()
        when(Jsoup.connect(anyString())).thenReturn(mockedConnection);
        when(mockedConnection.userAgent(anyString())).thenReturn(mockedConnection);
        when(mockedConnection.timeout(anyInt())).thenReturn(mockedConnection);
        when(mockedConnection.followRedirects(anyBoolean())).thenReturn(mockedConnection);
        when(mockedConnection.get()).thenReturn(mockedDocument);

        // Мокируем выбор элементов страницы (заголовки статей)
        when(mockedDocument.select(anyString())).thenReturn(mockedElements);
        when(mockedElements.size()).thenReturn(3);
        when(mockedElements.get(0).attr(anyString())).thenReturn("https://www.dutchnews.nl/article1");
        when(mockedElements.get(1).attr(anyString())).thenReturn("https://www.dutchnews.nl/article2");
        when(mockedElements.get(2).attr(anyString())).thenReturn("https://www.dutchnews.nl/article3");

        // Вызов метода, который мы тестируем
        List<String> newsList = newsFetcher.getDutchNews1();

        // Проверка результата
        assertNotNull(newsList);
        assertEquals(3, newsList.size());
        assertTrue(newsList.contains("https://www.dutchnews.nl/article1"));
        assertTrue(newsList.contains("https://www.dutchnews.nl/article2"));
        assertTrue(newsList.contains("https://www.dutchnews.nl/article3"));
    }

    @Test
    public void testGetDutchNews1_EmptyResponse() throws IOException {
        // Мокируем соединение и ответ от Jsoup
        NewsHandler newsFetcher = new NewsHandler(new TranslateService(),new DeepLTranslateService());
        Connection mockedConnection = mock(Connection.class);
        Document mockedDocument = mock(Document.class);
        Elements mockedElements = mock(Elements.class);

        when(Jsoup.connect(anyString())).thenReturn(mockedConnection);
        when(mockedConnection.get()).thenReturn(mockedDocument);
        when(mockedDocument.select(anyString())).thenReturn(mockedElements);
        when(mockedElements.size()).thenReturn(0);

        List<String> newsList = newsFetcher.getDutchNews1();

        assertNotNull(newsList);
        assertTrue(newsList.isEmpty());
    }
}

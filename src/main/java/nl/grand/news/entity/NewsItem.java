package nl.grand.news.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NewsItem {
    private String title;
    private String preview;
    private String url;
}

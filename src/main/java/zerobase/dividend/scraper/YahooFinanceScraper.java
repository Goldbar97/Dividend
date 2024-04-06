package zerobase.dividend.scraper;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import zerobase.dividend.model.Company;
import zerobase.dividend.model.Dividend;
import zerobase.dividend.model.ScrapedResult;
import zerobase.dividend.type.Month;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class YahooFinanceScraper implements Scraper {
    
    private static final String STATIC_URL = "https://finance.yahoo" +
            ".com/quote/%s/history?period1=%d&period2=%d&filter=div&frequency" +
            "=1mo";
    private static final String SUMMARY_URL = "https://finance.yahoo" +
            ".com/quote/%s";
    
    private static final long START_TIME = 86400; // 60 * 60 * 24 = 86400초 = 1일
    
    @Override
    public ScrapedResult scrap(Company company) {
        ScrapedResult scrapedResult = new ScrapedResult();
        scrapedResult.setCompany(company);
        
        try {
            long now = System.currentTimeMillis() / 1000;
            List<Dividend> dividends = new ArrayList<>();
            
            String url = String.format(STATIC_URL, company.getTicker(),
                                       START_TIME, now);
            Connection connection = Jsoup.connect(url);
            Document document = connection.get();
            
            Elements elements = document.getElementsByAttributeValue(
                    "data-test", "historical-prices");
            Element element = elements.getFirst();
            
            Element tbody = element.children().get(1);
            
            for (Element e : tbody.children()) {
                String txt = e.text();
                
                String[] splits = txt.split(" ");
                int month = Month.strToNumber(splits[0]);
                int day = Integer.parseInt(splits[1].replace(",", ""));
                int year = Integer.parseInt(splits[2]);
                String dividend = splits[3];
                
                if (month < 0) {
                    throw new RuntimeException("Unexpected Month enum value " +
                                                       "-> " + splits[0]);
                }
                
                dividends.add(
                        new Dividend(
                                LocalDateTime.of(year, month, day, 0, 0),
                                dividend));
            }
            scrapedResult.setDividendEntities(dividends);
            
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        return scrapedResult;
    }
    
    @Override
    public Company scrapCompanyByTicker(String ticker) {
        String url = String.format(SUMMARY_URL, ticker);
        
        try {
            Document document = Jsoup.connect(url).get();
            Element element = document.getElementsByTag("h1").getFirst();
            
            String title = element.text().split("\\(")[0].trim();
            
            return new Company(ticker, title);
            
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

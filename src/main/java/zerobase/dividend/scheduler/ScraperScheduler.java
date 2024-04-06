package zerobase.dividend.scheduler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import zerobase.dividend.entity.CompanyEntity;
import zerobase.dividend.entity.DividendEntity;
import zerobase.dividend.model.Company;
import zerobase.dividend.model.ScrapedResult;
import zerobase.dividend.repository.CompanyRepository;
import zerobase.dividend.repository.DividendRepository;
import zerobase.dividend.scraper.YahooFinanceScraper;
import zerobase.dividend.type.CacheKey;

import java.util.List;

@AllArgsConstructor
@Component
@EnableCaching
@Slf4j
public class ScraperScheduler {
  
    private final CompanyRepository companyRepository;
    private final YahooFinanceScraper yahooFinanceScraper;
    private final DividendRepository dividendRepository;
    
    @CacheEvict(value = CacheKey.KEY_FINANCE, allEntries = true)
    @Scheduled(cron = "${scheduler.scrap.yahoo}")
    public void yahooFinanceScheduling() {
        // 저장된 회사 목록 조회
        List<CompanyEntity> companies = companyRepository.findAll();
        
        // 회사마다 배당금 정보를 새로 스크래핑
        for (CompanyEntity company : companies) {
            log.info("scraping scheduler is started -> " + company.getName());
            ScrapedResult scrapedResult = yahooFinanceScraper.scrap(
                    new Company(company.getName(), company.getTicker()));
            
            // 스크래핑한 배당금 정보 중 DB 에 없는 값 저장
            scrapedResult.getDividendEntities().stream()
                    // Dividend -> DividendEntitiy 매핑
                    .map(e -> new DividendEntity(company.getId(), e))
                    // DividendEntity 하나씩 중복확인 후 저장
                    .forEach(e -> {
                        boolean exists =
                                dividendRepository.existsByCompanyIdAndDate(
                                        e.getCompanyId(), e.getDate());
                        if (!exists) {
                            dividendRepository.save(e);
                        }
                    });
            
            // 요청마다 쉬는 시간 입력
            try {
                Thread.sleep(3000); // 3초
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

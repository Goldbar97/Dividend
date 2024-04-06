package zerobase.dividend.service;

import lombok.AllArgsConstructor;
import org.apache.commons.collections4.Trie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import zerobase.dividend.entity.CompanyEntity;
import zerobase.dividend.entity.DividendEntity;
import zerobase.dividend.exception.CompanyException;
import zerobase.dividend.model.Company;
import zerobase.dividend.model.ScrapedResult;
import zerobase.dividend.repository.CompanyRepository;
import zerobase.dividend.repository.DividendRepository;
import zerobase.dividend.scraper.Scraper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CompanyService {
    
    private final Trie trie;
    private final Scraper yahooFinanceScraper;
    
    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;
    
    public void addAutocompleteKeyword(String keyword) {
        trie.put(keyword, null);
    }
    
    public List<String> autocomplate(String keyword) {
        return (List<String>) trie.prefixMap(keyword).keySet().stream()
                .collect(Collectors.toList());
    }
    
    public void deleteAutocompleteKeyword(String keyword) {
        trie.remove(keyword);
    }
    
    public String deleteCompany(String ticker) {
        CompanyEntity companyEntity = companyRepository.findByTicker(ticker)
                .orElseThrow(CompanyException.NoCompanyException::new);
        
        dividendRepository.deleteAllByCompanyId(companyEntity.getId());
        companyRepository.delete(companyEntity);
        deleteAutocompleteKeyword(companyEntity.getName());
        
        return companyEntity.getName();
    }
    
    public Page<CompanyEntity> getAllCompany(Pageable pageable) {
        return companyRepository.findAll(pageable);
    }
    
    public List<String> getCompanyNamesByKeyword(String keyword) {
        Pageable limit = PageRequest.of(0, 10);
        Page<CompanyEntity> companyEntities =
                companyRepository.findByNameStartingWithIgnoreCase(
                        keyword, limit);
        
        return companyEntities.stream()
                .map(CompanyEntity::getName)
                .collect(Collectors.toList());
    }
    
    public Company save(String ticker) {
        boolean exists = companyRepository.existsByTicker(ticker);
        if (exists) {
            throw new CompanyException.AlreadyExistTickerException(ticker);
        }
        
        return storeCompanyAndDividend(ticker);
    }
    
    private Company storeCompanyAndDividend(String ticker) {
        Company company = yahooFinanceScraper.scrapCompanyByTicker(ticker);
        if (ObjectUtils.isEmpty(company)) {
            throw new CompanyException.FailedScrapTickerException(ticker);
        }
        
        ScrapedResult scrapedResult = yahooFinanceScraper.scrap(company);
        
        CompanyEntity saved = companyRepository.save(
                new CompanyEntity(company));
        
        List<DividendEntity> dividendEntities =
                scrapedResult.getDividendEntities()
                        .stream()
                        .map(e -> new DividendEntity(saved.getId(), e))
                        .collect(Collectors.toList());
        
        dividendRepository.saveAll(dividendEntities);
        return company;
    }
}
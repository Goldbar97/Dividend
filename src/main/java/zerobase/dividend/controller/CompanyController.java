package zerobase.dividend.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import zerobase.dividend.entity.CompanyEntity;
import zerobase.dividend.exception.CompanyException;
import zerobase.dividend.model.Company;
import zerobase.dividend.service.CompanyService;
import zerobase.dividend.type.CacheKey;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@RequestMapping("/company")
@RestController
@Slf4j
public class CompanyController {
    private final CompanyService companyService;
    private final CacheManager redisCacheManager;
    
    @PostMapping("/multiple")
    public ResponseEntity<?> addCompany(@RequestBody List<Company> request) {
        List<String> emptyTickerList = new ArrayList<>();
        List<String> dupeTickerList = new ArrayList<>();
        List<Company> saveSuccessList = new ArrayList<>();
        
        for (Company item : request) {
            String ticker = item.getTicker();
            try {
                if (ObjectUtils.isEmpty(ticker)) {
                    emptyTickerList.add(ticker);
                    continue;
                }
                
                Company company = companyService.save(ticker);
                saveSuccessList.add(company);
                companyService.addAutocompleteKeyword(company.getName());
                Thread.sleep(3000);
            } catch (CompanyException.AlreadyExistTickerException e) {
                dupeTickerList.add(ticker);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        boolean empty = emptyTickerList.isEmpty();
        boolean dupe = dupeTickerList.isEmpty();
        
        if (empty && !dupe) {
            throw new RuntimeException("dupe tickers -> " + dupeTickerList);
        } else if (!empty && dupe) {
            throw new RuntimeException("empty tickers -> " + emptyTickerList);
        } else if (!empty && !dupe) {
            throw new RuntimeException(
                    "empty tickers -> " + emptyTickerList + "\n" + "dupe " +
                            "tickers -> " + dupeTickerList);
        }
        
        return ResponseEntity.ok(saveSuccessList);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('WRITE')")
    public ResponseEntity<?> addCompany(@RequestBody Company request) {
        log.info("Post request received. Ticker: {}", request.getTicker());
        String ticker = request.getTicker();
        if (ObjectUtils.isEmpty(ticker)) {
            throw new RuntimeException("ticker is empty");
        }
        
        Company company = companyService.save(ticker);
        companyService.addAutocompleteKeyword(company.getName());
        log.info("Post request success. Ticker: {}", request.getTicker());
        return ResponseEntity.ok(company);
    }
    
    @GetMapping("/autocomplete")
    public ResponseEntity<?> autocomplete(@RequestParam String keyword) {
        List<String> autocomplate = companyService.autocomplate(keyword);
        return ResponseEntity.ok(autocomplate);
    }
    
    @DeleteMapping("/{ticker}")
    @PreAuthorize("hasRole('WRITE')")
    public ResponseEntity<?> deleteCompany(@PathVariable String ticker) {
        log.info("Delete request received. Ticker: {}", ticker);
        String companyName = companyService.deleteCompany(ticker);
        clearFinanceCache(companyName);
        log.info("Delete request success. Ticker: {}", ticker);
        
        return ResponseEntity.ok(companyName);
    }
    
    public void clearFinanceCache(String companyName) {
        redisCacheManager.getCache(CacheKey.KEY_FINANCE).evict(companyName);
    }
    
    @GetMapping
    @PreAuthorize("hasRole('READ')")
    public ResponseEntity<?> searchCompany(final Pageable pageable) {
        Page<CompanyEntity> allCompany = companyService.getAllCompany(pageable);
        
        return ResponseEntity.ok(allCompany);
    }
}
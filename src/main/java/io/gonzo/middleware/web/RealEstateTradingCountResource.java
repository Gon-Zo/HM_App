package io.gonzo.middleware.web;

import io.gonzo.middleware.service.RealEstateTradingCountService;
import io.gonzo.middleware.web.dto.RealEstateTradingCountDTO;
import io.gonzo.middleware.web.dto.RealEstateTradingCountStoreDTO;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/middleware/real-estate-trading-count")
@AllArgsConstructor
public class RealEstateTradingCountResource {

    private final RealEstateTradingCountService service;

    @GetMapping("")
    public List<RealEstateTradingCountDTO> showByRealEstateTradingCount(RealEstateTradingCountStoreDTO dto) {
        return service.getByRealEstateTradingCount(dto);
    }

}

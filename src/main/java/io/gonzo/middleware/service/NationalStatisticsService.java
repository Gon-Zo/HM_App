package io.gonzo.middleware.service;

import io.gonzo.middleware.enums.NationalStatisticTypes;
import io.gonzo.middleware.web.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.gonzo.middleware.utils.ApiServerUtils.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class NationalStatisticsService {

    @Value("${app.key}")
    private String key;

    private final AreaCodeService areaCodeService;

    /**
     * [전국 조회] 부동산 거래 건수 조회
     *
     * @param dto
     * @return
     */
    public List getNumberOfTransactionsByNationwide(TransactionsStoreDTO.Nationwide dto) {

        List<AreaCodeDTO.IAreaCodeParents> parentsList = areaCodeService.getByParentsTypeAreaList();

        String startMonth = dto.getStartDate();

        String endMonth = dto.getEndDate();

        NationalStatisticTypes apiCode = dto.getApiCode();

        boolean isYear = setYearYn(dto.getApiCode().name());

        return (List) parentsList
                .stream()
                .map(parents -> {

                    TransactionsStoreDTO.Default transactionsStoreDTO = TransactionsStoreDTO.Default.builder()
                            .startDate(startMonth)
                            .endDate(endMonth)
                            .apiCode(apiCode)
                            .region(parents.getCode())
                            .typeCode(dto.getTypeCode())
                            .build();

                    return getNumberOfTransactions(transactionsStoreDTO, isYear);
                })
                .flatMap(Collection::parallelStream)
                .collect(Collectors.toList());

    }

    public List getNumberOfTransactions(TransactionsStoreDTO.Default dto, boolean isYear) {

        List<TransactionsDTO.Base> baseList = createByNumberOfTransactions(dto, isYear);

        if (CollectionUtils.isEmpty(baseList)) {
            return new ArrayList();
        }

        switch (dto.getApiCode()) {
            case RealEstateTradingCount: {
                return createByDefaultDTO(baseList, isYear);
            }
            default:
                return baseList;
        }

    }

    private List<TransactionsDTO.Default> createByDefaultDTO(List<TransactionsDTO.Base> baseList, boolean isYear) {

        TransactionsDTO.Base baseStatisticsDTO = baseList.get(0);

        String regionNm = baseStatisticsDTO.getRegionNm();

        return Arrays.stream(baseStatisticsDTO.getRsRow().split("\\|"))
                .map(baseItem -> {

                    String[] arrayOfRsRow = baseItem.split(",");

                    String standardDate = passerByStandardDate(arrayOfRsRow[0], isYear);

                    return TransactionsDTO.Default.builder()
                            .regionName(regionNm)
                            .date(standardDate)
                            .count(Integer.valueOf(arrayOfRsRow[1]))
                            .build();

                })
                .collect(Collectors.toList());

    }

    /**
     * 부동산 거래 건수 조회
     *
     * @param dto
     * @return
     */
    private List<TransactionsDTO.Base> createByNumberOfTransactions(TransactionsStoreDTO.Default dto, boolean isYear) {

        try {

            StringBuffer stringBuffer = new StringBuffer();

            String url = "http://openapi.reb.or.kr/OpenAPI_ToolInstallPackage/service/rest/RealEstateTradingSvc/";

            url += dto.getApiCode().getValue();

            // todo : api 별로 파라미터 바꾸기
            stringBuffer.append(url)
                    .append("?ServiceKey=")
                    .append(key)
                    .append(getByDateFormat(isYear, dto.getStartDate(), Boolean.TRUE))
                    .append(getByDateFormat(isYear, dto.getEndDate(), Boolean.FALSE))
                    .append("&region=")
                    .append(dto.getRegion())
                    .append("&tradingtype=")
                    .append(dto.getTypeCode().getValue());

            String publicUrl = stringBuffer.toString();

            NodeList nList = getByPublicApiItems(publicUrl);

            int itemSize = nList.getLength();

            return IntStream.range(0, itemSize)
                    .filter(num -> Node.ELEMENT_NODE == nList.item(num).getNodeType())
                    .mapToObj(num -> {

                        Node nNode = nList.item(num);

                        Element eElement = (Element) nNode;

                        String regionCd = getTagValue("regionCd", eElement);

                        String rsRow = getTagValue("rsRow", eElement);

                        String regionNm = getTagValue("regionNm", eElement);

                        return TransactionsDTO.Base.builder()
                                .regionCd(regionCd)
                                .regionNm(regionNm)
                                .rsRow(rsRow)
                                .build();
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    private String passerByStandardDate(String standardDate, boolean isYear) {

        if (isYear == Boolean.TRUE) {
            return standardDate;
        }

        int standardDateSize = standardDate.length();

        return standardDate.substring(0, 4) + "-" + standardDate.substring(4, standardDateSize);
    }

    private String getByDateFormat(boolean isYear, String dateStr, boolean isStart) {

        String format;

        if (isStart && isYear) {
            format = "&startyear=%s";
        } else if (isStart && !isYear) {
            format = "&startmonth=%s";
        } else if (!isStart && isYear) {
            format = "&endyear=%s";
        } else {
            format = "&endmonth=%s";
        }

        dateStr = isYear ? dateStr.substring(0, 4) : dateStr;

        return String.format(format, dateStr);
    }

}

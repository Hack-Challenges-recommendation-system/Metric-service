package team.nofantasy.recommendationsystem.metricservice.mapper;

import org.mapstruct.Mapper;
import team.nofantasy.recommendationsystem.metricservice.dto.controller.metric.MaterialUsersControllerDto;
import team.nofantasy.recommendationsystem.metricservice.dto.controller.metric.VariantMetricsControllerDto;
import team.nofantasy.recommendationsystem.metricservice.dto.service.metric.MaterialUsersServiceDto;
import team.nofantasy.recommendationsystem.metricservice.dto.service.metric.VariantMetricsServiceDto;

import java.util.List;

@Mapper
public interface MetricMapper {
    VariantMetricsControllerDto variantMetricsServiceToControllerDto(VariantMetricsServiceDto variantMetrics);

    List<VariantMetricsControllerDto> variantMetricsServiceToControllerDtoList(List<VariantMetricsServiceDto> variants);

    MaterialUsersControllerDto materialUsersServiceToControllerDto(MaterialUsersServiceDto materialUsers);

    List<MaterialUsersControllerDto> materialUsersServiceToControllerDtoList(List<MaterialUsersServiceDto> materials);
}

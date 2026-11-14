package team.nofantasy.recommendationsystem.metricservice.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import team.nofantasy.recommendationsystem.metricservice.dto.controller.event.EventControllerDto;
import team.nofantasy.recommendationsystem.metricservice.dto.controller.event.EventToCreateControllerDto;
import team.nofantasy.recommendationsystem.metricservice.dto.service.event.EventServiceDto;
import team.nofantasy.recommendationsystem.metricservice.dto.service.event.EventToCreateServiceDto;
import team.nofantasy.recommendationsystem.metricservice.entity.Event;

import java.util.List;

@Mapper
public interface EventMapper {
    EventToCreateServiceDto eventToCreateControllerToServiceDto(EventToCreateControllerDto eventToCreateControllerDto);

    List<EventToCreateServiceDto> eventToCreateControllerToServiceDtoList(List<EventToCreateControllerDto> events);

    EventControllerDto eventServiceToControllerDto(EventServiceDto eventServiceDto);

    List<EventControllerDto> eventServiceToControllerDtoList(List<EventServiceDto> events);

    @BeanMapping(ignoreUnmappedSourceProperties = {"id", "userId", "sessionId", "source", "receivedAt"})
    EventServiceDto eventEntityToServiceDto(Event event);
}

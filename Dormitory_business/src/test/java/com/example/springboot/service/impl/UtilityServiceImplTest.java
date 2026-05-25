package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.example.springboot.entity.DormRoom;
import com.example.springboot.entity.UtilityAlert;
import com.example.springboot.entity.UtilityConfig;
import com.example.springboot.entity.UtilityUsage;
import com.example.springboot.mapper.DormRoomMapper;
import com.example.springboot.mapper.UtilityAlertMapper;
import com.example.springboot.mapper.UtilityConfigMapper;
import com.example.springboot.mapper.UtilityUsageMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UtilityServiceImplTest {

    @Test
    void updateConfigAllowsAdminOnly() {
        UtilityServiceImpl service = newService();
        UtilityConfigMapper configMapper = mapper(service, "configMapper");
        UtilityConfig current = config("80.00", "20.00");
        UtilityConfig incoming = config("90.00", "25.00");
        when(configMapper.selectById(1)).thenReturn(current);
        when(configMapper.updateById(any(UtilityConfig.class))).thenReturn(1);

        int result = service.updateConfig(incoming, session("admin"));

        assertThat(result).isEqualTo(1);
        ArgumentCaptor<UtilityConfig> captor = ArgumentCaptor.forClass(UtilityConfig.class);
        verify(configMapper).updateById(captor.capture());
        assertThat(captor.getValue().getElectricLimit()).isEqualByComparingTo("90.00");
        assertThat(captor.getValue().getWaterLimit()).isEqualByComparingTo("25.00");
    }

    @Test
    void updateConfigRejectsNonAdmin() {
        UtilityServiceImpl service = newService();
        UtilityConfigMapper configMapper = mapper(service, "configMapper");

        int dormManagerResult = service.updateConfig(config("90.00", "25.00"), session("dormManager"));
        int studentResult = service.updateConfig(config("90.00", "25.00"), session("stu"));

        assertThat(dormManagerResult).isEqualTo(0);
        assertThat(studentResult).isEqualTo(0);
        verify(configMapper, never()).updateById(any(UtilityConfig.class));
    }

    @Test
    void simulateCreatesWaterAlertWhenWaterUsageExceedsLimit() {
        UtilityServiceImpl service = new FixedUsageUtilityService();
        wireMappers(service);
        DormRoomMapper dormRoomMapper = mapper(service, "dormRoomMapper");
        UtilityConfigMapper configMapper = mapper(service, "configMapper");
        UtilityUsageMapper usageMapper = mapper(service, "usageMapper");
        UtilityAlertMapper alertMapper = mapper(service, "alertMapper");

        DormRoom room = new DormRoom();
        room.setDormRoomId(101);
        room.setDormBuildId(1);
        when(dormRoomMapper.selectList(any(Wrapper.class))).thenReturn(Collections.singletonList(room));
        when(configMapper.selectById(1)).thenReturn(config("80.00", "20.00"));
        when(usageMapper.insert(any(UtilityUsage.class))).thenReturn(1);
        when(alertMapper.insert(any(UtilityAlert.class))).thenReturn(1);

        int result = service.simulate("MANUAL", session("admin"));

        assertThat(result).isEqualTo(1);
        ArgumentCaptor<UtilityAlert> captor = ArgumentCaptor.forClass(UtilityAlert.class);
        verify(alertMapper, times(2)).insert(captor.capture());
        List<UtilityAlert> alerts = captor.getAllValues();
        assertThat(alerts).extracting(UtilityAlert::getAlertType).contains("WATER");
    }

    @SuppressWarnings("unchecked")
    private <T> T mapper(UtilityServiceImpl service, String fieldName) {
        return (T) ReflectionTestUtils.getField(service, fieldName);
    }

    private UtilityServiceImpl newService() {
        UtilityServiceImpl service = new UtilityServiceImpl();
        wireMappers(service);
        return service;
    }

    private void wireMappers(UtilityServiceImpl service) {
        ReflectionTestUtils.setField(service, "dormRoomMapper", mock(DormRoomMapper.class));
        ReflectionTestUtils.setField(service, "usageMapper", mock(UtilityUsageMapper.class));
        ReflectionTestUtils.setField(service, "alertMapper", mock(UtilityAlertMapper.class));
        ReflectionTestUtils.setField(service, "configMapper", mock(UtilityConfigMapper.class));
    }

    private UtilityConfig config(String electricLimit, String waterLimit) {
        return new UtilityConfig(1, new BigDecimal(electricLimit), new BigDecimal(waterLimit), "2026-05-25 00:00:00");
    }

    private MockHttpSession session(String identity) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("Identity", identity);
        session.setAttribute("User", new Object());
        return session;
    }

    private static class FixedUsageUtilityService extends UtilityServiceImpl {
        @Override
        protected BigDecimal randomUsage(BigDecimal limit, boolean allowAlert) {
            return limit.add(BigDecimal.ONE);
        }
    }
}

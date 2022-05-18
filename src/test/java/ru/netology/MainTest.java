package ru.netology;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import ru.netology.entity.Country;
import ru.netology.entity.Location;
import ru.netology.geo.GeoService;
import ru.netology.geo.GeoServiceImpl;
import ru.netology.i18n.LocalizationService;
import ru.netology.i18n.LocalizationServiceImpl;
import ru.netology.sender.MessageSender;
import ru.netology.sender.MessageSenderImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class MainTest {

    public static Stream<Arguments> sourceForMessageSender () {
        return Stream.of(
                Arguments.of("172", new Location(null, Country.RUSSIA, null, 0),
                        GeoServiceImpl.MOSCOW_IP, "Добро пожаловать"),
                Arguments.of("96", new Location(null, Country.USA, null, 0),
                        GeoServiceImpl.NEW_YORK_IP, "Welcome")
        );
    }

    @ParameterizedTest
    @MethodSource("sourceForMessageSender")
    public void test_messageSender_mockito(String startOfIp, Location location, String ip, String expectedResult) {
        GeoService geoService = Mockito.mock(GeoService.class);
        Mockito.when(geoService.byIp(Mockito.startsWith(startOfIp)))
                .thenReturn(location);

        LocalizationService localizationService = Mockito.mock(LocalizationService.class);
        Mockito.when(localizationService.locale(Mockito.any(Country.class)))
                .thenReturn(expectedResult);

        MessageSender messageSender = new MessageSenderImpl(geoService, localizationService);
        Map<String, String> headers = new HashMap<>();
        headers.put(MessageSenderImpl.IP_ADDRESS_HEADER, ip);
        String actual = messageSender.send(headers);

        Assertions.assertEquals(actual, expectedResult);
    }

    public static Stream<Arguments> sourceForGeoService () {
        return Stream.of(
                Arguments.of("172.100.120.55", Country.RUSSIA),
                Arguments.of("96.111.111.111", Country.USA),
                Arguments.of(GeoServiceImpl.LOCALHOST, null),
                Arguments.of("250.250.250.250", null)
        );
    }
    @ParameterizedTest
    @MethodSource("sourceForGeoService")
    public void test_geoServiceImp (String ip, Country expectedResult) {
        GeoService geoService = new GeoServiceImpl();
        Location location = geoService.byIp(ip);
        Country actual = null;
        if (location != null) {
            actual = location.getCountry();
        }
        Assertions.assertEquals(actual, expectedResult);
    }

    public static Stream<Arguments> sourceForLocalizationService () {
        return Stream.of(
                Arguments.of(Country.RUSSIA, "Добро пожаловать"),
                Arguments.of(Country.USA, "Welcome"),
                Arguments.of(Country.BRAZIL, "Welcome"),
                Arguments.of(Country.GERMANY, "Welcome")
        );
    }
    @ParameterizedTest
    @MethodSource("sourceForLocalizationService")
    public void test_LocalizationServiceImp(Country country, String expectedResult) {
        LocalizationService localizationService = new LocalizationServiceImpl();
        String actual = localizationService.locale(country);
        Assertions.assertEquals(actual, expectedResult);
    }

    @Test
    public void test_byCoordinatesMethod() {
        GeoService geoService = new GeoServiceImpl();
        double latitude = Math.random();
        double longitude = Math.random();

        Assertions.assertThrows(RuntimeException.class,
                () -> geoService.byCoordinates(latitude, longitude));
    }
}

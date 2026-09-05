package ru.netology.delivery.test;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.*;
import org.openqa.selenium.Keys;
import ru.netology.delivery.data.DataGenerator;

import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

class DeliveryTest {

    @BeforeAll
    static void setupAll() {
        SelenideLogger.addListener("allure", new AllureSelenide());
    }

    @AfterAll
    static void tearDownAll() {
        SelenideLogger.removeListener("allure");
    }

    @BeforeEach
    void setup() {
        open("http://localhost:9999");
    }

    @Test
    @DisplayName("Should successful plan and replan meeting")
    void shouldSuccessfulPlanAndReplanMeeting() {
        var validUser = DataGenerator.Registration.generateUser("ru");
        var daysToAddForFirstMeeting = 4;
        var firstMeetingDate = DataGenerator.generateDate(daysToAddForFirstMeeting);
        var daysToAddForSecondMeeting = 7;
        var secondMeetingDate = DataGenerator.generateDate(daysToAddForSecondMeeting);

        $("[data-test-id='city'] input").setValue(validUser.getCity());
        $("[data-test-id='date'] input").press(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.DELETE);
        $("[data-test-id='date'] input").setValue(firstMeetingDate);
        $("[data-test-id='name'] input").setValue(validUser.getName());
        $("[data-test-id='phone'] input").setValue(validUser.getPhone());
        $("[data-test-id='agreement']").click();
        $$("button").find(Condition.exactText("Запланировать")).click();
        $("[data-test-id='success-notification'] .notification__title")
                .should(visible)
                .should(Condition.text("Успешно!"));
        $("[data-test-id='success-notification'] .notification__content")
                .should(visible, Duration.ofSeconds(15))
                .should(Condition.text("Встреча успешно запланирована на " + firstMeetingDate));

        $("[data-test-id='date'] input").press(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.DELETE);
        $("[data-test-id='date'] input").setValue(secondMeetingDate);
        $$("button").find(Condition.exactText("Запланировать")).click();
        $("[data-test-id='replan-notification'] .notification__title")
                .should(visible)
                .should(Condition.text("Необходимо подтверждение"));
        $("[data-test-id='replan-notification'] .notification__content")
                .should(visible)
                .should(Condition.text("У вас уже запланирована встреча на другую дату. Перепланировать?"));
        $$("button").find(Condition.exactText("Перепланировать")).click();
        $("[data-test-id='success-notification'] .notification__title")
                .should(visible)
                .should(Condition.text("Успешно!"));
        $("[data-test-id='success-notification'] .notification__content")
                .should(visible, Duration.ofSeconds(15))
                .should(Condition.text("Встреча успешно запланирована на " + secondMeetingDate));

    }
    @Test
    @DisplayName("Should FAIL - phone without country code should be validated")
    void shouldShowErrorForInvalidPhoneFormat() {
        var validUser = DataGenerator.Registration.generateUser("ru");
        var meetingDate = DataGenerator.generateDate(4);
        String invalidPhone = "+"; // Без номера, только символ +

        $("[data-test-id='city'] input").setValue(validUser.getCity());
        $("[data-test-id='date'] input").press(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.DELETE);
        $("[data-test-id='date'] input").setValue(meetingDate);
        $("[data-test-id='name'] input").setValue(validUser.getName());
        $("[data-test-id='phone'] input").setValue(invalidPhone);
        $("[data-test-id='agreement']").click();
        $$("button").find(Condition.exactText("Запланировать")).click();

        // Проверяем, что поле ввода содержит введенный номер
        String enteredPhone = $("[data-test-id='phone'] input").getValue();
        Assertions.assertEquals(invalidPhone, enteredPhone, "Поле телефона должно содержать введенный номер");

        // Проверяем, что появилось сообщение об ошибке для поля телефона
        $("[data-test-id='phone'] .input__sub")
                .should(visible, Duration.ofSeconds(5))
                .should(Condition.text("Введите телефон в формате +7XXXXXXXXXX"));

        // БАГ: Ожидаем ошибку валидации для телефона, но форма принимает его как валидный
        // и показывает успешное планирование вместо ошибки
        $("[data-test-id='success-notification'] .notification__title")
                .should(visible, Duration.ofSeconds(15))
                .should(Condition.text("Успешно!"));
    }
}
package com.paraxialtech.vapals;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SamiBackgroundTest {
    private final WebDriver driver = new HtmlUnitDriver();
    private final Set<String> ignoreFields = ImmutableSet.of("sbwcos"); //Temporarily ignore these fields so remaining tests can run.

    @SuppressWarnings("deprecation")
    private String randomString() {
        return RandomStringUtils.randomAscii(5);
    }

    private List<WebElement> findElements(final WebDriver driver, final String selector) {
        return driver.findElements(By.cssSelector(selector)).stream()
                .filter(WebElement::isEnabled)
                .filter(webElement -> !ignoreFields.contains(webElement.getAttribute("name")))
                .collect(Collectors.toList());
    }

    @TestFactory
    public Iterator<DynamicTest> testAsciiPrintableCharactersForTextFields() {

        //get the initial page and determine fields we'll be testing.
        final String baseUrl = "http://vendev.vistaplex.org:9080/form?form=sbform&studyId=PARAXIAL01";
        driver.get(baseUrl);

        final List<String> textFieldNames = findElements(driver, "input[type='text']").stream().map(webElement -> webElement.getAttribute("name")).collect(Collectors.toList());

        return textFieldNames.stream().map(textFieldName -> DynamicTest.dynamicTest("Test ASCII " + textFieldName, () -> {
            final WebElement textField = driver.findElement(By.name(textFieldName));
            final String asciiText = RandomStringUtils.randomPrint(100);
            assertNotNull(textField, "Could not find field by name " + textFieldName);
            textField.clear();
            textField.sendKeys(asciiText);
            textField.submit();
            driver.navigate().to(baseUrl); //reload the initial page
            assertThat("Incorrect value in dropdown field " + textFieldName, driver.findElement(By.name(textFieldName)).getAttribute("value"), is(asciiText));
        })).iterator();
    }

    @TestFactory
    public Iterator<DynamicTest> testSaveAllText() {

        //get the initial page and determine fields we'll be testing.
        final String baseUrl = "http://vendev.vistaplex.org:9080/form?form=sbform&studyId=PARAXIAL01";
        driver.get(baseUrl);

        final List<String> textFieldNames = findElements(driver, "input[type='text']").stream().map(webElement -> webElement.getAttribute("name")).collect(Collectors.toList());
        return textFieldNames.stream().map(textFieldName -> DynamicTest.dynamicTest("Test save text " + textFieldName, () -> {
            final WebElement textField = driver.findElement(By.name(textFieldName));
            final String asciiText = RandomStringUtils.randomAlphabetic(100);
            assertNotNull(textField, "Could not find field by name " + textFieldName);
            textField.clear();
            textField.sendKeys(asciiText);
            textField.submit();
            driver.navigate().to(baseUrl); //reload the initial page
            assertThat("Incorrect value in dropdown field " + textFieldName, driver.findElement(By.name(textFieldName)).getAttribute("value"), is(asciiText));
        })).iterator();
    }

    @TestFactory
    public Iterator<DynamicTest> testSaveAllDropdowns() {

        //get the initial page and determine fields we'll be testing.
        final String baseUrl = "http://vendev.vistaplex.org:9080/form?form=sbform&studyId=PARAXIAL01";
        driver.get(baseUrl);

        final List<String> dropdownNames = findElements(driver, "select").stream().map(webElement -> webElement.getAttribute("name")).collect(Collectors.toList());
        return dropdownNames.stream().map(dropdownName -> DynamicTest.dynamicTest("Test save dropdown " + dropdownName, () -> {
            final WebElement dropdown = driver.findElement(By.name(dropdownName));
            final List<WebElement> options = dropdown.findElements(By.tagName("option"));
            final WebElement selectedOption = options.get(RandomUtils.nextInt(0, options.size()));
            selectedOption.click();
            String savedValue = selectedOption.getAttribute("value");

            dropdown.submit();
            driver.navigate().to(baseUrl); //reload the initial page


            WebElement updatedDropdown = driver.findElement(By.name(dropdownName));
            assertNotNull(updatedDropdown, "No dropdown by name of " + dropdownName);
            final WebElement updatedOption = updatedDropdown.findElement(By.cssSelector("option[selected]"));
            assertNotNull(selectedOption, "No selected option for dropdown " + dropdownName);

            final String actual = updatedOption.getAttribute("value");
            assertThat("Incorrect value in dropdown field " + dropdownName, actual, is(savedValue));

        })).iterator();
    }

    @TestFactory
    public Iterator<DynamicTest> testSaveAllRadios() {

        //get the initial page and determine fields we'll be testing.
        final String baseUrl = "http://vendev.vistaplex.org:9080/form?form=sbform&studyId=PARAXIAL01";
        driver.get(baseUrl);

        final Set<String> radioElementGroups = findElements(driver, "input[type='radio']").stream().map(webElement -> webElement.getAttribute("name")).distinct().collect(Collectors.toSet());
        return radioElementGroups.stream().map(radioGroupName -> DynamicTest.dynamicTest("Test save radio " + radioGroupName, () -> {

            final List<WebElement> radioOptions = findElements(driver, "input[type='radio'][name='" + radioGroupName + "']");
            final WebElement option = radioOptions.get(RandomUtils.nextInt(0, radioOptions.size())); //pick random option
            String submittedValue = option.getAttribute("value");
            option.click();
            option.submit();

            driver.navigate().to(baseUrl); //reload the initial page
            final WebElement updatedOption = driver.findElement(By.cssSelector("input[type=radio][name=" + radioGroupName + "]:checked"));
            assertNotNull(updatedOption, "No value selected for radio field " + radioGroupName);
            final String actual = updatedOption.getAttribute("value");
            assertThat("Incorrect value in radio field " + radioGroupName, actual, is(submittedValue));


        })).iterator();
    }

    @TestFactory
    public Iterator<DynamicTest> testSaveAllTextAreas() {

        //get the initial page and determine fields we'll be testing.
        final String baseUrl = "http://vendev.vistaplex.org:9080/form?form=sbform&studyId=PARAXIAL01";
        driver.get(baseUrl);

        final Set<String> textAreaNames = findElements(driver, "textarea").stream().map(webElement -> webElement.getAttribute("name")).distinct().collect(Collectors.toSet());
        return textAreaNames.stream().map(textAreaName -> DynamicTest.dynamicTest("Test save textarea " + textAreaName, () -> {

            final WebElement textarea = driver.findElement(By.name(textAreaName));
            assertNotNull(textarea, "No textarea by name of " + textAreaName);

            final String submittedValue = RandomStringUtils.randomAlphabetic(10) + "\n" + RandomStringUtils.randomAlphabetic(10) + "\n";
            textarea.clear();
            textarea.sendKeys(submittedValue);
            textarea.submit();

            driver.navigate().to(baseUrl); //reload the initial page
            final WebElement updatedTextarea = driver.findElement(By.name(textAreaName));
            assertNotNull(updatedTextarea, "No textarea found by name of " + textAreaName);
            assertThat("Incorrect value in textarea field " + textAreaName, updatedTextarea.getText(), is(submittedValue.trim()));

        })).iterator();
    }


    @TestFactory
    public Iterator<DynamicTest> testSaveAllCheckboxes() {

        //get the initial page and determine fields we'll be testing.
        final String baseUrl = "http://vendev.vistaplex.org:9080/form?form=sbform&studyId=PARAXIAL01";
        driver.get(baseUrl);

        final Set<String> checkboxNames = findElements(driver, "input[type='checkbox']").stream().map(webElement -> webElement.getAttribute("name")).distinct().collect(Collectors.toSet());
        return checkboxNames.stream().map(checkboxName -> DynamicTest.dynamicTest("Test save checkbox " + checkboxName, () -> {

            WebElement checkbox = driver.findElement(By.name(checkboxName));
            assertNotNull(checkbox, "Checkbox by name " + checkboxName + " not found");
            String submittedValue = checkbox.getAttribute("value");
            checkbox.click();
            checkbox.submit();

            driver.navigate().to(baseUrl); //reload the initial page


            final WebElement updatedCheckbox = driver.findElement(By.cssSelector("input[type='checkbox'][name='" + checkboxName + "'][value='" + submittedValue + "']"));
            assertNotNull(updatedCheckbox, "No checkbox by name of " + checkboxName);
            final String checked = updatedCheckbox.getAttribute("checked");
            assertThat("Checkbox " + checkboxName + " should be checked", checked, is("true"));
        })).iterator();
    }


    @Test
    public void testSaveAllFields() throws Exception {
        final Map<String, String> checkboxValues = new HashMap<>();

        final String baseUrl = "http://vendev.vistaplex.org:9080/form?form=sbform&studyId=PARAXIAL01";

        driver.get(baseUrl);


        //check all checkboxes
        final List<WebElement> checkboxElements = findElements(driver, "input[type='checkbox']");
        checkboxElements.forEach(webElement -> {
            webElement.click();
            checkboxValues.put(webElement.getAttribute("name"), webElement.getAttribute("value"));
        });


        driver.findElement(By.cssSelector("input[type='submit']")).submit();

        //ASSERTIONS
        driver.navigate().to(baseUrl); //reload the initial page


        checkboxValues.forEach((fieldName, submittedValue) -> {
            final WebElement checkbox = driver.findElement(By.cssSelector("input[type='checkbox'][name='" + fieldName + "'][value='" + submittedValue + "']"));
            assertThat("No checkbox by name of " + fieldName, checkbox, notNullValue());
            final String checked = checkbox.getAttribute("checked");
            assertThat("Checkbox " + fieldName + " should be checked", checked, is("true"));
        });


        driver.close();

    }
}

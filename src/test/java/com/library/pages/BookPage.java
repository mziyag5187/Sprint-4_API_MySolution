package com.library.pages;

import com.library.utility.BrowserUtil;
import com.library.utility.Driver;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import java.security.Key;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BookPage extends BasePage {

    @FindBy(xpath = "//table/tbody/tr")
    public List<WebElement> allRows;

    @FindBy(xpath = "//input[@type='search']")
    public WebElement search;

    @FindBy(id = "book_categories")
    public WebElement mainCategoryElement;

    @FindBy(name = "name")
    public WebElement bookName;


    @FindBy(xpath = "(//input[@type='text'])[4]")
    public WebElement author;

    @FindBy(xpath = "//div[@class='portlet-title']//a")
    public WebElement addBook;

    @FindBy(xpath = "//button[@type='submit']")
    public WebElement saveChanges;

    @FindBy(xpath = "//div[@class='toast-message']")
    public WebElement toastMessage;

    @FindBy(name = "year")
    public WebElement year;

    @FindBy(name = "isbn")
    public WebElement isbn;

    @FindBy(id = "book_group_id")
    public WebElement categoryDropdown;


    @FindBy(xpath = "//tbody//tr[1]//td[5]")
    public WebElement categoryNameForFirstRow;

    @FindBy(id = "description")
    public WebElement description;



    public WebElement editBook(String book) {
        String xpath = "//td[3][.='" + book + "']/../td/a";
        return Driver.getDriver().findElement(By.xpath(xpath));
    }

    public WebElement borrowBook(String book) {
        String xpath = "//td[3][.='" + book + "']/../td/a";
        return Driver.getDriver().findElement(By.xpath(xpath));
    }

    public Map<String,Object> getNewCreatedBookInfo_UI(String bookName){
        search.sendKeys(bookName + Keys.ENTER );
        BrowserUtil.waitFor(2);
        editBook(bookName).click();
        BrowserUtil.waitFor(2);

        Map<String,Object> bookInfo = new LinkedHashMap<>();

        bookInfo.put("bookName",this.bookName.getAttribute("value"));
        bookInfo.put("isbn",isbn.getAttribute("value"));
        bookInfo.put("year",Integer.parseInt(year.getAttribute("value")));
        bookInfo.put("author",this.author.getAttribute("value"));
        Select select = new Select(categoryDropdown);
        bookInfo.put("bookCategoryName",select.getFirstSelectedOption().getText());

        return bookInfo;
    }

}
